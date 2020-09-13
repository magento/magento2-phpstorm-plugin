/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.generation.php;

import com.intellij.ide.util.projectWizard.WebProjectTemplate;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.ProjectGeneratorPeer;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.PlatformUtils;
import com.magento.idea.magento2plugin.MagentoIcons;
import com.magento.idea.magento2plugin.actions.generation.data.ModuleComposerJsonData;
import com.magento.idea.magento2plugin.actions.generation.data.ModuleRegistrationPhpData;
import com.magento.idea.magento2plugin.actions.generation.data.ModuleXmlData;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleComposerJsonGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleRegistrationPhpGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleXmlGenerator;
import com.magento.idea.magento2plugin.init.ConfigurationManager;
import com.magento.idea.magento2plugin.project.Settings;
import java.util.ArrayList;
import javax.swing.Icon;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class MagentoModuleGenerator extends WebProjectTemplate<MagentoProjectGeneratorSettings> {
    public static String actionName = "Magento 2 Module";

    @Nls
    @NotNull
    @Override
    public String getName() {
        return actionName;
    }

    @Override
    public String getDescription() {
        return "Create a Magento 2 Module";
    }

    @Override
    public Icon getIcon() {
        return MagentoIcons.MODULE;
    }

    @NotNull
    @Override
    public ProjectGeneratorPeer<MagentoProjectGeneratorSettings> createPeer() {
        return new MagentoProjectPeer();
    }

    @Override
    public boolean isPrimaryGenerator() {
        return PlatformUtils.isPhpStorm();
    }

    /**
     * Generate project.
     *
     * @param project Project
     * @param baseDir Base directory
     * @param settings Settings
     * @param module Module
     */
    @Override
    public void generateProject(
            @NotNull final Project project,
            @NotNull final VirtualFile baseDir,
            @NotNull final MagentoProjectGeneratorSettings settings,
            @NotNull final Module module
    ) {
        final Settings dataService = Settings.getInstance(project);
        dataService.setState(settings.getMagentoState());

        final Runnable generate = () -> {
            ApplicationManager.getApplication().runWriteAction(() -> {
                PsiDirectory baseDirElement = PsiManager
                        .getInstance(project)
                        .findDirectory(baseDir);
                if (baseDirElement == null) {
                    return;
                }

                generateComposerJson(project, baseDirElement, settings);
                generateRegistrationPhp(project, baseDirElement, settings);
                generateModuleXml(project, baseDirElement, settings);
                ConfigurationManager
                        .getInstance()
                        .refreshIncludePaths(dataService.getState(), project);
            });
        };
        StartupManager.getInstance(project).runWhenProjectIsInitialized(generate);
    }

    /**
     * Generate composer json.
     *
     * @param project Project
     * @param baseDir Base directory
     * @param settings Settings
     * @return
     */
    private PsiFile generateComposerJson(
            @NotNull final Project project,
            @NotNull final PsiDirectory baseDir,
            @NotNull final MagentoProjectGeneratorSettings settings
    ) {
        return new ModuleComposerJsonGenerator(new ModuleComposerJsonData(
                settings.getPackageName(),
                settings.getModuleName(),
                baseDir,
                settings.getModuleDescription(),
                settings.getComposerPackageName(),
                settings.getModuleVersion(),
                settings.getModuleLicenses(),
                new ArrayList<>(),
                false
        ), project).generate(actionName);
    }

    /**
     * Generate registration php.
     *
     * @param project Project
     * @param baseDir Base directory
     * @param settings Settings
     * @return
     */
    private PsiFile generateRegistrationPhp(
            @NotNull final Project project,
            @NotNull final PsiDirectory baseDir,
            @NotNull final MagentoProjectGeneratorSettings settings
    ) {
        return new ModuleRegistrationPhpGenerator(new ModuleRegistrationPhpData(
                settings.getPackageName(),
                settings.getModuleName(),
                baseDir,
                false
        ), project).generate(actionName);
    }

    /**
     * Generate module xml.
     *
     * @param project Project
     * @param baseDir Base directory
     * @param settings Settings
     */
    private void generateModuleXml(
            @NotNull final Project project,
            @NotNull final PsiDirectory baseDir,
            @NotNull final MagentoProjectGeneratorSettings settings
    ) {
        final ModuleXmlData moduleXmlData = new ModuleXmlData(
                settings.getPackageName(),
                settings.getModuleName(),
                settings.getModuleVersion(),
                baseDir,
                false
        );
        final ModuleXmlGenerator moduleXmlGenerator =
                new ModuleXmlGenerator(moduleXmlData, project);
        moduleXmlGenerator.generate(actionName, true);
    }

    @Nullable
    @Override
    public String getHelpId() {
        return null;
    }
}
