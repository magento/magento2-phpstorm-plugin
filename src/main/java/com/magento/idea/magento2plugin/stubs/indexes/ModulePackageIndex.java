/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.stubs.indexes;

import com.intellij.json.JsonFileType;
import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonObject;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.magento.packages.ComposerPackageModel;
import com.magento.idea.magento2plugin.magento.packages.ComposerPackageModelImpl;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dkvashnin on 12/3/15.
 */
public class ModulePackageIndex extends ScalarIndexExtension<String> {
    public static final ID<String, Void> KEY =
            ID.create("com.magento.idea.magento2plugin.stubs.indexes.module_package");

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
            JsonFile jsonFile = (JsonFile)inputData.getPsiFile();
            if (!Settings.isEnabled(jsonFile.getProject())) {
                return map;
            }

            JsonObject jsonObject = PsiTreeUtil.getChildOfType(jsonFile, JsonObject.class);
            if (jsonObject == null) {
                return map;
            }
            ComposerPackageModel composerObject = new ComposerPackageModelImpl(jsonObject);

            String type = composerObject.getType();
            if (type == null) {
                return map;
            }

            if (!type.startsWith("magento2-")) {
                return map;
            }

            String name = composerObject.getName();

            if (name != null) {
                map.put(name, null);
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
            virtualFile.getFileType().equals(JsonFileType.INSTANCE) && virtualFile.getName().equals("composer.json")
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
