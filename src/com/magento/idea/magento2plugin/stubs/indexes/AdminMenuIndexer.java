/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.stubs.indexes;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileBasedIndex.InputFilter;
import com.intellij.util.indexing.FileBasedIndexExtension;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.magento.idea.magento2plugin.magento.files.AdminMenuItems;
import com.magento.idea.magento2plugin.project.Settings;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class AdminMenuIndexer extends FileBasedIndexExtension<String, String> {
    public static final ID<String, String> KEY = ID.create(
            "com.magento.idea.magento2plugin.stubs.indexes.admin_menu"
    );
    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

    @NotNull
    @Override
    public ID<String, String> getName() {
        return KEY;
    }

    @NotNull
    @Override
    public DataIndexer<String, String, FileContent> getIndexer() {
        return fileContent -> {
            Map<String, String> map = new HashMap<>();
            PsiFile psiFile = fileContent.getPsiFile();

            if (!Settings.isEnabled(psiFile.getProject())) {
                return map;
            }

            if (!(psiFile instanceof XmlFile)) {
                return map;
            }

            XmlDocument document = ((XmlFile) psiFile).getDocument();
            if (document == null) {
                return map;
            }

            XmlTag[] xmlTags = PsiTreeUtil.getChildrenOfType(psiFile.getFirstChild(), XmlTag.class);
            if (xmlTags == null) {
                return map;
            }

            for (XmlTag xmlTag: xmlTags) {
                if (xmlTag.getName().equals(AdminMenuItems.CONFIG_TAG)) {
                    XmlTag menuTag = xmlTag.findFirstSubTag(AdminMenuItems.MENU_TAG);
                    assert menuTag != null;
                    for (XmlTag menuNode: menuTag.findSubTags(AdminMenuItems.ITEM_ADD_TAG)) {
                        String menuItemId = menuNode.getAttributeValue(AdminMenuItems.ID_ATTRIBUTE);
                        if (menuItemId != null) {
                            map.put(menuItemId, fileContent.getFile().getPath());
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
        return myKeyDescriptor;
    }

    @NotNull
    @Override
    public DataExternalizer<String> getValueExternalizer() {
        return new EnumeratorStringDescriptor();
    }

    @NotNull
    @Override
    public InputFilter getInputFilter() {
        return virtualFile -> (virtualFile.getFileType() == XmlFileType.INSTANCE
                && virtualFile.getNameWithoutExtension().equals(
                        AdminMenuItems.ADMIN_MENU_FILE_TAG)
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
