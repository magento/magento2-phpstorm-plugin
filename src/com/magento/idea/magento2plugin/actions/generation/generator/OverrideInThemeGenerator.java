package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.util.RegExUtil;
import com.magento.idea.magento2plugin.util.magento.ComponentType;
import com.magento.idea.magento2plugin.util.magento.GetComponentNameByDirectory;
import com.magento.idea.magento2plugin.util.magento.GetComponentTypeByName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        GetComponentNameByDirectory getComponentNameByDirectory = GetComponentNameByDirectory.getInstance(project);
        String componentType = GetComponentTypeByName.execute(getComponentNameByDirectory
                .execute(baseFile.getContainingDirectory()));

        List<String> pathComponents;
        if (componentType.equals(ComponentType.TYPE_MODULE)) {
            pathComponents = getModulePathComponents(
                    baseFile,
                    getComponentNameByDirectory.execute(baseFile.getContainingDirectory())
            );
        } else if (componentType.equals(ComponentType.TYPE_THEME)) {
            pathComponents = getThemePathComponents(baseFile);
        } else {
            return;
        }

        directory = getTargetDirectory(directory, pathComponents);

        if (directory.findFile(baseFile.getName()) != null) {
            JBPopupFactory.getInstance()
                    .createMessage("Override already exists in selected theme.")
                    .showCenteredInCurrentWindow(project);
            directory.findFile(baseFile.getName()).navigate(true);
            return;
        }

        directory.copyFileFrom(baseFile.getName(), baseFile);
        PsiFile newFile = directory.findFile(baseFile.getName());
        newFile.navigate(true);
    }

    private boolean isModule(String componentName) {
        Pattern modulePattern = Pattern.compile(RegExUtil.Magento.MODULE_NAME);
        Matcher moduleMatcher = modulePattern.matcher(componentName);

        return moduleMatcher.find();
    }

    private boolean isTheme(String componentName) {
        Pattern themePattern = Pattern.compile(RegExUtil.Magento.THEME_NAME);
        Matcher themeMatcher = themePattern.matcher(componentName);

        return themeMatcher.find();
    }

    private List<String> getModulePathComponents(PsiFile file, String componentName) {
        List<String> pathComponents = new ArrayList<>();
        PsiDirectory parent = file.getParent();
        while (!parent.getName().equals("frontend") && !parent.getName().equals("adminhtml")) {
            pathComponents.add(parent.getName());
            parent = parent.getParent();
        }
        pathComponents.add(componentName);
        Collections.reverse(pathComponents);

        return pathComponents;
    }

    private List<String> getThemePathComponents(PsiFile file) {
        List<String> pathComponents = new ArrayList<>();
        Pattern pattern = Pattern.compile(RegExUtil.Magento.MODULE_NAME);

        PsiDirectory parent = file.getParent();
        do {
            pathComponents.add(parent.getName());
            parent = parent.getParent();
        } while (!pattern.matcher(parent.getName()).find());
        pathComponents.add(parent.getName());
        Collections.reverse(pathComponents);

        return pathComponents;
    }

    private PsiDirectory getTargetDirectory(PsiDirectory directory, List<String> pathComponents) {
        for (int i = 0; i < pathComponents.size(); i++) {
            String directoryName = pathComponents.get(i);
            if (directory.findSubdirectory(directoryName) != null) {
                directory = directory.findSubdirectory(directoryName);
            } else {
                directory = directory.createSubdirectory(directoryName);
            }
        }

        return directory;
    }
}
