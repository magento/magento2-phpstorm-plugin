/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.context.EntityCreatorContext;
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormButtonData;
import com.magento.idea.magento2plugin.actions.generation.dialog.util.ClassPropertyFormatterUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.actions.generation.util.GenerationContextRegistry;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.FormButtonBlockFile;
import com.magento.idea.magento2plugin.magento.files.FormGenericButtonBlockFile;
import com.magento.idea.magento2plugin.magento.packages.UiFormButtonTypeSettings;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;
import org.bouncycastle.util.Strings;
import org.jetbrains.annotations.NotNull;

public class UiComponentFormButtonBlockGenerator extends FileGenerator {

    private final UiComponentFormButtonData buttonData;
    private final Project project;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final DirectoryGenerator directoryGenerator;
    private final ModuleIndex moduleIndex;
    private final boolean checkFileAlreadyExists;
    private final List<String> uses;
    private final String entityName;
    private final String entityIdField;

    /**
     * Ui Form button by its type generator constructor.
     *
     * @param buttonData UiComponentFormButtonData
     * @param project Project
     */
    public UiComponentFormButtonBlockGenerator(
            final @NotNull UiComponentFormButtonData buttonData,
            final @NotNull Project project
    ) {
        this(buttonData, project, "Entity", "entity_id", true);
    }

    /**
     * Ui Form button by its type generator constructor.
     *
     * @param buttonData UiComponentFormButtonData
     * @param project Project
     * @param entityName String
     * @param entityIdField String
     */
    public UiComponentFormButtonBlockGenerator(
            final @NotNull UiComponentFormButtonData buttonData,
            final @NotNull Project project,
            final @NotNull String entityName,
            final @NotNull String entityIdField
    ) {
        this(buttonData, project, entityName, entityIdField, true);
    }

    /**
     * Ui Form button by its type generator constructor.
     *
     * @param buttonData UiComponentFormButtonData
     * @param project Project
     * @param entityName String
     * @param entityIdField String
     * @param checkFileAlreadyExists boolean
     */
    public UiComponentFormButtonBlockGenerator(
            final @NotNull UiComponentFormButtonData buttonData,
            final @NotNull Project project,
            final @NotNull String entityName,
            final @NotNull String entityIdField,
            final boolean checkFileAlreadyExists
    ) {
        super(project);
        this.buttonData = buttonData;
        this.project = project;
        this.checkFileAlreadyExists = checkFileAlreadyExists;
        this.entityName = entityName;
        this.entityIdField = entityIdField;
        fileFromTemplateGenerator = new FileFromTemplateGenerator(project);
        directoryGenerator = DirectoryGenerator.getInstance();
        moduleIndex = new ModuleIndex(project);
        uses = new ArrayList<>();
    }

    /**
     * Creates a module UI form button file.
     *
     * @param actionName String
     *
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final @NotNull String actionName) {
        final PhpClass buttonClass = GetPhpClassByFQN.getInstance(project).execute(
                buttonData.getFqn()
        );

        if (this.checkFileAlreadyExists && buttonClass != null) {
            return buttonClass.getContainingFile();
        }
        final PsiDirectory moduleBaseDir = moduleIndex.getModuleDirectoryByModuleName(
                buttonData.getButtonModule()
        );

        if (moduleBaseDir == null) {
            return null;
        }
        final PsiDirectory baseDirectory = directoryGenerator.findOrCreateSubdirectories(
                moduleBaseDir,
                buttonData.getButtonDirectory()
        );

        return fileFromTemplateGenerator.generate(
                new FormButtonBlockFile(
                        buttonData.getButtonModule(),
                        buttonData.getButtonClassName(),
                        buttonData.getButtonDirectory()
                ),
                getAttributes(),
                baseDirectory,
                actionName
        );
    }

    /**
     * Fill save command file attributes.
     *
     * @param attributes Properties
     */
    @Override
    protected void fillAttributes(final @NotNull Properties attributes) {
        attributes.setProperty("NAME", buttonData.getButtonClassName());
        attributes.setProperty("NAMESPACE", buttonData.getNamespace());
        attributes.setProperty("DATA_PROVIDER_TYPE",
                PhpClassGeneratorUtil.getNameFromFqn(FormButtonBlockFile.DATA_PROVIDER_TYPE));
        uses.add(FormButtonBlockFile.DATA_PROVIDER_TYPE);

        final FormGenericButtonBlockFile genericButtonBlockFile =
                new FormGenericButtonBlockFile(buttonData.getButtonModule(), entityName);

        final NamespaceBuilder genericBtnNamespace = new NamespaceBuilder(
                buttonData.getButtonModule(),
                FormGenericButtonBlockFile.CLASS_NAME,
                genericButtonBlockFile.getDirectory()
        );
        attributes.setProperty("GENERIC_BUTTON",
                PhpClassGeneratorUtil.getNameFromFqn(genericBtnNamespace.getClassFqn()));

        if (!genericButtonBlockFile.getDirectory().equals(buttonData.getButtonDirectory())) {
            uses.add(genericBtnNamespace.getClassFqn());
        }

        final UiFormButtonTypeSettings buttonTypeSettings = UiFormButtonTypeSettings.getByValue(
                Strings.toUpperCase(buttonData.getButtonType())
        );
        attributes.setProperty("CLASS_ANNOTATION", buttonTypeSettings.getAnnotation());
        attributes.setProperty("TYPE", buttonTypeSettings.getLabel());
        attributes.setProperty("LABEL", buttonTypeSettings.getLabel());
        attributes.setProperty("CLASS", buttonTypeSettings.getClasses());

        final String entityIdAccessor = "get" + Arrays.stream(entityIdField.split("_"))
                .map(s -> s.substring(0, 1).toUpperCase(Locale.getDefault()) + s.substring(1))
                .collect(Collectors.joining()) + "()";

        final Map<String, String> variables = new HashMap<>();
        variables.put("$varName", Strings.toLowerCase(entityName));

        final EntityCreatorContext context =
                (EntityCreatorContext) GenerationContextRegistry.getInstance().getContext();

        if (buttonTypeSettings.isVariableExpected("$varIdConst") && context != null) {
            final String dtoTypeFqn = context.getUserData(EntityCreatorContext.DTO_TYPE);
            Objects.requireNonNull(dtoTypeFqn);
            variables.put(
                    "$varIdConst",
                    ClassPropertyFormatterUtil.formatNameToConstant(entityIdField, dtoTypeFqn)
            );
            uses.add(dtoTypeFqn);
        } else {
            variables.put("$varIdConst", entityIdField);
        }
        variables.put("$varEntityIdAccessor", entityIdAccessor);

        attributes.setProperty("ON_CLICK", buttonTypeSettings.getOnClick(variables));
        attributes.setProperty("DATA_ATTRS", buttonTypeSettings.getDataAttrs(variables));
        attributes.setProperty("SORT_ORDER", String.valueOf(buttonTypeSettings.getSortOrder()));
        attributes.setProperty("ENTITY_NAME", Strings.toLowerCase(entityName));
        attributes.setProperty("ENTITY_ID", entityIdField);
        if (buttonData.getButtonType().equals(FormButtonBlockFile.TYPE_DELETE)) {
            attributes.setProperty("GET_ID", entityIdAccessor);
        }

        attributes.setProperty("USES", PhpClassGeneratorUtil.formatUses(uses));
    }
}
