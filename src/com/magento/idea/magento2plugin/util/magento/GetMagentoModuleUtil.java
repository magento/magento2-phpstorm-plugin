/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SlowOperations;
import com.jetbrains.php.lang.psi.elements.ClassConstantReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.impl.ClassConstImpl;
import com.magento.idea.magento2plugin.magento.files.RegistrationPhp;
import com.magento.idea.magento2plugin.magento.packages.ComponentType;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.RegExUtil;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GetMagentoModuleUtil {

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
        final PsiFile registrationFile = getModuleRegistrationFile(psiDirectory, basePath);

        if (registrationFile == null) {
            return null;
        }
        final PsiDirectory moduleDir = registrationFile.getContainingDirectory();
        final PsiDirectory configDir = moduleDir.findSubdirectory(Package.moduleBaseAreaDir);
        final PsiDirectory viewDir = moduleDir.findSubdirectory(Package.moduleViewDir);
        final Collection<MethodReference> methodReferences = PsiTreeUtil.findChildrenOfType(
                registrationFile,
                MethodReference.class
        );

        for (final MethodReference methodReference : methodReferences) {
            if (!RegistrationPhp.REGISTER_METHOD_NAME.equals(methodReference.getName())) {
                continue;
            }
            final PsiElement[] parameters =  methodReference.getParameters();
            final PsiElement typeHolder = parameters[0];
            final PsiElement nameHolder = parameters[1];

            final String type = parseParameterValue(typeHolder);
            final String name = parseParameterValue(nameHolder);

            if (name == null || type == null) {
                return null;
            }
            final ComponentType resolvedType = ComponentType.getByValue(type);

            if (resolvedType == null) {
                return null;
            }

            return new MagentoModuleData(name, resolvedType, moduleDir, configDir, viewDir);
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

    private static PsiFile getModuleRegistrationFile(
            final @NotNull PsiDirectory directory,
            final @NotNull String basePath
    ) {
        if (basePath.equals(directory.getVirtualFile().getPath())) {
            return null;
        }
        final PsiFile registration = directory.findFile(RegistrationPhp.FILE_NAME);

        if (registration != null) {
            return registration;
        }
        final PsiDirectory parentDirectory = directory.getParentDirectory();

        if (parentDirectory == null) {
            return null;
        }

        return getModuleRegistrationFile(parentDirectory, basePath);
    }

    private static String parseParameterValue(final PsiElement valueHolder) {
        if (valueHolder instanceof ClassConstantReference) {
            final ClassConstantReference constantReference = (ClassConstantReference) valueHolder;
            final PsiElement resolved = SlowOperations.allowSlowOperations(
                    constantReference::resolve
            );

            if (!(resolved instanceof ClassConstImpl)) {
                return null;
            }
            final ClassConstImpl resolvedConstant = (ClassConstImpl) resolved;
            final PsiElement value = resolvedConstant.getDefaultValue();

            if (value == null) {
                return null;
            }

            return parseParameterValue(value);
        } else if (valueHolder instanceof StringLiteralExpression) {
            return ((StringLiteralExpression) valueHolder).getContents();
        }

        return null;
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
