/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.xml;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.XmlSuppressableInspectionTool;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlToken;
import com.intellij.psi.xml.XmlTokenType;
import com.magento.idea.magento2uct.settings.UctSettingsService;
import com.magento.idea.magento2uct.versioning.VersionStateManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class ModuleConfigFileInspection extends XmlSuppressableInspectionTool {

    private final String[] supportedFiles = new String[]{
            "di.xml",
            "system.xml",
            "events.xml",
            "extension_attributes.xml",
            "webapi.xml",
            "communication.xml",
            "queue_consumer.xml",
            "crontab.xml",
            "indexer.xml",
            "mview.xml",
            "product_types.xml",
            "widget.xml",
    };

    @Override
    public @Nullable ProblemDescriptor[] checkFile(
            final @NotNull PsiFile file,
            final @NotNull InspectionManager manager,
            final boolean isOnTheFly
    ) {
        final Project project = file.getProject();
        final UctSettingsService settings = UctSettingsService.getInstance(project);

        if (!settings.isEnabled()) {
            return getEmptyResult();
        }

        if (Arrays.stream(supportedFiles).noneMatch(name -> name.equals(file.getName()))) {
            return getEmptyResult();
        }
        final List<IElementType> allowedTokenTypes = new ArrayList<>();
        allowedTokenTypes.add(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN);
        allowedTokenTypes.add(XmlTokenType.XML_DATA_CHARACTERS);

        final List<ProblemDescriptor> descriptors = new ArrayList<>();

        for (final XmlToken token : PsiTreeUtil.findChildrenOfType(file, XmlToken.class)) {
            if (!allowedTokenTypes.contains(token.getTokenType())) {
                continue;
            }
            final String fqn = token.getText().trim();

            if (!VersionStateManager.getInstance(project).isPresentInCodebase(fqn)) {
                continue;
            }
            // Inspection logic.
            doInspection(fqn, token, manager, isOnTheFly, descriptors);
        }

        return descriptors.toArray(new ProblemDescriptor[0]);
    }

    /**
     * Implement this method to specify inspection logic.
     *
     * @param fqn String
     * @param target PsiElement
     * @param manager InspectionManager
     * @param isOnTheFly boolean
     * @param descriptors List[ProblemDescriptor]
     */
    protected abstract void doInspection(
            final @NotNull String fqn,
            final @NotNull PsiElement target,
            final @NotNull InspectionManager manager,
            final boolean isOnTheFly,
            final @NotNull List<ProblemDescriptor> descriptors
    );

    /**
     * Retrieves an empty result.
     *
     * @return ProblemDescriptor[]
     */
    private ProblemDescriptor[] getEmptyResult() {
        return new ProblemDescriptor[0];
    }
}
