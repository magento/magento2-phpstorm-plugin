/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.stubs.indexes;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.magento.idea.magento2plugin.util.php.PhpPatternsHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/** Retains PHP dispatch discovery when the IDE PHP plugin is installed. */
public final class PhpEventNameIndexProvider implements EventNameIndexProvider {
    private static final String GENERATED_METADATA_PATH_SEGMENT = "/generated/metadata/";

    @Override
    public boolean accepts(final @NotNull VirtualFile file) {
        return file.getFileType() == PhpFileType.INSTANCE
                && !file.getPath().contains(GENERATED_METADATA_PATH_SEGMENT);
    }

    @Override
    public void collectEventNames(final @NotNull PsiFile file, final @NotNull Map<String, Void> names) {
        if (file instanceof PhpFile) {
            grabEventNamesFromPhpFile((PhpFile) file, names);
        }
    }

    private void grabEventNamesFromPhpFile(PhpFile file, Map<String, Void> map) {
        List<String> results = new ArrayList<>();
        recursiveFill(results, file);
        for (String result: results) {
            map.put(result, null);
        }
    }

    private void recursiveFill(List<String> results, PsiElement psiElement) {
        if (PhpPatternsHelper.STRING_METHOD_ARGUMENT.accepts(psiElement) && isInContextOfDispatchMethod(psiElement)) {
            String eventName= StringUtil.unquoteString(psiElement.getText());
            if (eventName.length() > 0) {
                results.add(eventName);
            }
            return;
        }
        for(PsiElement child: psiElement.getChildren()) {
            recursiveFill(results, child);
        }
    }

    private boolean isInContextOfDispatchMethod(PsiElement psiElement) {
        ParameterList parameterList = PsiTreeUtil.getParentOfType(psiElement, ParameterList.class);
        if (parameterList == null) {
            return false;
        }
        if (!(parameterList.getContext() instanceof MethodReference)) {
            return false;
        }
        MethodReference methodReference = (MethodReference)parameterList.getContext();
        return methodReference.getName() != null && methodReference.getName().equals("dispatch");
    }

}
