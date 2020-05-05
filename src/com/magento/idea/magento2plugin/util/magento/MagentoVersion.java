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
import com.magento.idea.magento2plugin.magento.packages.Package;

public final class MagentoVersion {

    public static final String DEFAULT_VERSION = "any";

    private MagentoVersion() {}

    /**
     * Parse composer.json to detect Magento 2 version
     *
     * @param project Project
     * @param magentoPath String
     * @return String
     */
    public static String get(final Project project, final String magentoPath) {
        final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(
                getFilePath(magentoPath)
        );

        if (file == null) {
            return DEFAULT_VERSION;
        }

        final PsiManager psiManager = PsiManager.getInstance(project);
        final PsiFile composerFile = psiManager.findFile(file);

        if (composerFile instanceof JsonFile) {
            final JsonFile composerJsonFile = (JsonFile) composerFile;
            final JsonObject jsonObject = PsiTreeUtil.getChildOfType(composerJsonFile, JsonObject.class);

            if (jsonObject == null) {
                return DEFAULT_VERSION;
            }

            final ComposerPackageModel composerObject = new ComposerPackageModelImpl(jsonObject);

            if (composerObject.getType() != null
                    && composerObject.getType().equals(Package.composerType)
                    && composerObject.getVersion() != null) {
                return composerObject.getVersion();
            }
        }

        return DEFAULT_VERSION;
    }

    private static String getFilePath(final String magentoPath) {
        return magentoPath + File.separator + ComposerJson.FILE_NAME;
    }
}
