/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.stubs.indexes;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.php.PhpPatternsHelper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dkvashnin on 11/5/15.
 */
public class EventNameIndex extends ScalarIndexExtension<String> {
    public static final ID<String, Void> KEY = ID.create("com.magento.idea.magento2plugin.stubs.indexes.event_name");
    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

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
                grabEventNamesFromPhpFile((PhpFile) psiFile, map);
            } else if (psiFile instanceof XmlFile) {
                grabEventNamesFromXmlFile((XmlFile) psiFile, map);
            }

            return map;
        };
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

    private void grabEventNamesFromXmlFile(XmlFile file, Map<String, Void> map) {
        XmlDocument xmlDocument = file.getDocument();
        if (xmlDocument != null) {
            XmlTag xmlRootTag = xmlDocument.getRootTag();
            if (xmlRootTag != null) {
                for (XmlTag eventTag : xmlRootTag.findSubTags("event")) {
                    String name = eventTag.getAttributeValue("name");
                    if (name != null && !name.isEmpty()) {
                        map.put(name, null);
                    }
                }
            }
        }
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return myKeyDescriptor;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return file -> (
                file.getFileType() == PhpFileType.INSTANCE
                    || (file.getFileType() == XmlFileType.INSTANCE && file.getName().equals("events.xml"))
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
