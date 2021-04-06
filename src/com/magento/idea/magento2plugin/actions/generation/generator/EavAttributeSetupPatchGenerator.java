package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.EavEntityDataInterface;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.eav.AttributeMapperFactory;
import com.magento.idea.magento2plugin.actions.generation.generator.util.eav.AttributeMapperInterface;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.EavAttributeDataPatchPhp;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.magento.packages.eav.DataPatchDependency;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import com.sun.istack.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;

public class EavAttributeSetupPatchGenerator extends FileGenerator {
    private final EavEntityDataInterface eavEntityData;
    private final Project project;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;

    public EavAttributeSetupPatchGenerator(
            final @NotNull EavEntityDataInterface eavEntityData,
            Project project
    ) {
        super(project);

        this.eavEntityData = eavEntityData;
        this.project = project;
        this.directoryGenerator = new DirectoryGenerator();
        this.fileFromTemplateGenerator = new FileFromTemplateGenerator(project);
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }

    @Override
    public PsiFile generate(String actionName) {
        final String errorTitle = commonBundle.message("common.error");
        final PhpClass dataPatchClass = GetPhpClassByFQN.getInstance(project)
                .execute(getDataPatchFqn());

        if (validateIfFileAlreadyExist(dataPatchClass, errorTitle)) return null;

        final PhpFile dataPathFile = createDataPathClass(actionName);

        if (validateIfFileCanBeCreated(errorTitle, dataPathFile)) return null;

        return dataPathFile;
    }

    private String getDataPatchFqn() {
        return eavEntityData.getNamespace()
                + Package.fqnSeparator
                + eavEntityData.getDataPatchName();
    }

    private boolean validateIfFileAlreadyExist(PhpClass dataPatchClass, String errorTitle) {
        if (dataPatchClass != null) {
            final String errorMessage = validatorBundle.message(
                    "validator.file.alreadyExists",
                    "Data Patch Class"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return true;
        }

        return false;
    }

    @Nullable
    private PhpFile createDataPathClass(final String actionName) {
        PsiDirectory parentDirectory = getDataPatchDirectory();
        final Properties attributes = getAttributes();
        final PsiFile dataPatchFile = fileFromTemplateGenerator.generate(
                EavAttributeDataPatchPhp.getInstance(eavEntityData.getDataPatchName()),
                attributes,
                parentDirectory,
                actionName
        );

        if (dataPatchFile == null) {
            return null;
        }

        return (PhpFile) dataPatchFile;
    }

    private PsiDirectory getDataPatchDirectory() {
        PsiDirectory parentDirectory = new ModuleIndex(project)
                .getModuleDirectoryByModuleName(eavEntityData.getModuleName());
        final String[] dataPatchDirectories = eavEntityData.getDirectory().split(File.separator);

        for (final String dataPatchDirectory : dataPatchDirectories) {
            parentDirectory = directoryGenerator.findOrCreateSubdirectory(
                    parentDirectory,
                    dataPatchDirectory
            );
        }

        return parentDirectory;
    }

    private boolean validateIfFileCanBeCreated(String errorTitle, PhpFile dataPathFile) {
        if (dataPathFile == null) {
            final String errorMessage = validatorBundle.message(
                    "validator.file.cantBeCreated",
                    "Data Patch Class"
            );
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return true;
        }
        return false;
    }

    @Override
    protected void fillAttributes(Properties attributes) {
        attributes.setProperty("NAME", eavEntityData.getDataPatchName());
        attributes.setProperty("NAMESPACE", eavEntityData.getNamespace());

        final String dataPatchInterface = DataPatchDependency.DATA_PATCH_INTERFACE.getClassPatch();
        attributes.setProperty(
                "IMPLEMENTS",
                PhpClassGeneratorUtil.getNameFromFqn(dataPatchInterface)
        );

        final String eavSetup = DataPatchDependency.ENV_SETUP.getClassPatch();
        final String eavSetupFactory = DataPatchDependency.EAV_SETUP_FACTORY.getClassPatch();
        final String moduleDataSetupInterface = DataPatchDependency.MODULE_DATA_SETUP_INTERFACE.getClassPatch();
        final String entityClass = eavEntityData.getEntityClass();

        attributes.setProperty(
                "MODULE_DATA_SETUP_INTERFACE",
                PhpClassGeneratorUtil.getNameFromFqn(moduleDataSetupInterface)
        );
        attributes.setProperty(
                "EAV_SETUP_FACTORY",
                PhpClassGeneratorUtil.getNameFromFqn(eavSetupFactory)
        );
        attributes.setProperty(
                "EAV_SETUP",
                PhpClassGeneratorUtil.getNameFromFqn(eavSetup)
        );
        attributes.setProperty(
                "ENTITY_CLASS",
                entityClass
        );

        final List<String> uses = new ArrayList<>();
        uses.add(dataPatchInterface);
        uses.add(eavSetup);
        uses.add(eavSetupFactory);
        uses.add(moduleDataSetupInterface);

        attributes.setProperty("USES", PhpClassGeneratorUtil.formatUses(uses));

        attributes.setProperty("ATTRIBUTE_CODE", eavEntityData.getCode());

        List<String> entityAttributes = getAttributesList(eavEntityData);
        attributes.setProperty("ATTRIBUTE_SET", String.join(",", entityAttributes));


    }

    private List<String> getAttributesList(EavEntityDataInterface eavEntityData) {
        AttributeMapperFactory attributeMapperFactory = new AttributeMapperFactory();
        AttributeMapperInterface attributeMapper = attributeMapperFactory.createByEntityClass(eavEntityData.getEntityClass());

        return attributeMapper.mapAttributesByEntityData(eavEntityData);
    }
}
