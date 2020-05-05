/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.generator.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.ModuleEventsXml;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.FileBasedIndexUtil;

import java.util.ArrayList;
import java.util.Properties;

public class FindOrCreateEventsXml {
    private static FindOrCreateEventsXml INSTANCE = null;
    private Project project;

    public static FindOrCreateEventsXml getInstance(Project project) {
        if (null == INSTANCE) {
            INSTANCE = new FindOrCreateEventsXml(project);
        }

        return INSTANCE;
    }

    FindOrCreateEventsXml(Project project) {
        this.project = project;
    }

    public PsiFile execute(String actionName, String moduleName, String area) {
        DirectoryGenerator directoryGenerator = DirectoryGenerator.getInstance();
        FileFromTemplateGenerator fileFromTemplateGenerator = FileFromTemplateGenerator.getInstance(project);
        PsiDirectory parentDirectory = ModuleIndex.getInstance(project).getModuleDirectoryByModuleName(moduleName);
        ArrayList<String> fileDirectories = new ArrayList<>();
        fileDirectories.add(Package.moduleBaseAreaDir);
        if (!getArea(area).equals(Areas.base)) {
            fileDirectories.add(getArea(area).toString());
        }
        for (String fileDirectory: fileDirectories) {
            parentDirectory = directoryGenerator.findOrCreateSubdirectory(parentDirectory, fileDirectory);
        }
        ModuleEventsXml moduleEventsXml = new ModuleEventsXml();
        PsiFile eventsXml = FileBasedIndexUtil.findModuleConfigFile(
                moduleEventsXml.getFileName(),
                getArea(area),
                moduleName,
                project
        );
        if (eventsXml == null) {
            eventsXml = fileFromTemplateGenerator.generate(moduleEventsXml, new Properties(), parentDirectory, actionName);
        }
        return eventsXml;
    }

    private Areas getArea(String area) {
        return Package.getAreaByString(area);
    }
}
