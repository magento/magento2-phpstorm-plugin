/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.xml

import com.intellij.driver.client.Driver
import com.intellij.driver.client.Remote
import com.intellij.driver.client.utility
import com.intellij.driver.model.LockSemantics
import com.intellij.driver.model.OnDispatcher
import com.intellij.driver.sdk.Project
import com.intellij.driver.sdk.getHighlights
import com.intellij.driver.sdk.invokeAction
import com.intellij.driver.sdk.isPluginLoaded
import com.intellij.driver.sdk.openFile
import com.intellij.driver.sdk.selectedTextEditor
import com.intellij.driver.sdk.singleProject
import com.intellij.driver.sdk.waitFor
import com.magento.idea.magento2plugin.ui.runWebStormUiTest
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.seconds

class XmlIntelligenceUiTest {
    @Test
    fun `completes navigates and inspects declarations without PHP`() = runWebStormUiTest(
        "file-generators",
        prepareProject = ::prepareDeclarations,
    ) {
        check(!isPluginLoaded("com.jetbrains.php")) { "This test must run without the PHP plugin" }

        assertCompletion("$CONSUMER/etc/di.xml", "type=\"AcmeVirt", "AcmeVirtualOne", "AcmeVirtualTwo")
        assertCompletion("$CONSUMER/etc/events.xml", "name=\"acme_declared_", "acme_declared_one", "acme_declared_two")

        assertNavigation("$CONSUMER/etc/di.xml", "type=\"AcmeVirtualOne", "$DECLARATIONS/etc/di.xml")
        assertNavigation("$CONSUMER/etc/di.xml", "name=\"acme_plugin", "$DECLARATIONS/etc/di.xml")
        assertNavigation("$CONSUMER/etc/events.xml", "name=\"acme_observer", "$DECLARATIONS/etc/events.xml")

        assertInspection("$CONSUMER/etc/di.xml", "This plugin does not exist to be disabled.", "missing_plugin", "acme_plugin")
        assertInspection("$CONSUMER/etc/events.xml", "This observer does not exist to be disabled.", "missing_observer", "acme_observer")
    }

    private fun Driver.assertCompletion(path: String, caretPrefix: String, vararg expected: String) {
        openAt(path, caretPrefix)
        invokeAction("CodeCompletion")
        waitFor("Magento completion in $path", 30.seconds) {
            withContext(OnDispatcher.EDT, semantics = LockSemantics.READ_ACTION) {
                val lookup = utility<XmlLookupManager>().getInstance(singleProject()).getActiveLookup()
                val names = lookup?.getItems()?.map { it.getLookupString() }.orEmpty()
                lookup?.isCalculating() == false && expected.all { name -> names.count { it == name } == 1 }
            }
        }
        invokeAction("EditorEscape")
    }

    private fun Driver.assertNavigation(path: String, caretPrefix: String, target: String) {
        openAt(path, caretPrefix)
        invokeAction("GotoDeclaration")
        waitFor("Magento declaration in $target", 30.seconds) {
            selectedTextEditor()?.getVirtualFile()?.getPath()?.endsWith(target) == true
        }
    }

    private fun Driver.assertInspection(path: String, message: String, invalidName: String, validName: String) {
        openFile(path)
        val editor = checkNotNull(selectedTextEditor())
        val text = editor.getDocument().getText()
        waitFor("Magento inspection for $invalidName", 30.seconds) {
            getHighlights(editor.getDocument()).any {
                it.getDescription()?.startsWith(message) == true && it.getText().contains(invalidName)
            }
        }
        check(getHighlights(editor.getDocument()).none {
            it.getDescription()?.startsWith(message) == true && it.getText().contains(validName)
        }) { "Valid declaration was reported as missing in $path: $text" }
    }

    private fun Driver.openAt(path: String, caretPrefix: String) {
        openFile(path)
        withContext(OnDispatcher.EDT, semantics = LockSemantics.READ_ACTION) {
            val editor = checkNotNull(selectedTextEditor())
            val offset = editor.getDocument().getText().indexOf(caretPrefix)
            check(offset >= 0) { "Missing caret text $caretPrefix in $path" }
            editor.getCaretModel().moveToOffset(offset + caretPrefix.length)
        }
    }

    private fun prepareDeclarations(root: Path) {
        fun file(path: String, text: String) {
            Files.createDirectories(root.resolve(path).parent)
            root.resolve(path).writeText(text.trimIndent())
        }
        file("$DECLARATIONS/etc/module.xml", "<config><module name=\"Acme_Declarations\"/></config>")
        file("$CONSUMER/etc/module.xml", "<config><module name=\"Acme_Consumer\"/></config>")
        file("$DECLARATIONS/etc/di.xml", """
            <config>
                <virtualType name="AcmeVirtualOne" type="Acme\Model"/>
                <virtualType name="AcmeVirtualTwo" type="Acme\Model"/>
                <type name="Acme\Target"><plugin name="acme_plugin" type="Acme\Plugin"/></type>
            </config>
        """)
        file("$CONSUMER/etc/di.xml", """
            <config>
                <virtualType name="Completion" type="AcmeVirt"/>
                <virtualType name="Navigation" type="AcmeVirtualOne"/>
                <type name="Acme\Target">
                    <plugin name="acme_plugin" disabled="true"/>
                    <plugin name="missing_plugin" disabled="true"/>
                </type>
            </config>
        """)
        file("$DECLARATIONS/etc/events.xml", """
            <config>
                <event name="acme_declared_one"><observer name="acme_observer" instance="Acme\Observer"/></event>
                <event name="acme_declared_two"><observer name="another_observer" instance="Acme\Observer"/></event>
            </config>
        """)
        file("$CONSUMER/etc/events.xml", """
            <config>
                <event name="acme_declared_"/>
                <event name="acme_declared_one">
                    <observer name="acme_observer" disabled="true"/>
                    <observer name="missing_observer" disabled="true"/>
                </event>
            </config>
        """)
    }

    companion object {
        private const val DECLARATIONS = "app/code/Acme/Declarations"
        private const val CONSUMER = "app/code/Acme/Consumer"
    }
}

@Remote("com.intellij.codeInsight.lookup.LookupManager")
private interface XmlLookupManager {
    fun getInstance(project: Project): XmlLookupManager
    fun getActiveLookup(): XmlLookup?
}

@Remote("com.intellij.codeInsight.lookup.impl.LookupImpl")
private interface XmlLookup {
    fun getItems(): List<XmlLookupElement>
    fun isCalculating(): Boolean
}

@Remote("com.intellij.codeInsight.lookup.LookupElement")
private interface XmlLookupElement {
    fun getLookupString(): String
}
