/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormButtonData;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormFileData;
import com.magento.idea.magento2plugin.actions.generation.generator.code.XmlDeclarationsGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassTypesBuilder;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.UiComponentDataProviderFile;
import com.magento.idea.magento2plugin.magento.files.UiComponentFormXmlFile;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.FileBasedIndexUtil;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class UiComponentFormGenerator extends FileGenerator {

    private final UiComponentFormFileData data;
    private final Project project;

    /**
     * Ui Component form generator constructor.
     *
     * @param data UiFormFileData
     * @param project Project
     */
    public UiComponentFormGenerator(
            final @NotNull UiComponentFormFileData data,
            final Project project
    ) {
        super(project);
        this.data = data;
        this.project = project;
    }

    /**
     * Creates a module UI form file.
     *
     * @param actionName String
     *
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final String actionName) {
        final PsiFile formFile = createForm(
                actionName
        );
        generateButtonClasses(actionName);

        return formFile;
    }

    /**
     * Generate buttons classes.
     *
     * @param actionName String
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    protected void generateButtonClasses(final @NotNull String actionName) {
        for (final UiComponentFormButtonData buttonData : data.getButtons()) {
            new UiComponentFormButtonBlockGenerator(
                    buttonData,
                    project,
                    data.getEntityName(),
                    data.getEntityId()
            ).generate(actionName);
        }
    }

    /**
     * Finds or creates form.
     *
     * @param actionName String
     *
     * @return PsiFile
     */
    protected PsiFile createForm(
            final String actionName
    ) {
        final String moduleName = data.getModuleName();
        final PsiDirectory parentDirectory = new ModuleIndex(project)
                .getModuleDirectoryByModuleName(moduleName);

        if (parentDirectory == null) {
            return null;
        }
        final DirectoryGenerator directoryGenerator = DirectoryGenerator.getInstance();
        final FileFromTemplateGenerator fileFromTemplateGenerator =
                new FileFromTemplateGenerator(project);
        final ArrayList<String> fileDirectories = new ArrayList<>();
        fileDirectories.add(Package.moduleViewDir);
        final String area = data.getFormArea();
        fileDirectories.add(getArea(area).toString());
        fileDirectories.add(Package.moduleViewUiComponentDir);

        final PsiDirectory formDirectory =
                directoryGenerator.findOrCreateSubdirectories(
                        parentDirectory,
                        fileDirectories.stream().collect(Collectors.joining(File.separator))
                );

        final String formName = data.getFormName();
        final UiComponentFormXmlFile uiComponentFormXml = new UiComponentFormXmlFile(formName);
        XmlFile formFile = (XmlFile) FileBasedIndexUtil.findModuleViewFile(
                uiComponentFormXml.getFileName(),
                getArea(area),
                moduleName,
                project,
                Package.moduleViewUiComponentDir
        );

        if (formFile == null) {
            formFile = (XmlFile) fileFromTemplateGenerator.generate(
                    uiComponentFormXml,
                    getAttributes(),
                    formDirectory,
                    actionName
            );
        }

        if (formFile != null) {
            new XmlDeclarationsGenerator(data, project).generate(formFile);
        }

        return formFile;
    }

    /**
     * Fill ui component form properties.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        final PhpClassTypesBuilder phpClassTypesBuilder = new PhpClassTypesBuilder();

        if (data.getFields().isEmpty()) {
            phpClassTypesBuilder
                    .appendProperty("PRIMARY_FIELD", "");
        } else {
            phpClassTypesBuilder
                    .appendProperty("PRIMARY_FIELD", data.getFields().get(0).getName());
        }

        phpClassTypesBuilder
                .appendProperty("NAME", data.getFormName())
                .appendProperty("LABEL", data.getLabel())
                .appendProperty("BUTTONS", data.getButtons().isEmpty() ? "" : "true")
                .appendProperty("ROUTE", data.getRoute())
                .appendProperty("SUBMIT_CONTROLLER",
                        data.getSubmitControllerName().toLowerCase(new Locale("en","EN")))
                .appendProperty("SUBMIT_ACTION",
                        data.getSubmitActionName().toLowerCase(new Locale("en","EN")))
                .appendProperty("DATA_PROVIDER",
                        new UiComponentDataProviderFile(
                                data.getModuleName(),
                                data.getDataProviderName(),
                                data.getDataProviderPath()
                        ).getClassFqn()
                )
                .mergeProperties(attributes);
    }

    /**
     * Get Area Enum by its name.
     *
     * @param area String
     *
     * @return Areas
     */
    private Areas getArea(final @NotNull String area) {
        return Areas.getAreaByString(area);
    }
}
