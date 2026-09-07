/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.DumbService;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.magento.packages.ComponentType;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.stubs.indexes.xml.ThemeXmlIndex;
import com.magento.idea.magento2plugin.util.RegExUtil;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GetMagentoModuleUtil {

    private static final String MODULE_FILE_NAME = "module.xml";
    private static final String THEME_FILE_NAME = "theme.xml";

    private GetMagentoModuleUtil() {}

    /**
     * Get module component by context.
     *
     * @param psiDirectory PsiDirectory
     * @param project Project
     *
     * @return MagentoModuleData
     */
    @SuppressWarnings("PMD.AvoidBranchingStatementAsLastInLoop")
    public static MagentoModuleData getByContext(
            final @NotNull PsiDirectory psiDirectory,
            final @NotNull Project project
    ) {
        final String basePath = project.getBasePath();

        if (basePath == null) {
            return null;
        }
        PsiDirectory contextDirectory = psiDirectory;

        while (!basePath.equals(contextDirectory.getVirtualFile().getPath())) {
            final MagentoModuleData moduleData = getModuleData(contextDirectory);

            if (moduleData != null) {
                return moduleData;
            }
            final MagentoModuleData themeData = getThemeData(contextDirectory, project);

            if (themeData != null) {
                return themeData;
            }
            contextDirectory = contextDirectory.getParentDirectory();

            if (contextDirectory == null) {
                return null;
            }
        }

        return null;
    }

    /**
     * Check if specified module is in the app/code directory.
     *
     * @param moduleData MagentoModuleData
     *
     * @return boolean
     */
    public static boolean isEditableModule(final @NotNull MagentoModuleData moduleData) {
        final Pattern pattern = Pattern.compile(RegExUtil.Magento.CUSTOM_VENDOR_NAME);
        final Matcher matcher = pattern.matcher(
                moduleData.getModuleDir().getVirtualFile().getPath()
        );

        return matcher.find();
    }

    /**
     * Check if specified directory is in the app/code.
     *
     * @param directory PsiDirectory
     *
     * @return boolean
     */
    public static boolean isDirectoryInEditableModule(final @NotNull PsiDirectory directory) {
        final Pattern pattern = Pattern.compile(RegExUtil.Magento.CUSTOM_VENDOR_NAME);
        final Matcher matcher = pattern.matcher(directory.getVirtualFile().getPath());

        return matcher.find();
    }

    private static @Nullable MagentoModuleData getModuleData(
            final @NotNull PsiDirectory directory
    ) {
        final PsiDirectory configDirectory = directory.findSubdirectory(
                Package.moduleBaseAreaDir
        );

        if (configDirectory == null) {
            return null;
        }
        final PsiFile moduleFile = configDirectory.findFile(MODULE_FILE_NAME);

        if (!(moduleFile instanceof XmlFile)) {
            return null;
        }
        final XmlTag rootTag = ((XmlFile) moduleFile).getRootTag();
        final XmlTag moduleTag = rootTag == null ? null : rootTag.findFirstSubTag("module");
        final String moduleName = moduleTag == null ? null : moduleTag.getAttributeValue("name");

        if (moduleName == null) {
            return null;
        }

        return new MagentoModuleData(
                moduleName,
                ComponentType.module,
                directory,
                configDirectory,
                directory.findSubdirectory(Package.moduleViewDir)
        );
    }

    private static @Nullable MagentoModuleData getThemeData(
            final @NotNull PsiDirectory directory,
            final @NotNull Project project
    ) {
        if (DumbService.isDumb(project)) {
            return null;
        }
        final PsiFile themeFile = directory.findFile(THEME_FILE_NAME);

        if (!(themeFile instanceof XmlFile) || themeFile.getVirtualFile() == null) {
            return null;
        }
        final Map<String, String> themeData = FileBasedIndex.getInstance().getFileData(
                ThemeXmlIndex.KEY,
                themeFile.getVirtualFile(),
                project
        );

        if (themeData.isEmpty()) {
            return null;
        }

        return new MagentoModuleData(
                themeData.keySet().iterator().next(),
                ComponentType.theme,
                directory,
                null,
                null
        );
    }

    public static class MagentoModuleData {

        private final String name;
        private final ComponentType type;
        private final PsiDirectory moduleDir;
        private final PsiDirectory configDir;
        private final PsiDirectory viewDir;

        /**
         * Default constructor.
         *
         * @param name String
         * @param type ComponentType
         * @param moduleDir PsiDirectory
         */
        public MagentoModuleData(
                final @NotNull String name,
                final @NotNull ComponentType type,
                final @NotNull PsiDirectory moduleDir
        ) {
            this(name, type, moduleDir, null, null);
        }

        /**
         * Constructor with a config directory specified.
         *
         * @param name String
         * @param type ComponentType
         * @param moduleDir PsiDirectory
         * @param configDir PsiDirectory
         * @param viewDir PsiDirectory
         */
        public MagentoModuleData(
                final @NotNull String name,
                final @NotNull ComponentType type,
                final @NotNull PsiDirectory moduleDir,
                final @Nullable PsiDirectory configDir,
                final @Nullable PsiDirectory viewDir
        ) {
            this.name = name;
            this.type = type;
            this.moduleDir = moduleDir;
            this.configDir = configDir;
            this.viewDir = viewDir;
        }

        public String getName() {
            return name;
        }

        public ComponentType getType() {
            return type;
        }

        public PsiDirectory getModuleDir() {
            return moduleDir;
        }

        public @Nullable PsiDirectory getConfigDir() {
            return configDir;
        }

        public @Nullable PsiDirectory getViewDir() {
            return viewDir;
        }
    }
}
