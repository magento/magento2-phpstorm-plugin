/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.generator.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.magento.FileBasedIndexUtil;
import java.util.ArrayList;
import java.util.Properties;

public class FindOrCreateDiXml {
    private static FindOrCreateDiXml INSTANCE = null;
    private Project project;

    public static FindOrCreateDiXml getInstance(Project project) {
        if (null == INSTANCE) {
            INSTANCE = new FindOrCreateDiXml(project);
        }

        return INSTANCE;
    }

    FindOrCreateDiXml(Project project) {
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
        ModuleDiXml moduleDiXml = new ModuleDiXml();
        PsiFile diXml = FileBasedIndexUtil.findModuleConfigFile(
                moduleDiXml.getFileName(),
                getArea(area),
                moduleName,
                project
        );
        if (diXml == null) {
            diXml = fileFromTemplateGenerator.generate(moduleDiXml, new Properties(), parentDirectory, actionName);
        }
        return diXml;
    }

    private Areas getArea(String area) {
        return Package.getAreaByString(area);
    }
}
