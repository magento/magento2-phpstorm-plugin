/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.inspections.xml;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.XmlSuppressableInspectionTool;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
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
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class ModuleConfigFileInspection extends XmlSuppressableInspectionTool {

    private static final String LAYOUT_FILE_REGEX = "\\/(layout|ui_component)\\/.*\\.xml";
    private static final Pattern LAYOUT_FILE_PATTERN = Pattern.compile(LAYOUT_FILE_REGEX);

    private final String[] supportedFiles = {
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
            "queue.xml",
            "product_options.xml",
            "export.xml",
            "import.xml",
            "analytics.xml",
            "reports.xml",
            "pdf.xml",
            "cache.xml",
            "validation.xml"
    };
    private ProblemsHolder problemsHolder;

    @Override
    public @NotNull PsiElementVisitor buildVisitor(
            final @NotNull ProblemsHolder holder,
            final boolean isOnTheFly
    ) {
        problemsHolder = holder;

        return super.buildVisitor(holder, isOnTheFly);
    }

    @Override
    public @Nullable ProblemDescriptor[] checkFile(
            final @NotNull PsiFile file,
            final @NotNull InspectionManager manager,
            final boolean isOnTheFly
    ) {
        final Project project = file.getProject();
        final UctSettingsService settings = UctSettingsService.getInstance(project);
        final ProblemsHolder holder = getProblemsHolder();

        if (!settings.isEnabled() || holder == null) {
            return getEmptyResult();
        }

        if (Arrays.stream(supportedFiles).noneMatch(name -> name.equals(file.getName()))
                && !isLayoutFile(file)) {
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
            doInspection(fqn, token, manager, holder, isOnTheFly, descriptors);
        }

        return descriptors.toArray(new ProblemDescriptor[0]);
    }

    /**
     * Implement this method to specify inspection logic.
     *
     * @param fqn String
     * @param target PsiElement
     * @param manager InspectionManager
     * @param holder ProblemsHolder
     * @param isOnTheFly boolean
     * @param descriptors List[ProblemDescriptor]
     */
    protected abstract void doInspection(
            final @NotNull String fqn,
            final @NotNull PsiElement target,
            final @NotNull InspectionManager manager,
            final @NotNull ProblemsHolder holder,
            final boolean isOnTheFly,
            final @NotNull List<ProblemDescriptor> descriptors
    );

    private @Nullable ProblemsHolder getProblemsHolder() {
        return problemsHolder;
    }

    /**
     * Retrieves an empty result.
     *
     * @return ProblemDescriptor[]
     */
    private ProblemDescriptor[] getEmptyResult() {
        return new ProblemDescriptor[0];
    }

    private boolean isLayoutFile(final @NotNull PsiFile file) {
        final VirtualFile virtualFile = file.getVirtualFile();

        if (virtualFile == null) {
            return false;
        }

        return LAYOUT_FILE_PATTERN.matcher(virtualFile.getPath()).find();
    }
}
