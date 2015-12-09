package com.magento.idea.magento2plugin.php.module;

import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.php.index.ModulePackageFileBasedIndex;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by dkvashnin on 12/5/15.
 */
public class ModuleManager {
    private Map<String, MagentoModule> modules = new HashMap<>();
    private long cacheStartTime;
    private static final int CACHE_LIFE_TIME = 20000;
    private static ModuleManager moduleManager;
    private Project project;

    private ModuleManager(Project project){
        this.project = project;
    }

    public static ModuleManager getInstance(Project project) {
        if (moduleManager == null) {
            moduleManager = new ModuleManager(project);
        }
        return moduleManager;
    }

    synchronized public Collection<MagentoModule> getModules() {
        if (cacheStartTime + CACHE_LIFE_TIME < System.currentTimeMillis()) {
            flushModules();
            loadModules();
            cacheStartTime = System.currentTimeMillis();
        }

        return modules.values();
    }

    @Nullable
    public MagentoModule getModuleForFile(PsiFile psiFile) {
        for (MagentoModule magentoModule: this.getModules()) {
            if (magentoModule.isFileInContext(psiFile)) {
                return magentoModule;
            }
        }

        return null;
    }

    public void flushModules() {
        modules = new HashMap<>();
    }

    private void loadModules() {
        Collection<String> packages = FileBasedIndex.getInstance().getAllKeys(ModulePackageFileBasedIndex.NAME, this.project);
        PsiManager psiManager = PsiManager.getInstance(this.project);
        for (String packageName: packages) {
            if (modules.containsKey(packageName)) {
                continue;
            }


            Collection<VirtualFile> containingFiles = FileBasedIndex.getInstance()
                .getContainingFiles(ModulePackageFileBasedIndex.NAME, packageName, GlobalSearchScope.allScope(this.project));

            if (containingFiles.size() > 0) {
                VirtualFile configurationFile = containingFiles.iterator().next();

                PsiFile psiFile = psiManager.findFile(configurationFile);
                if (psiFile != null && psiFile instanceof JsonFile) {
                    JsonObject jsonObject = PsiTreeUtil.getChildOfType((JsonFile) psiFile, JsonObject.class);
                    modules.put(
                        packageName,
                        new MagentoModuleImpl(new ComposerPackageModelImpl(jsonObject), psiFile.getContainingDirectory())
                    );
                }
            }
        }
    }
}

/**
 * Created by dkvashnin on 12/5/15.
 */
class MagentoModuleImpl implements MagentoModule {
    private ComposerPackageModel composerPackageModel;
    private PsiDirectory directory;
    private static final String DEFAULT_MODULE_NAME = "Undefined module";
    private static final String CONFIGURATION_PATH = "etc";

    public MagentoModuleImpl(ComposerPackageModel composerPackageModel, PsiDirectory directory) {

        this.composerPackageModel = composerPackageModel;
        this.directory = directory;
    }

    @Nullable
    @Override
    public String getMagentoName() {
        PsiDirectory configurationDir = directory.findSubdirectory(CONFIGURATION_PATH);
        if (configurationDir != null) {
            PsiFile configurationFile = configurationDir.findFile("module.xml");

            if (configurationFile != null && configurationFile instanceof XmlFile) {
                XmlTag rootTag = ((XmlFile) configurationFile).getRootTag();
                if (rootTag != null) {
                    XmlTag module = rootTag.findFirstSubTag("module");
                    if (module != null && module.getAttributeValue("name") != null) {
                        return module.getAttributeValue("name");
                    }
                }
            }
        }

        return DEFAULT_MODULE_NAME;
    }

    @Nullable
    @Override
    public List<MagentoModule> getMagentoDependencies() {
        return null;
    }

    @Nullable
    @Override
    public ComposerPackageModel getComposerModel() {
        return composerPackageModel;
    }

    @Override
    public boolean isFileInContext(PsiFile psiFile) {
        PsiDirectory containingDirectory = psiFile.getContainingDirectory();
        while (containingDirectory != null) {
            if (containingDirectory.getManager().areElementsEquivalent(containingDirectory, directory)) {
                return true;
            }

            containingDirectory = containingDirectory.getParentDirectory();
        }

        return false;
    }

    @Override
    public boolean isClassInContext(PhpClass phpClass) {
        return false;
    }

    @Override
    public PsiDirectory getSourceDirectory() {
        return directory;
    }
}