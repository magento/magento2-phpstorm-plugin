package com.magento.idea.magento2plugin.stubs.indexes;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.magento.idea.magento2plugin.project.Settings;
import org.jetbrains.annotations.NotNull;


import java.util.HashMap;
import java.util.Map;

public class ModuleNameIndex extends ScalarIndexExtension<String> {
    public static final ID<String, Void> KEY =
            ID.create("com.magento.idea.magento2plugin.stubs.indexes.module_name");

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return KEY;
    }

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return inputData -> {
            Map<String, Void> map = new HashMap<>();
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
                            if (constantName != null && constantName.equalsIgnoreCase("module")) {
                                PsiElement moduleName = ((ClassConstantReference) firstParameter).getNextPsiSibling();
                                if (moduleName != null && moduleName instanceof StringLiteralExpression) {
                                    map.put(StringUtil.unquoteString(moduleName.getText()), null);
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
