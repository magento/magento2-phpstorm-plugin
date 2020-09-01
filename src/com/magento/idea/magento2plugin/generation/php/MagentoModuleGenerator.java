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
    public static String ACTION_NAME = "Magento 2 Module";

    public MagentoModuleGenerator() {
    }

    @Nls
    @NotNull
    public String getName() {
        return ACTION_NAME;
    }

    public String getDescription() {
        return "Create a Magento 2 Module";
    }

    public Icon getIcon() {
        return MagentoIcons.MODULE;
    }

    @NotNull
    public ProjectGeneratorPeer<MagentoProjectGeneratorSettings> createPeer() {
        return new MagentoProjectPeer();
    }

    public boolean isPrimaryGenerator() {
        return PlatformUtils.isPhpStorm();
    }

    /**
     * generateProject.
     *
     * @param project Project
     * @param baseDir Base directory
     * @param settings Settings
     * @param module Module
     */
    public void generateProject(
            @NotNull Project project,
            @NotNull VirtualFile baseDir,
            @NotNull MagentoProjectGeneratorSettings settings,
            @NotNull Module module
    ) {
        Settings dataService = Settings.getInstance(project);
        dataService.setState(settings.getMagentoState());

        Runnable generate = () -> {
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
     * generateComposerJson.
     *
     * @param project Project
     * @param baseDir Base directory
     * @param settings Settings
     * @return
     */
    private PsiFile generateComposerJson(
            @NotNull Project project,
            @NotNull PsiDirectory baseDir,
            @NotNull MagentoProjectGeneratorSettings settings
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
        ), project).generate(ACTION_NAME);
    }

    /**
     * generateRegistrationPhp.
     * @param project Project
     * @param baseDir Base directory
     * @param settings Settings
     * @return
     */
    private PsiFile generateRegistrationPhp(
            @NotNull Project project,
            @NotNull PsiDirectory baseDir,
            @NotNull MagentoProjectGeneratorSettings settings
    ) {
        return new ModuleRegistrationPhpGenerator(new ModuleRegistrationPhpData(
                settings.getPackageName(),
                settings.getModuleName(),
                baseDir,
                false
        ), project).generate(ACTION_NAME);
    }

    /**
     * generateModuleXml.
     *
     * @param project Project
     * @param baseDir Base directory
     * @param settings Settings
     */
    private void generateModuleXml(
            @NotNull Project project,
            @NotNull PsiDirectory baseDir,
            @NotNull MagentoProjectGeneratorSettings settings
    ) {
        ModuleXmlData moduleXmlData = new ModuleXmlData(
                settings.getPackageName(),
                settings.getModuleName(),
                settings.getModuleVersion(),
                baseDir,
                false
        );
        ModuleXmlGenerator moduleXmlGenerator = new ModuleXmlGenerator(moduleXmlData, project);
        moduleXmlGenerator.generate(ACTION_NAME, true);
    }

    @Nullable
    public String getHelpId() {
        return null;
    }
}
