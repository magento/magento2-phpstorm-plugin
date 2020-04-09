/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.project;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.EventDispatcher;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import com.magento.idea.magento2plugin.init.ConfigurationManager;
import com.magento.idea.magento2plugin.util.magento.MagentoBasePathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.EventListener;

@State(
    name = "Magento2PluginSettings",
    storages = {
        @Storage("magento2plugin.xml")
    }
)
public class Settings implements PersistentStateComponent<Settings.State> {
    private final EventDispatcher<MagentoModuleDataListener> myEventDispatcher = EventDispatcher.create(MagentoModuleDataListener.class);
    public boolean pluginEnabled = false;
    public static String DEFAULT_LICENSE = "Proprietary";
    public String magentoPath;
    public boolean mftfSupportEnabled = false;
    public boolean myDoNotAskContentConfigAgain = false;

    @Nullable
    public Settings.State getState() {
        return new Settings.State(this.pluginEnabled, this.magentoPath, DEFAULT_LICENSE, this.mftfSupportEnabled, this.myDoNotAskContentConfigAgain);
    }

    public void setState(State state) {
        State oldState = this.getState();
        this.loadState(state);
        this.notifyListeners(state, oldState);
    }

    public void loadState(@NotNull Settings.State state) {
        this.pluginEnabled = state.isPluginEnabled();
        this.magentoPath = state.getMagentoPath();
        this.DEFAULT_LICENSE = state.getDefaultLicenseName();
        this.mftfSupportEnabled = state.isMftfSupportEnabled();
        this.myDoNotAskContentConfigAgain = state.isDoNotAskContentConfigAgain();
    }

    public void addListener(MagentoModuleDataListener listener) {
        this.myEventDispatcher.addListener(listener);
    }

    private void notifyListeners(State state, State oldState) {
        if (!state.equals(oldState)) {
            this.myEventDispatcher.getMulticaster().stateChanged(state, oldState);
        }
    }

    public void setDoNotAskContentConfigurationAgain(boolean doNotAskContentConfigurationAgain) {
        this.myDoNotAskContentConfigAgain = doNotAskContentConfigurationAgain;
    }

    public void setMagentoPath(String magentoPath) {
        this.magentoPath = magentoPath;
    }

    public interface MagentoModuleDataListener extends EventListener {
        void stateChanged(State state, State oldState);
    }

    public static Settings getInstance(Project project) {
        return ServiceManager.getService(project, Settings.class);
    }

    public static boolean isEnabled(@NotNull Project project) {
        return getInstance(project).pluginEnabled;
    }

    public static String getDefaultLicenseName(@NotNull Project project) {
        return getInstance(project).DEFAULT_LICENSE;
    }

    public static boolean isMftfSupportEnabled(@NotNull Project project) {
        return getInstance(project).mftfSupportEnabled;
    }

    @Nullable
    public static String getLastMagentoPath() {
        return PropertiesComponent.getInstance().getValue("magento.support.magentoPath");
    }

    @Nullable
    public static String getMagentoPath(@NotNull Project project) {
        Settings settings = getInstance(project);
        String path = settings.magentoPath;
        if (path == null || path.isEmpty()) {
            if (MagentoBasePathUtil.isMagentoFolderValid(project.getBasePath())) {
                settings.setMagentoPath(project.getBasePath());
            } else {
                settings.pluginEnabled = false;
                ConfigurationManager.suggestToConfigureMagentoPath(project);
                return null;
            }
        }
        return settings.magentoPath;
    }

    @Tag
    public static class State {
        public boolean pluginEnabled;
        public String defaultLicenseName;
        public String magentoPath;
        public boolean mftfSupportEnabled;
        public boolean myDoNotAskContentConfigAgain;

        public State() {
        }

        public State(boolean pluginEnabled, String magentoPath, String defaultLicenseName, boolean mftfSupportEnabled, boolean myDoNotAskContentConfigAgain) {
            this.pluginEnabled = pluginEnabled;
            this.magentoPath = magentoPath;
            this.defaultLicenseName = defaultLicenseName;
            this.mftfSupportEnabled = mftfSupportEnabled;
            this.myDoNotAskContentConfigAgain = myDoNotAskContentConfigAgain;
        }

        @Attribute("enabled")
        public boolean isPluginEnabled() {
            return this.pluginEnabled;
        }

        public void setPluginEnabled(boolean enabled) {
            this.pluginEnabled = enabled;
        }

        public String getMagentoPath() {
            return this.magentoPath;
        }

        @Tag("magentoPath")
        public void setMagentoPath(String magentoPath) {
            this.magentoPath = magentoPath;
        }

        public void setMagentoPathAndUpdateLastUsed(String magetnoPath) {
            this.setMagentoPath(magetnoPath);
            if (!StringUtil.isEmptyOrSpaces(magetnoPath)) {
                PropertiesComponent.getInstance().setValue("magento.support.magentoPath", magetnoPath);
            }
        }

        public String getDefaultLicenseName() {
            return this.defaultLicenseName;
        }

        public void setDefaultLicenseName(String defaultLicenseName) {
            this.defaultLicenseName = defaultLicenseName;
        }

        public boolean isMftfSupportEnabled() {
            return this.mftfSupportEnabled;
        }

        public boolean isDoNotAskContentConfigAgain() {
            return this.myDoNotAskContentConfigAgain;
        }

        public void setMftfSupportEnabled(boolean mftfSupportEnabled) {
            this.mftfSupportEnabled = mftfSupportEnabled;
        }

        public boolean equals(Object objectToCompare) {
            if (this == objectToCompare) {
                return true;
            } else if (objectToCompare != null && this.getClass() == objectToCompare.getClass()) {
                State state = (State) objectToCompare;
                if (this.isPluginEnabled() != state.isPluginEnabled()) {
                    return false;
                } else if (this.isMftfSupportEnabled() != state.isMftfSupportEnabled()) {
                    return false;
                } else if (this.isDoNotAskContentConfigAgain() != state.isDoNotAskContentConfigAgain()) {
                    return false;
                } else {
                    if (this.magentoPath != null) {
                        return this.magentoPath.equals(state.magentoPath);
                    }
                    if (this.defaultLicenseName != null) {
                        return this.defaultLicenseName.equals(state.defaultLicenseName);
                    } else return state.defaultLicenseName == null;
                }
            } else {
                return false;
            }
        }

        public int hashCode() {
            int result = this.isPluginEnabled() ? 1 : 0;
            result = 31 * result + (this.magentoPath != null ? this.magentoPath.hashCode() : 0);
            result = 31 * result + (this.isMftfSupportEnabled() ? 1 : 0);
            result = 31 * result + (this.isDoNotAskContentConfigAgain() ? 1 : 0);
            result = 31 * result + (this.defaultLicenseName != null ? this.defaultLicenseName.hashCode() : 0);
            return result;
        }
    }
}
