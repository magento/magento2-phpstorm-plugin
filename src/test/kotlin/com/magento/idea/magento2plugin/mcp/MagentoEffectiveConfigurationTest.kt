/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import org.junit.Assert.*
import org.junit.Test

class MagentoEffectiveConfigurationTest {
    private val first = "Magento_CatalogInventory"
    private val second = "Magento_InventorySales"
    private val modules = linkedMapOf(first to true, second to true)
    private val original = ConfigDeclaration("sales_order_item_cancel", "inventory", "original.xml",
        "global", first, "OldObserver", null)
    private val replacement = original.copy(file = "replacement.xml", module = second, instance = "NewObserver")

    @Test fun moduleOrderIsReadFromTheLiteralTopLevelModulesArray() {
        val config = """<?php return array(
            'system' => ['modules' => ['Wrong_Module' => 1], 'value' => '], //'],
            /* generated */ "modules" => array('$second' => 0, '$first' => 1,),
        );"""
        val parsed = MagentoModuleLoadOrder.parse(config)!!
        assertEquals(listOf(second, first), parsed.keys.toList())
        assertEquals(false, parsed[second])
        assertNull(MagentoModuleLoadOrder.parse("<?php return ['modules' => ['"))
        assertNull(MagentoModuleLoadOrder.parse(config.replace("=> 0", "=> getenv('ON')")))
        assertNull(MagentoModuleLoadOrder.parse(config.replace("\"modules\"", "'comment'")))
    }

    @Test fun laterModuleReplacesTheInstanceInEitherLoadOrder() {
        for (order in listOf(modules, linkedMapOf(second to true, first to true))) {
            val rows = MagentoEffectiveConfiguration.resolve(listOf(replacement, original), "frontend", order, true)
            val winner = if (order.keys.last() == second) replacement else original
            assertEquals(winner.instance, rows.single { it.status == "applies" }.instance)
            assertEquals(winner.file, rows.single { it.status == "overridden" }.decidedBy)
        }
    }

    @Test fun globalAndAreaDisableRetainTheirSourceAndDisabledModulesDoNotOverride() {
        val disable = replacement.copy(instance = null, disabled = true)
        for (order in listOf(modules, linkedMapOf(second to true, first to true))) {
            val rows = MagentoEffectiveConfiguration.resolve(listOf(original, disable), "frontend", order, true)
            assertTrue(rows.all { it.status == "disabled" && it.decidedBy == disable.file })
        }
        val local = disable.copy(area = "adminhtml")
        for ((area, expected) in listOf("adminhtml" to "disabled", "frontend" to "applies")) {
            val rows = MagentoEffectiveConfiguration.resolve(listOf(local, original), area, modules, true)
            assertEquals(expected, rows.single { it.source == original }.status)
        }
        val rows = MagentoEffectiveConfiguration.resolve(listOf(original, disable), "global",
            modules + (second to false), true)
        assertEquals(listOf("disabled_module", "applies"), rows.map { it.status })
    }

    @Test fun childPluginCanDisableOrReenableAnInheritedPluginWithoutRepeatingItsClass() {
        val parent = original.copy(subject = "ParentInterface", sortOrder = 12)
        val child = replacement.copy(subject = "Child", instance = null, area = "adminhtml", disabled = true)
        val rows = MagentoEffectiveConfiguration.resolve(listOf(parent, child), "adminhtml", modules, false)
        assertTrue(rows.all { it.status == "disabled" && it.decidedBy == child.file })
        val enabled = MagentoEffectiveConfiguration.resolve(listOf(parent.copy(disabled = true),
            child.copy(disabled = false, sortOrder = 4)), "adminhtml", modules, false).single { it.status == "applies" }
        assertEquals(parent.instance, enabled.instance)
        assertEquals(4, enabled.sortOrder)
    }

    @Test fun untypedDisableOnASiblingInterfaceDoesNotDisableTheSuperclassPlugin() {
        val parent = original.copy(subject = "Superclass")
        val unrelated = replacement.copy(subject = "OtherInterface", instance = null, disabled = true)
        val rows = MagentoEffectiveConfiguration.resolve(listOf(parent, unrelated), "global", modules,
            observer = false, interfaces = setOf("OtherInterface"))
        assertEquals("applies", rows.single { it.source == parent }.status)
        assertEquals("not_loaded", rows.single { it.source == unrelated }.status)
    }

    @Test fun missingModuleOrderReportsCandidatesAndDeclarationModeRetainsBothFiles() {
        for ((area, expected) in listOf("global" to "candidate", null to "declaration")) {
            val rows = MagentoEffectiveConfiguration.resolve(listOf(original, replacement), area, null, true)
            assertEquals(listOf(expected, expected), rows.map { it.status })
        }
    }

    @Test fun observersMergeXmlWithinAnAreaBeforeConvertingDisabledFlags() {
        val disabled = original.copy(disabled = true)
        val enabled = replacement.copy(disabled = false)
        assertEquals("applies", MagentoEffectiveConfiguration.resolve(listOf(disabled, enabled),
            "global", modules, true).last().status)
        assertTrue(MagentoEffectiveConfiguration.resolve(listOf(disabled, enabled.copy(area = "frontend")),
            "frontend", modules, true).all { it.status == "disabled" && it.decidedBy == original.file })
    }
}
