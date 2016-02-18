package com.magento.idea.magento2plugin.php.module;

import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonObject;
import com.intellij.openapi.project.DumbService;
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
import com.magento.idea.magento2plugin.php.index.ModulePackageFileBasedIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by dkvashnin on 12/5/15.
 */
public class MagentoComponentManager {
    private Map<String, MagentoComponent> components = new HashMap<>();
    private long cacheStartTime;
    private static final int CACHE_LIFE_TIME = 20000;
    private static MagentoComponentManager magentoComponentManager;
    private Project project;

    private MagentoComponentManager(Project project){
        this.project = project;
    }

    public static MagentoComponentManager getInstance(@NotNull Project project) {
        if (magentoComponentManager == null) {
            magentoComponentManager = new MagentoComponentManager(project);
        }
        return magentoComponentManager;
    }

    public Collection<MagentoComponent> getAllComponents() {
        return getComponents().values();
    }

    @SuppressWarnings("unchecked")
    public <T extends MagentoComponent> Collection<T> getAllComponentsOfType(@NotNull Class<T> type) {
        Collection<T> result = new ArrayList<>();
        Map<String, MagentoComponent> components = getComponents();
        for (String key: components.keySet()) {
            if (type.isInstance(components.get(key))) {
                result.add((T)components.get(key));
            }
        }

        return result;
    }

    synchronized private Map<String, MagentoComponent> getComponents() {
        if (DumbService.getInstance(project).isDumb() || project.isDisposed()) {
            return new HashMap<>();
        }

        if (cacheStartTime + CACHE_LIFE_TIME < System.currentTimeMillis()) {
            flushModules();
            loadModules();
            cacheStartTime = System.currentTimeMillis();
        }

        return components;
    }

    @Nullable
    public MagentoComponent getComponentForFile(@NotNull PsiFile psiFile) {
        for (MagentoComponent magentoComponent: this.getAllComponents()) {
            if (magentoComponent.isFileInContext(psiFile)) {
                return magentoComponent;
            }
        }

        return null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends MagentoComponent> T getComponentOfTypeForFile(@NotNull PsiFile psiFile, @NotNull Class<T> type) {
        for (MagentoComponent magentoComponent: this.getAllComponents()) {
            if (type.isInstance(magentoComponent) && magentoComponent.isFileInContext(psiFile)) {
                return (T)magentoComponent;
            }
        }

        return null;
    }

    synchronized public void flushModules() {
        components = new HashMap<>();
    }

    private void loadModules() {
        Collection<String> packages = FileBasedIndex.getInstance().getAllKeys(ModulePackageFileBasedIndex.NAME, this.project);
        PsiManager psiManager = PsiManager.getInstance(this.project);
        for (String packageName: packages) {
            if (components.containsKey(packageName)) {
                continue;
            }

            Collection<VirtualFile> containingFiles = FileBasedIndex.getInstance()
                .getContainingFiles(ModulePackageFileBasedIndex.NAME, packageName, GlobalSearchScope.allScope(this.project));

            if (containingFiles.size() > 0) {
                VirtualFile configurationFile = containingFiles.iterator().next();

                PsiFile psiFile = psiManager.findFile(configurationFile);
                if (psiFile != null && psiFile instanceof JsonFile) {
                    JsonObject jsonObject = PsiTreeUtil.getChildOfType((JsonFile) psiFile, JsonObject.class);
                    if (jsonObject == null) {
                        continue;
                    }

                    MagentoComponent magentoComponent;
                    ComposerPackageModel composerPackageModel = new ComposerPackageModelImpl(jsonObject);
                    if ("magento2-module".equals(composerPackageModel.getType())) {
                        magentoComponent = new MagentoModuleImpl(new ComposerPackageModelImpl(jsonObject), psiFile.getContainingDirectory());
                    } else {
                        magentoComponent = new MagentoComponentImp(new ComposerPackageModelImpl(jsonObject), psiFile.getContainingDirectory());
                    }

                    components.put(
                        packageName,
                        magentoComponent
                    );
                }
            }
        }
    }
}

/**
 * Created by dkvashnin on 12/5/15.
 */
class MagentoModuleImpl extends MagentoComponentImp implements MagentoModule {
    private static final String DEFAULT_MODULE_NAME = "Undefined module";
    private static final String CONFIGURATION_PATH = "etc";
    private String moduleName;

    public MagentoModuleImpl(@NotNull ComposerPackageModel composerPackageModel, @NotNull PsiDirectory directory) {
        super(composerPackageModel, directory);
    }

    @Override
    public String getMagentoName() {
        if (moduleName != null) {
            return moduleName;
        }

        PsiDirectory configurationDir = directory.findSubdirectory(CONFIGURATION_PATH);
        if (configurationDir != null) {
            PsiFile configurationFile = configurationDir.findFile("module.xml");

            if (configurationFile != null && configurationFile instanceof XmlFile) {
                XmlTag rootTag = ((XmlFile) configurationFile).getRootTag();
                if (rootTag != null) {
                    XmlTag module = rootTag.findFirstSubTag("module");
                    if (module != null && module.getAttributeValue("name") != null) {
                        moduleName = module.getAttributeValue("name");
                        return moduleName;
                    }
                }
            }
        }

        return DEFAULT_MODULE_NAME;
    }
}