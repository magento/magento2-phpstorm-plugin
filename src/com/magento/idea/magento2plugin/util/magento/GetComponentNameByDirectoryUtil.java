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
import org.jetbrains.annotations.Nullable;

public final class GetComponentNameByDirectoryUtil {

    private GetComponentNameByDirectoryUtil() {}

    /**
     * Returns component name.
     *
     * @param psiDirectory PsiDirectory
     * @param project Project
     * @return String
     */
    public static String execute(final PsiDirectory psiDirectory, final Project project) {
        final PhpFile registrationPhp = getRegistrationPhpRecursively(psiDirectory, project);
        if (registrationPhp == null) {
            return null;
        }
        final PsiElement[] childElements = registrationPhp.getChildren();
        if (childElements.length == 0) {
            return null;
        }
        return getName(childElements);
    }

    /**
     * Get module registration.php file.
     *
     * @param psiDirectory PsiDirectory
     * @param project Project
     * @return PhpFile
     */
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

    private static String getName(final PsiElement... childElements) {
        final MethodReference[] methods = parseRegistrationPhpElements(childElements);
        for (final MethodReference method: methods) {
            if (!method.getName().equals(RegistrationPhp.REGISTER_METHOD_NAME)) {
                continue;
            }
            final PsiElement[] parameters =  method.getParameters();
            for (final PsiElement parameter: parameters) {
                if (!(parameter instanceof StringLiteralExpression)) {
                    continue;
                }
                return ((StringLiteralExpression) parameter).getContents(); //NOPMD
            }
        }
        return null;
    }

    /**
     * Parse registration PHP.
     *
     * @param elements PsiElement...
     * @return MethodReference[]
     */
    public static MethodReference[] parseRegistrationPhpElements(//NOPMD
            final PsiElement... elements
    ) {
        for (final PsiElement element: elements) {
            MethodReference[] methods = PsiTreeUtil
                    .getChildrenOfType(element, MethodReference.class);
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

    @Nullable
    private static PhpFile getModuleRegistrationPhpFile(final PsiElement... containingFiles) {
        if (containingFiles.length != 0) {
            for (final PsiElement containingFile: containingFiles) {
                if (!(containingFile instanceof PhpFile)) {
                    continue;
                }
                final String filename = ((PhpFile) containingFile).getName();
                if (!filename.equals(RegistrationPhp.FILE_NAME)) {
                    continue;
                }
                return (PhpFile) containingFile; //NOPMD
            }
        }
        return null;
    }
}
