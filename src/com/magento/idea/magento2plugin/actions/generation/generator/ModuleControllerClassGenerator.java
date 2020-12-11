/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.ControllerFileData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.Controller;
import com.magento.idea.magento2plugin.magento.files.ControllerBackendPhp;
import com.magento.idea.magento2plugin.magento.files.ControllerFrontendPhp;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.HttpMethod;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.swing.JOptionPane;

@SuppressWarnings({"PMD.OnlyOneReturn", "PMD.DataflowAnomalyAnalysis"})
public class ModuleControllerClassGenerator extends FileGenerator {
    private final ControllerFileData controllerFileData;
    private final Project project;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    private final GetFirstClassOfFile getFirstClassOfFile;
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;

    /**
     * Generates new Controller PHP Class based on provided data.
     *
     * @param controllerFileData ControllerFileData
     * @param project Project
     */
    public ModuleControllerClassGenerator(
            final ControllerFileData controllerFileData,
            final Project project
    ) {
        super(project);
        this.project = project;
        this.controllerFileData = controllerFileData;
        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        this.getFirstClassOfFile = GetFirstClassOfFile.getInstance();
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }

    /**
     * Generate controller class.
     *
     * @param actionName Action name
     * @return PsiFile
     */
    public PsiFile generate(final String actionName) {
        final PsiFile[] controllerFiles = new PsiFile[1];

        WriteCommandAction.runWriteCommandAction(project, () -> {
            PhpClass controller = GetPhpClassByFQN.getInstance(project).execute(
                    getControllerFqn()
            );

            if (controller != null) {
                final String errorMessage = this.validatorBundle.message(
                        "validator.file.alreadyExists",
                        "Controller Class"
                );
                JOptionPane.showMessageDialog(
                        null,
                        errorMessage,
                        commonBundle.message("common.error"),
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            controller = createControllerClass(actionName);

            if (controller == null) {
                final String errorMessage = this.validatorBundle.message(
                        "validator.file.cantBeCreated",
                        "Controller Class"
                );
                JOptionPane.showMessageDialog(
                        null,
                        errorMessage,
                        commonBundle.message("common.error"),
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            controllerFiles[0] = controller.getContainingFile();
        });

        return controllerFiles[0];
    }

    /**
     * Get controller module.
     *
     * @return String
     */
    public String getControllerModule() {
        return controllerFileData.getControllerModule();
    }

    /**
     * Get HTTP method interface by method name.
     *
     * @param method HTTP Method name
     * @return String
     */
    public static String getHttpMethodInterfaceByMethod(final String method) {
        return HttpMethod.getRequestInterfaceFqnByMethodName(method);
    }

    private String getControllerFqn() {
        return String.format(
                "%s%s%s",
                controllerFileData.getNamespace(),
                Package.fqnSeparator,
                controllerFileData.getActionClassName()
        );
    }

    private PhpClass createControllerClass(final String actionName) {
        PsiDirectory parentDirectory = ModuleIndex.getInstance(project)
                .getModuleDirectoryByModuleName(getControllerModule());
        final PsiFile controllerFile;
        final String[] controllerDirectories = controllerFileData.getActionDirectory().split(
                File.separator
        );
        for (final String controllerDirectory: controllerDirectories) {
            parentDirectory = directoryGenerator.findOrCreateSubdirectory(
                    parentDirectory, controllerDirectory
            );
        }

        final Properties attributes = getAttributes();
        final String adminhtmlArea = Areas.adminhtml.toString();

        if (controllerFileData.getControllerArea().equals(adminhtmlArea)) {
            controllerFile = fileFromTemplateGenerator.generate(
                    ControllerBackendPhp.getInstance(controllerFileData.getActionClassName()),
                    attributes,
                    parentDirectory,
                    actionName
            );
        } else {
            controllerFile = fileFromTemplateGenerator.generate(
                    ControllerFrontendPhp.getInstance(controllerFileData.getActionClassName()),
                    attributes,
                    parentDirectory,
                    actionName
            );
        }

        if (controllerFile == null) {
            return null;
        }

        return getFirstClassOfFile.execute((PhpFile) controllerFile);
    }

    protected void fillAttributes(final Properties attributes) {
        final String actionClassName = controllerFileData.getActionClassName();
        attributes.setProperty("NAME", actionClassName);
        attributes.setProperty("NAMESPACE", controllerFileData.getNamespace());
        final String httpMethodInterface = getHttpMethodInterfaceByMethod(
                controllerFileData.getHttpMethodName()
        );
        attributes.setProperty(
                "IMPLEMENTS",
                PhpClassGeneratorUtil.getNameFromFqn(httpMethodInterface)
        );
        final List<String> uses = getUses();
        uses.add(httpMethodInterface);

        if (controllerFileData.getIsInheritClass()) {
            final String adminhtmlArea = Areas.adminhtml.toString();

            if (controllerFileData.getControllerArea().equals(adminhtmlArea)) {
                uses.add(Controller.ADMINHTML_CONTROLLER_FQN);
                attributes.setProperty(
                        "EXTENDS",
                        PhpClassGeneratorUtil.getNameFromFqn(Controller.ADMINHTML_CONTROLLER_FQN)
                );
                attributes.setProperty(
                        "ACL",
                        PhpClassGeneratorUtil.getNameFromFqn(controllerFileData.getAcl())
                );
            } else {
                uses.add(Controller.FRONTEND_CONTROLLER_FQN);
                attributes.setProperty(
                        "EXTENDS",
                        PhpClassGeneratorUtil.getNameFromFqn(Controller.FRONTEND_CONTROLLER_FQN)
                );
            }
        }

        attributes.setProperty("USES", PhpClassGeneratorUtil.formatUses(uses));
    }

    private List<String> getUses() {
        return new ArrayList<>(Arrays.asList(
                Controller.RESPONSE_INTERFACE_FQN,
                Controller.RESULT_INTERFACE_FQN,
                Controller.EXCEPTION_CLASS_FQN
        ));
    }
}
