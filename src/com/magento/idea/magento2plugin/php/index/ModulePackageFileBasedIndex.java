package com.magento.idea.magento2plugin.php.index;

import com.intellij.json.JsonFileType;
import com.intellij.json.psi.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.magento.idea.magento2plugin.Settings;
import com.magento.idea.magento2plugin.php.module.ComposerPackageModel;
import com.magento.idea.magento2plugin.php.module.ComposerPackageModelImpl;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dkvashnin on 12/3/15.
 */
public class ModulePackageFileBasedIndex extends ScalarIndexExtension<String> {
    public static final ID<String, Void> NAME = ID.create("com.magento.idea.magento2plugin.php.index.module");

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return NAME;
    }

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return new DataIndexer<String, Void, FileContent>() {
            @NotNull
            @Override
            public Map<String, Void> map(@NotNull FileContent fileContent) {
                Map<String, Void> map = new HashMap<>();
                JsonFile jsonFile = (JsonFile)fileContent.getPsiFile();
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
            }
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
        return new FileBasedIndex.InputFilter() {
            @Override
            public boolean acceptInput(@NotNull VirtualFile virtualFile) {
                return virtualFile.getFileType().equals(JsonFileType.INSTANCE) && virtualFile.getName().equals("composer.json");
            }
        };
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
