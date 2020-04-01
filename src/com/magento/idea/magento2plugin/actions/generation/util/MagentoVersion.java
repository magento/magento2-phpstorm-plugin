/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.util;

import com.intellij.ide.DataManager;
import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonObject;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.magento.idea.magento2plugin.php.module.ComposerPackageModel;
import com.magento.idea.magento2plugin.php.module.ComposerPackageModelImpl;

public class MagentoVersion {
    private static MagentoVersion INSTANCE = null;
    private String version = "unknown";
    private Project project;

    public static MagentoVersion getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new MagentoVersion();
        }
        return INSTANCE;
    }

    public String get() {
        DataContext dataContext = DataManager.getInstance().getDataContext();
        this.project = (Project) dataContext.getData(DataConstants.PROJECT);

        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(this.getFilePath());

        if (file == null) {
            return version;
        }

        PsiManager psiManager = PsiManager.getInstance(this.project);
        PsiFile composerFile = psiManager.findFile(file);

        if (composerFile instanceof JsonFile) {
            JsonFile composerJsonFile = (JsonFile) composerFile;
            JsonObject jsonObject = PsiTreeUtil.getChildOfType(composerJsonFile, JsonObject.class);

            if (jsonObject == null) {
                return version;
            }

            ComposerPackageModel composerObject = new ComposerPackageModelImpl(jsonObject);

            if (composerObject.getType() != null) {
                version = composerObject.getVersion();
            }
        }

        return version;
    }

    private String getFilePath() {
        String fileName = "composer.json";
        return this.project.getBasePath() + "/" + fileName;
    }
}
