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
import org.jetbrains.annotations.Nullable;

public final class GetModuleNameByDirectoryUtil {
    public static final int THEME_SPLIT_COUNT = 1;
    public static final String THEME_DIRECTORY_REGEX = "app\\/design\\/(adminhtml|frontend)\\/";

    private GetModuleNameByDirectoryUtil() {}

    /**
     * Provides module name if directory belongs to one.
     *
     * @param psiDirectory PsiDirectory
     * @param project Project
     * @return String
     */
    public static String execute(
            final PsiDirectory psiDirectory,
            final Project project
    ) {
        // Check if directory is theme directory and return module name from directory path if yes
        final String[] splits = psiDirectory.getVirtualFile().getPath()
                .split(THEME_DIRECTORY_REGEX);
        if (splits.length > THEME_SPLIT_COUNT) {
            return splits[1].split("\\/")[2];
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

    private static MethodReference[] parseRegistrationPhpElements(//NOPMD
            final PsiElement... elements
    ) {
        for (final PsiElement element: elements) {
            MethodReference[] methods = PsiTreeUtil.getChildrenOfType(
                element,
                MethodReference.class
            );
            if (methods == null) {
                final PsiElement[] children = element.getChildren();
                methods = parseRegistrationPhpElements(children);
            }
            if (methods != null) {
                return methods;
            }
        }
        return null;
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
        if (methods == null) {
            return null;
        }
        for (final MethodReference method: methods) {
            if (!method.getName().equals(RegistrationPhp.REGISTER_METHOD_NAME)) {
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
                if (filename.equals(RegistrationPhp.FILE_NAME)) {
                    return (PhpFile) containingFile;
                }
            }
        }
        return null;
    }
}
