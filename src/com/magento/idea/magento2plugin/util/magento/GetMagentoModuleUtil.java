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
import com.jetbrains.php.lang.psi.elements.ClassConstantReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.impl.ClassConstImpl;
import com.magento.idea.magento2plugin.magento.files.RegistrationPhp;
import com.magento.idea.magento2plugin.magento.packages.ComponentType;
import java.util.Collection;

import com.magento.idea.magento2plugin.magento.packages.Package;
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
        final PsiDirectory configDir = registrationFile
                .getContainingDirectory()
                .findSubdirectory(Package.moduleBaseAreaDir);
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

            return new MagentoModuleData(name, resolvedType, configDir);
        }

        return null;
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
            final PsiElement resolved = ((ClassConstantReference) valueHolder).resolve();

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
        private final PsiDirectory configDir;

        /**
         * Default constructor.
         *
         * @param name String
         * @param type ComponentType
         */
        public MagentoModuleData(
                final @NotNull String name,
                final @NotNull ComponentType type
        ) {
            this(name, type, null);
        }

        /**
         * Constructor with a config directory specified.
         *
         * @param name String
         * @param type ComponentType
         * @param configDir PsiDirectory
         */
        public MagentoModuleData(
                final @NotNull String name,
                final @NotNull ComponentType type,
                final @Nullable PsiDirectory configDir
        ) {
            this.name = name;
            this.type = type;
            this.configDir = configDir;
        }

        public String getName() {
            return name;
        }

        public ComponentType getType() {
            return type;
        }

        public @Nullable PsiDirectory getConfigDir() {
            return configDir;
        }
    }
}
