/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.magento.idea.magento2plugin.magento.files.RegistrationPhp;
import com.magento.idea.magento2plugin.util.RegExUtil;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GetModuleNameByDirectoryUtil {

    private GetModuleNameByDirectoryUtil() {}

    /**
     * Provides module name if directory belongs to one.
     *
     * @param psiDirectory PsiDirectory
     * @param project Project
     *
     * @return String
     */
    public static @Nullable String execute(
            final @NotNull PsiDirectory psiDirectory,
            final @NotNull Project project
    ) {
        // Check if directory is theme directory and return module name from directory path if yes
        final String path = psiDirectory.getVirtualFile().getPath();
        final Pattern pattern = Pattern.compile(RegExUtil.CustomTheme.MODULE_NAME);
        final Matcher matcher = pattern.matcher(path);

        while (matcher.find()) {
            final String moduleNamePath =  matcher.group(0);

            if (!moduleNamePath.isEmpty()) {
                final String[] moduleNamePathParts = moduleNamePath.split("/");

                if (moduleNamePathParts.length >= 6) { //NOPMD
                    return moduleNamePath.split("/")[5];
                }
            }
        }

        final PhpFile registrationPhp = getRegistrationPhpRecursively(
                psiDirectory,
                project
        );

        if (registrationPhp == null) {
            return null;
        }
        final PsiElement[] childElements = registrationPhp.getChildren();

        if (childElements.length == 0) {
            return null;
        }

        return getModuleName(childElements);
    }

    private static MethodReference[] parseRegistrationPhpElements(
            final PsiElement... elements
    ) {
        for (final PsiElement element : elements) {
            MethodReference[] methods = PsiTreeUtil.getChildrenOfType(
                element,
                MethodReference.class
            );

            if (methods == null) {
                final PsiElement[] children = element.getChildren();
                methods = parseRegistrationPhpElements(children);
            }

            if (methods.length > 0) {
                return methods;
            }
        }

        return new MethodReference[0];
    }

    private static PhpFile getRegistrationPhpRecursively(
            final PsiDirectory psiDirectory,
            final Project project
    ) {
        final PsiElement[] containingFiles = psiDirectory.getChildren();
        final PhpFile containingFile = getModuleRegistrationPhpFile(containingFiles);
        if (containingFile != null) {
            return containingFile;
        }
        final String basePath = project.getBasePath();
        if (psiDirectory.getVirtualFile().getPath().equals(basePath)) {
            return null;
        }
        final PsiDirectory getParent = psiDirectory.getParent();
        if (getParent != null) {
            return getRegistrationPhpRecursively(getParent, project);
        }
        return null;
    }

    private static String getModuleName(final PsiElement... childElements) {
        final MethodReference[] methods = parseRegistrationPhpElements(childElements);

        if (methods.length == 0) {
            return null;
        }

        for (final MethodReference method: methods) {
            if (!RegistrationPhp.REGISTER_METHOD_NAME.equals(method.getName())) {
                continue;
            }
            final PsiElement[] parameters =  method.getParameters();

            for (final PsiElement parameter: parameters) {
                if (!(parameter instanceof StringLiteralExpression)) {
                    continue;
                }
                final String moduleName = ((StringLiteralExpression) parameter)
                        .getContents();

                if (moduleName.matches(RegExUtil.Magento.MODULE_NAME)) {
                    return moduleName;
                }
            }
        }

        return null;
    }

    @Nullable
    private static PhpFile getModuleRegistrationPhpFile(
            final PsiElement... containingFiles
    ) {
        if (containingFiles.length != 0) {
            for (final PsiElement containingFile: containingFiles) {
                if (!(containingFile instanceof PhpFile)) {
                    continue;
                }
                final String filename = ((PhpFile) containingFile).getName();

                if (RegistrationPhp.FILE_NAME.equals(filename)) {
                    return (PhpFile) containingFile;
                }
            }
        }

        return null;
    }
}
