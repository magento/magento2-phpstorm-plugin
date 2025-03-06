/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.decorator;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.openapi.project.Project;
 import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.util.magento.MagentoPathUrlUtil;

public class MagentoFolderDecorator implements ProjectViewNodeDecorator {
    @Override
    public void decorate(
            final ProjectViewNode<?> projectViewNode,
            final PresentationData presentationData
    ) {
        final Project project = projectViewNode.getProject();
        if (project == null) {
            return;
        }
        final Settings settings = Settings.getInstance(project);

        final Object value = projectViewNode.getValue();
        if (value instanceof PsiDirectory virtualFile) {
            final String directoryUrl = virtualFile.getVirtualFile().getUrl();
            final String magentoPathUrl = MagentoPathUrlUtil.execute(project);
            if (settings.containsMagentoFolder(directoryUrl) ||
                directoryUrl.equals(magentoPathUrl)) {
                presentationData.setIcon(MagentoIcons.MARK_AS);
            }
        }
    }
}

