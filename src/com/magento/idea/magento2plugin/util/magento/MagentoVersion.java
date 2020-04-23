/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento;

import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.magento.idea.magento2plugin.magento.files.ComposerJson;
import com.magento.idea.magento2plugin.magento.packages.ComposerPackageModel;
import com.magento.idea.magento2plugin.magento.packages.ComposerPackageModelImpl;
import com.magento.idea.magento2plugin.magento.packages.File;

public class MagentoVersion {
    private static MagentoVersion INSTANCE = null;
    public static final String DEFAULT_VERSION = "any";

    public static MagentoVersion getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new MagentoVersion();
        }
        return INSTANCE;
    }

    public String get(Project project, String magentoPath) {
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(
            this.getFilePath(magentoPath)
        );

        if (file == null) {
            return DEFAULT_VERSION;
        }

        PsiManager psiManager = PsiManager.getInstance(project);
        PsiFile composerFile = psiManager.findFile(file);

        if (composerFile instanceof JsonFile) {
            JsonFile composerJsonFile = (JsonFile) composerFile;
            JsonObject jsonObject = PsiTreeUtil.getChildOfType(composerJsonFile, JsonObject.class);

            if (jsonObject == null) {
                return DEFAULT_VERSION;
            }

            ComposerPackageModel composerObject = new ComposerPackageModelImpl(jsonObject);

            if (composerObject.getType() != null && composerObject.getVersion() != null) {
               return composerObject.getVersion();
            }
        }

        return DEFAULT_VERSION;
    }

    private String getFilePath(String magentoPath) {
        return magentoPath + File.separator + ComposerJson.FILE_NAME;
    }
}
