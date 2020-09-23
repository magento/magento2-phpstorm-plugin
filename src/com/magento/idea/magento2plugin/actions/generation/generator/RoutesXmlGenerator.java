/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.GroupStatement;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormButtonData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.GetCodeTemplate;
import com.magento.idea.magento2plugin.actions.generation.util.CodeStyleSettings;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.FormButtonBlockPhp;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.MagentoPhpClass;
import com.magento.idea.magento2plugin.util.CamelCaseToHyphen;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.util.Properties;

public class RoutesXmlGenerator extends FileGenerator {
    private final UiComponentFormButtonData buttonData;
    private final Project project;
    private final GetFirstClassOfFile getFirstClassOfFile;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private final GetCodeTemplate getCodeTemplate;

    /**
     * Constructor.
     *
     * @param buttonData UiComponentFormButtonData
     * @param project Project
     */
    public RoutesXmlGenerator(
            final @NotNull UiComponentFormButtonData buttonData,
            final Project project
    ) {
        super(project);
        this.buttonData = buttonData;
        this.project = project;
        this.getFirstClassOfFile = GetFirstClassOfFile.getInstance();
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
        this.getCodeTemplate = GetCodeTemplate.getInstance(project);
    }

    /**
     * Creates a module UI form file.
     *
     * @param actionName String
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final String actionName) {
        return null;
    }

    @Override
    protected void fillAttributes(final Properties attributes) {
        attributes.setProperty("NAME", buttonData.getButtonClassName());
        attributes.setProperty("NAMESPACE", buttonData.getNamespace());
    }
}
