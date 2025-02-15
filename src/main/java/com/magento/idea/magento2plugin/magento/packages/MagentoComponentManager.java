/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages;

import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonObject;
import com.intellij.openapi.components.Service;
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
import com.magento.idea.magento2plugin.stubs.indexes.ModulePackageIndex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Service
public final class MagentoComponentManager {

    private Map<String, MagentoComponent> components = new HashMap<>();
    private long cacheStartTime;
    private static final int CACHE_LIFE_TIME = 20000;//NOPMD
    private final Project project;

    public MagentoComponentManager(final Project project) {
        this.project = project;
    }

    public static MagentoComponentManager getInstance(final @NotNull Project project) {
        return project.getService(MagentoComponentManager.class);
    }

    public Collection<MagentoComponent> getAllComponents() {
        return getComponents().values();
    }

    /**
     * Get all components of the specified type.
     *
     * @param type Class
     *
     * @return Collection
     */
    @SuppressWarnings("unchecked")
    public <T extends MagentoComponent> Collection<T> getAllComponentsOfType(
            final @NotNull Class<T> type
    ) {
        final Collection<T> result = new ArrayList<>();
        final Map<String, MagentoComponent> components = getComponents();

        for (final String key: components.keySet()) {
            if (type.isInstance(components.get(key))) {
                result.add((T)components.get(key));
            }
        }

        return result;
    }

    @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
    private synchronized Map<String, MagentoComponent> getComponents() {
        if (project.isDisposed() || DumbService.getInstance(project).isDumb()) {
            return new HashMap<>();
        }

        if (cacheStartTime + CACHE_LIFE_TIME < System.currentTimeMillis()) {
            flushModules();
            loadModules();
            cacheStartTime = System.currentTimeMillis();
        }

        return components;
    }

    /**
     * Get component for the specified file.
     *
     * @param psiFile PsiFile
     *
     * @return MagentoComponent
     */
    @Nullable
    public MagentoComponent getComponentForFile(final @NotNull PsiFile psiFile) {
        for (final MagentoComponent magentoComponent: this.getAllComponents()) {
            if (magentoComponent.isFileInContext(psiFile)) {
                return magentoComponent;
            }
        }

        return null;
    }

    /**
     * Get component of type for the specified file.
     *
     * @param psiFile PsiFile
     * @param type Class
     *
     * @return T
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends MagentoComponent> T getComponentOfTypeForFile(
            final @NotNull PsiFile psiFile,
            final @NotNull Class<T> type
    ) {
        for (final MagentoComponent magentoComponent: this.getAllComponents()) {
            if (type.isInstance(magentoComponent) && magentoComponent.isFileInContext(psiFile)) {
                return (T)magentoComponent;
            }
        }

        return null;
    }

    @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
    public synchronized void flushModules() {
        components = new HashMap<>();
    }

    @SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops", "PMD.AvoidDeeplyNestedIfStmts"})
    private void loadModules() {
        final Collection<String> packages = FileBasedIndex
                .getInstance()
                .getAllKeys(ModulePackageIndex.KEY, this.project);
        final PsiManager psiManager = PsiManager.getInstance(this.project);

        for (final String packageName: packages) {
            if (components.containsKey(packageName)) {
                continue;
            }

            final Collection<VirtualFile> containingFiles = FileBasedIndex
                    .getInstance()
                    .getContainingFiles(
                            ModulePackageIndex.KEY,
                            packageName,
                            GlobalSearchScope.allScope(this.project)
                    );

            if (!containingFiles.isEmpty()) {
                final VirtualFile configurationFile = containingFiles.iterator().next();
                final PsiFile psiFile = psiManager.findFile(configurationFile);

                if (psiFile instanceof JsonFile) {
                    final JsonObject jsonObject = PsiTreeUtil
                            .getChildOfType((JsonFile) psiFile, JsonObject.class);
                    if (jsonObject == null) {
                        continue;
                    }

                    MagentoComponent magentoComponent;
                    final ComposerPackageModel composerPackageModel = new ComposerPackageModelImpl(
                            jsonObject
                    );

                    if ("magento2-module".equals(composerPackageModel.getType())) {
                        magentoComponent = new MagentoModuleImpl(
                                new ComposerPackageModelImpl(jsonObject),
                                psiFile.getContainingDirectory()
                        );
                    } else {
                        magentoComponent = new MagentoComponentImp(
                                new ComposerPackageModelImpl(jsonObject),
                                psiFile.getContainingDirectory()
                        );
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

@SuppressWarnings("checkstyle:OneTopLevelClass")
class MagentoModuleImpl extends MagentoComponentImp implements MagentoModule {
    private static final String DEFAULT_MODULE_NAME = "Undefined module";
    private static final String CONFIGURATION_PATH = "etc";
    private String moduleName;

    public MagentoModuleImpl(
            final @NotNull ComposerPackageModel composerPackageModel,
            final @NotNull PsiDirectory directory
    ) {
        super(composerPackageModel, directory);
    }

    @SuppressWarnings({"PMD.AvoidDeeplyNestedIfStmts"})
    @Override
    public String getMagentoName() {
        if (moduleName != null) {
            return moduleName;
        }

        final PsiDirectory configurationDir = directory.findSubdirectory(CONFIGURATION_PATH);
        if (configurationDir != null) {
            final PsiFile configurationFile = configurationDir.findFile("module.xml");

            if (configurationFile instanceof XmlFile) {
                final XmlTag rootTag = ((XmlFile) configurationFile).getRootTag();
                if (rootTag != null) {
                    final XmlTag module = rootTag.findFirstSubTag("module");
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
