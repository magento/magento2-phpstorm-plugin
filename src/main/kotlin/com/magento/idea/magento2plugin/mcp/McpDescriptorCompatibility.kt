/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.mcpserver.McpToolCategory
import com.intellij.mcpserver.McpToolDescriptor
import com.intellij.mcpserver.McpToolSchema
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/**
 * 2026.3 adds optional display fields to the public MCP constructors without retaining
 * their 2026.2 JVM signatures. Bind the shared parameters by name and let the host
 * supply its defaults so the same plugin binary can discover tools in both IDEs.
 */
internal object McpDescriptorCompatibility {
    fun descriptor(name: String, description: String, inputSchema: McpToolSchema): McpToolDescriptor {
        val category = construct(McpToolCategory::class, mapOf(
            "shortName" to "MagentoMcpToolset",
            "fullyQualifiedName" to MagentoMcpToolset::class.java.name,
            "isExperimental" to false,
            "alwaysIncluded" to false
        ))
        return construct(McpToolDescriptor::class, mapOf(
            "name" to name,
            "description" to description,
            "category" to category,
            "fullyQualifiedName" to "${MagentoMcpToolset::class.java.name}.magentoCompatibility",
            "inputSchema" to inputSchema
        ))
    }

    private fun <T : Any> construct(type: KClass<T>, arguments: Map<String, Any?>): T {
        val constructor = requireNotNull(type.primaryConstructor) { "Missing MCP constructor: ${type.qualifiedName}" }
        val parameters = constructor.parameters.associateBy { it.name }
        require(parameters.keys.containsAll(arguments.keys)) { "Unsupported MCP constructor: ${type.qualifiedName}" }
        return constructor.callBy(arguments.mapKeys { (name, _) -> parameters.getValue(name) })
    }
}
