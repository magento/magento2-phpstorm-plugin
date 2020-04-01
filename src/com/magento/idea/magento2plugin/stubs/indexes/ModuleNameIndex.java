/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.stubs.indexes;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.magento.idea.magento2plugin.project.Settings;
import org.jetbrains.annotations.NotNull;


import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModuleNameIndex extends FileBasedIndexExtension<String, String> {
    public static final ID<String, String> KEY =
            ID.create("com.magento.idea.magento2plugin.stubs.indexes.module_name");

    @NotNull
    @Override
    public ID<String, String> getName() {
        return KEY;
    }

    @NotNull
    @Override
    public DataIndexer<String, String, FileContent> getIndexer() {
        return inputData -> {
            Map<String, String> map = new HashMap<>();
            PsiFile psiFile = inputData.getPsiFile();

            if (!Settings.isEnabled(psiFile.getProject())) {
                return map;
            }

            if (psiFile instanceof PhpFile) {
                MethodReference  method = PsiTreeUtil.findChildOfAnyType(psiFile, MethodReference.class);
                if (method != null) {
                    ParameterList parameterList = method.getParameterList();
                    if (parameterList != null) {
                        PsiElement firstParameter = parameterList.getFirstPsiChild();
                        if (firstParameter != null && firstParameter instanceof ClassConstantReference) {
                            String constantName = ((ClassConstantReference) firstParameter).getName();
                            if (constantName != null) {
                                PsiElement moduleNameEl = ((ClassConstantReference) firstParameter).getNextPsiSibling();
                                if (moduleNameEl != null && moduleNameEl instanceof StringLiteralExpression) {
                                    String moduleName = StringUtil.unquoteString(moduleNameEl.getText());

                                    PsiElement modulePathEl =
                                            ((StringLiteralExpression) moduleNameEl).getNextPsiSibling();
                                    if (modulePathEl != null) {
                                        Pattern pattern = Pattern.compile(
                                                "__DIR__(\\s*[.,]\\s*[\\'\\\"]((/[\\w-]+)+)/?[\\'\\\"])?"
                                        );
                                        Matcher matcher = pattern.matcher(modulePathEl.getText());
                                        if (matcher.find()) {
                                            String modulePath = matcher.group(2);
                                            map.put(
                                                    moduleName,
                                                    modulePath == null ? "" : modulePath
                                            );
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return map;
        };
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return new EnumeratorStringDescriptor();
    }

    @NotNull
    public DataExternalizer<String> getValueExternalizer() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return virtualFile -> (
                virtualFile.getFileType().equals(PhpFileType.INSTANCE)
                        && virtualFile.getName().equals("registration.php")
        );
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return 1;
    }
}
