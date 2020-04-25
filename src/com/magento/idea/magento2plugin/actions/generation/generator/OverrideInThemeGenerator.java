package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;

public class OverrideInThemeGenerator {

    private static OverrideInThemeGenerator INSTANCE = null;

    private final Project project;

    public static OverrideInThemeGenerator getInstance(Project project) {
        if (null == INSTANCE) {
            INSTANCE = new OverrideInThemeGenerator(project);
        }
        return INSTANCE;
    }

    public OverrideInThemeGenerator(Project project) {
        this.project = project;
    }

    public void execute(PsiFile baseFile, String themeName) {
        ModuleIndex moduleIndex = ModuleIndex.getInstance(project);
        PsiDirectory directory = moduleIndex.getModuleDirectoryByModuleName(themeName);

        //TODO: Copy file to correct path when source is module
        //TODO: Copy file to correct path when source is theme
        //TODO: Throw error if theme file already exists
        directory.copyFileFrom(baseFile.getName(), baseFile);
        PsiFile newFile = directory.findFile(baseFile.getName());
        newFile.navigate(true);
    }
}
