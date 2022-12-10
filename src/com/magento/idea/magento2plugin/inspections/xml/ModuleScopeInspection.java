/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.xml;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.XmlSuppressableInspectionTool;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlElementVisitor;
import com.magento.idea.magento2plugin.bundles.InspectionBundle;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.ComponentType;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.GetMagentoModuleUtil;
import org.jetbrains.annotations.NotNull;

public class ModuleScopeInspection extends XmlSuppressableInspectionTool {

    /**
     * Inspection for the module config area.
     */
    @NotNull
    @SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops", "PMD.CognitiveComplexity"})
    public PsiElementVisitor buildVisitor(
            final @NotNull ProblemsHolder problemsHolder,
            final boolean isOnTheFly
    ) {
        return new XmlElementVisitor() {
            private final InspectionBundle inspectionBundle = new InspectionBundle();
            private final ProblemHighlightType errorSeverity = ProblemHighlightType.WARNING;

            @Override
            public void visitFile(final @NotNull PsiFile file) {
                final PsiDirectory targetDirectory = file.getParent();
                if (targetDirectory == null) {
                    return;
                }

                final PsiDirectory parentDirectory = targetDirectory.getParent();
                if (parentDirectory == null) {
                    return;
                }

                if (!parentDirectory.getName().equals(Package.moduleBaseAreaDir)) {
                    return;
                }

                final GetMagentoModuleUtil.MagentoModuleData moduleData = GetMagentoModuleUtil
                        .getByContext(targetDirectory, file.getProject());

                if (moduleData == null || moduleData.getType() == null
                        || !moduleData.getType().equals(ComponentType.module)) {
                    return;
                }

                final String directoryName = targetDirectory.getName();
                final Areas area = Areas.getAreaByString(targetDirectory.getName());
                if (area == null || directoryName.equals(Areas.base.toString())) {
                    problemsHolder.registerProblem(
                            file,
                            inspectionBundle.message(
                                    "inspection.config.wrong.area"
                            ),
                            errorSeverity
                    );
                }
            }
        };
    }
}
