package com.magento.idea.magento2plugin

import com.intellij.ide.IdeView
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.impl.PresentationFactory
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.actionSystem.impl.Utils
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.util.indexing.FileBasedIndex
import com.magento.idea.magento2plugin.project.Settings
import com.magento.idea.magento2plugin.stubs.indexes.xml.ModuleXmlIndex
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit

/** Temporary probe: real NTFS junctions on Windows, directory symlinks on Linux. */
class Issue1296ReproductionTest : PhysicalMagentoTestCase() {
    private lateinit var root: Path
    private lateinit var external: Path
    private val links = mutableListOf<Path>()

    override fun setUp() {
        VfsRootAccess.allowRootAccess(
            testRootDisposable, Path.of(".intellijPlatform").toAbsolutePath().normalize().toString()
        )
        super.setUp()
        root = Path.of(myFixture.tempDirFixture.tempDirPath)
        external = Files.createTempDirectory("issue1296 external ")
        VfsRootAccess.allowRootAccess(testRootDisposable, external.toString())
        Settings.getInstance(project).apply {
            pluginEnabled = true
            magentoPath = root.toString().replace('\\', '/')
        }
    }

    override fun tearDown() {
        try {
            // Remove links before either fixture cleanup or deleting their targets.
            links.asReversed().forEach { Files.deleteIfExists(it) }
            if (::external.isInitialized) {
                Files.walk(external).use { paths ->
                    paths.sorted(Comparator.reverseOrder()).forEach { Files.deleteIfExists(it) }
                }
            }
        } finally {
            super.tearDown()
        }
    }

    fun testOrdinaryModuleDirectoryControl() {
        val module = root.resolve("app/code/Probe/Extension")
        writeModule(module)
        assertModuleMenu(module)
    }

    fun testModuleJunctionToExternalDirectory() {
        checkJunction(vendorLevel = false, targetInsideProject = false)
    }

    fun testVendorJunctionToExternalDirectory() {
        checkJunction(vendorLevel = true, targetInsideProject = false)
    }

    fun testModuleJunctionWithTargetAlsoInsideProject() {
        checkJunction(vendorLevel = false, targetInsideProject = true)
    }

    fun testVendorJunctionWithTargetAlsoInsideProject() {
        checkJunction(vendorLevel = true, targetInsideProject = true)
    }

    private fun checkJunction(vendorLevel: Boolean, targetInsideProject: Boolean) {
        val target = (if (targetInsideProject) root.resolve("development sources") else external)
            .resolve("Probe")
        writeModule(target.resolve("Extension"))
        val link = root.resolve(if (vendorLevel) "app/code/Probe" else "app/code/Probe/Extension")
        val destination = if (vendorLevel) target else target.resolve("Extension")
        Files.createDirectories(link.parent)
        if (SystemInfo.isWindows) {
            val script = """
                ${'$'}ErrorActionPreference = 'Stop'
                ${'$'}item = New-Item -ItemType Junction -Path ${'$'}env:ISSUE1296_LINK -Target ${'$'}env:ISSUE1296_TARGET
                if ((Get-Item -LiteralPath ${'$'}env:ISSUE1296_LINK).LinkType -ne 'Junction') { throw 'Not an NTFS junction' }
            """.trimIndent()
            val process = ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-Command", script)
                .redirectErrorStream(true)
                .apply {
                    environment()["ISSUE1296_LINK"] = link.toString()
                    environment()["ISSUE1296_TARGET"] = destination.toString()
                }.start()
            assertTrue("Junction creation timed out", process.waitFor(30, TimeUnit.SECONDS))
            assertEquals(process.inputStream.bufferedReader().readText(), 0, process.exitValue())
        } else {
            Files.createSymbolicLink(link, destination)
        }
        links.add(link)
        assertTrue(Files.isSameFile(link, destination))
        val module = root.resolve("app/code/Probe/Extension")
        assertModuleMenu(module)
        // A repeated popup after refresh checks the reported inconsistent behavior.
        assertModuleMenu(module)
    }

    private fun writeModule(module: Path) {
        Files.createDirectories(module.resolve("etc"))
        Files.writeString(module.resolve("etc/module.xml"), """
            <config xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:noNamespaceSchemaLocation="urn:magento:framework:Module/etc/module.xsd">
                <module name="Probe_Extension"/>
            </config>
        """.trimIndent())
        Files.writeString(module.resolve("registration.php"), """
            <?php
            \Magento\Framework\Component\ComponentRegistrar::register(
                \Magento\Framework\Component\ComponentRegistrar::MODULE, 'Probe_Extension', __DIR__
            );
        """.trimIndent())
    }

    private fun assertModuleMenu(module: Path) {
        val rootFile = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(root)!!
        VfsUtil.markDirtyAndRefresh(false, true, true, rootFile)
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        val virtual = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(module)!!
        val directory = psiManager.findDirectory(virtual)!!
        val indexed = FileBasedIndex.getInstance().getContainingFiles(
            ModuleXmlIndex.KEY, "Probe_Extension", GlobalSearchScope.projectScope(project)
        ).map { it.path }
        val evidence = "selected=${virtual.path}; canonical=${virtual.canonicalPath}; indexed=$indexed"
        assertTrue("Module declaration was not indexed: $evidence", indexed.isNotEmpty())
        val manager = ActionManager.getInstance()
        val group = manager.getAction("MagentoNewModuleFileGroup") as ActionGroup
        val view = object : IdeView {
            override fun selectElement(element: PsiElement) {}
            override fun getDirectories(): Array<PsiDirectory> = arrayOf(directory)
            override fun getOrChooseDirectory(): PsiDirectory = directory
        }
        val context = Utils.createAsyncDataContext(SimpleDataContext.builder()
            .add(CommonDataKeys.PROJECT, project)
            .add(CommonDataKeys.PSI_ELEMENT, directory)
            .add(LangDataKeys.PSI_ELEMENT_ARRAY, arrayOf<PsiElement>(directory))
            .add(CommonDataKeys.VIRTUAL_FILE, virtual)
            .add(CommonDataKeys.VIRTUAL_FILE_ARRAY, arrayOf(virtual))
            .add(LangDataKeys.IDE_VIEW, view)
            .build())
        val presentations = PresentationFactory()
        val popup = PlatformTestUtil.waitForPromise(Utils.expandActionGroupAsync(
            DefaultActionGroup(group), presentations, context, ActionPlaces.PROJECT_VIEW_POPUP,
            ActionUiKind.POPUP, false
        ), 30_000)!!
        assertTrue("Magento module submenu hidden: $evidence", popup.contains(group))
        assertTrue(evidence, presentations.getPresentation(group).isEnabledAndVisible)
        val children = PlatformTestUtil.waitForPromise(Utils.expandActionGroupAsync(
            group, presentations, context, ActionPlaces.PROJECT_VIEW_POPUP, ActionUiKind.POPUP, false
        ), 30_000)!!
        for (id in listOf("MagentoCreateADataModel", "MagentoCreateCLICommand")) {
            val action = manager.getAction(id)
            assertNotNull(id, action)
            assertTrue("Missing $id: $evidence", children.contains(action))
            assertTrue("Disabled $id: $evidence", presentations.getPresentation(action).isEnabledAndVisible)
        }
    }
}
