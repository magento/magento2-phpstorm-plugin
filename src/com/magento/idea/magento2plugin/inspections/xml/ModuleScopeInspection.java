/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.xml;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.magento.idea.magento2plugin.bundles.InspectionBundle;
import com.magento.idea.magento2plugin.magento.files.ModuleEventsXml;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.magento.idea.magento2plugin.magento.packages.ComponentType;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.GetMagentoModuleUtil;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public class ModuleScopeInspection extends PhpInspection {

    @NotNull
    @SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops", "PMD.CognitiveComplexity"})
    public PsiElementVisitor buildVisitor(
            final @NotNull ProblemsHolder problemsHolder,
            final boolean isOnTheFly
    ) {
        return new XmlElementVisitor() {
            private final HashMap<String, VirtualFile> loadedFileHash = new HashMap<>();//NOPMD
            private final InspectionBundle inspectionBundle = new InspectionBundle();
            private final ProblemHighlightType errorSeverity = ProblemHighlightType.WARNING;

            @Override
            public void visitFile(final @NotNull PsiFile file) {
                if (!ModuleEventsXml.FILE_NAME.equals(file.getName())) {
                    return;
                }

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

                if (moduleData == null || moduleData.getType() == null || !moduleData.getType().equals(ComponentType.module)) {
                    return;
                }

                final String directoryName = targetDirectory.getName();
                final Areas area = Areas.getAreaByString(targetDirectory.getName());
                if (area == null || directoryName.equals(Areas.base)) {
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
