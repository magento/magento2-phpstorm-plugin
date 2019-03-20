package com.magento.idea.magento2plugin.project;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
    name = "Magento2PluginSettings",
    storages = {
        @Storage("magento2plugin.xml")
    }
)
public class Settings implements PersistentStateComponent<Settings> {
    public boolean pluginEnabled = false;

    @Nullable
    @Override
    public Settings getState() {
        return this;
    }

    @Override
    public void loadState(Settings settings) {
        XmlSerializerUtil.copyBean(settings, this);
    }

    public static Settings getInstance(Project project) {
        return ServiceManager.getService(project, Settings.class);
    }

    public static boolean isEnabled(@NotNull Project project) {
        return getInstance(project).pluginEnabled;
    }
}
