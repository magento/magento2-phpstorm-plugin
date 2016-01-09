package com.magento.idea.magento2plugin;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
    name = "Magento2PluginSettings",
    storages = {
        @Storage(id = "default", file = StoragePathMacros.PROJECT_FILE),
        @Storage(id = "dir", file = StoragePathMacros.PROJECT_CONFIG_DIR + "/magento2plugin.xml", scheme = StorageScheme.DIRECTORY_BASED)
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

    protected Project project;

    public static Settings getInstance(Project project) {
        Settings settings = ServiceManager.getService(project, Settings.class);

        settings.project = project;

        return settings;
    }

    public static boolean isEnabled(@NotNull Project project) {
        return getInstance(project).pluginEnabled;
    }
}
