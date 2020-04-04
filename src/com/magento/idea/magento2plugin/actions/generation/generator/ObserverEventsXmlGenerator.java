package com.magento.idea.magento2plugin.actions.generation.generator;


import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.data.PluginDiXmlData;


public class ObserverEventsXmlGenerator {
    private PluginDiXmlData pluginDiXmlData;
    private Project project;

    public ObserverEventsXmlGenerator(PluginDiXmlData pluginDiXmlData, Project project) {
        this.pluginDiXmlData = pluginDiXmlData;
        this.project = project;
    }
}
