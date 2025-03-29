/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.EventDispatcher;
import com.intellij.util.SmartList;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import java.util.EventListener;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "Magento2PluginSettings",
        storages = {
            @Storage("magento2plugin.xml")
        }
)
public class Settings implements PersistentStateComponent<Settings.State> {
    private final EventDispatcher<MagentoModuleDataListener> myEventDispatcher
            = EventDispatcher.create(MagentoModuleDataListener.class);
    public boolean pluginEnabled;
    public String defaultLicense;
    public static final String DEFAULT_LICENSE = "Proprietary";
    public String magentoPath;
    public boolean mftfSupportEnabled;
    public boolean myDoNotAskContentConfigAgain;
    public String magentoVersion;
    public String magentoEdition;
    public List<String> myMagentoFolders;

    @Override
    @Nullable
    public Settings.State getState() {
        return new State(
                this.pluginEnabled,
                this.magentoPath,
                this.defaultLicense,
                this.mftfSupportEnabled,
                this.myDoNotAskContentConfigAgain,
                this.magentoVersion,
                this.magentoEdition,
                this.myMagentoFolders
        );
    }

    /**
     * State setter.
     *
     * @param state State
     */
    public void setState(final State state) {
        final State oldState = this.getState();
        this.loadState(state);
        this.notifyListeners(state, oldState);
    }

    /**
     * Adds a Magento folder to the settings.
     *
     * @param folder Magento folder to add.
     */
    public void addMagentoFolder(@NotNull final String folder) {
        if (this.myMagentoFolders == null) {
            this.myMagentoFolders = new SmartList<>();
        }

        if (!this.myMagentoFolders.contains(folder)) {
            final State oldState = this.getState();
            this.myMagentoFolders.add(folder);
            this.notifyListeners(Objects.requireNonNull(this.getState()), oldState);
        }
    }

    /**
     * Removes a Magento folder from the settings.
     *
     * @param folder Magento folder to remove.
     */
    public void removeMagentoFolder(@NotNull final String folder) {
        if (this.myMagentoFolders != null && this.myMagentoFolders.contains(folder)) {
            final State oldState = this.getState();
            this.myMagentoFolders.remove(folder);
            this.notifyListeners(Objects.requireNonNull(this.getState()), oldState);
        }
    }

    /**
     * Checks if a Magento folder exists in the settings.
     *
     * @param folder Magento folder to check.
     * @return true if the folder exists, false otherwise.
     */
    public boolean containsMagentoFolder(@NotNull final String folder) {
        return this.myMagentoFolders != null && this.myMagentoFolders.contains(folder);
    }

    /**
     * Retrieves the list of Magento folders currently configured in the settings.
     *
     * @return a list of strings representing the paths of Magento folders.
     */
    public @Nullable List<String> getMagentoFolders() {
        return this.myMagentoFolders;
    }

    @Override
    public void loadState(final @NotNull Settings.State state) {
        this.pluginEnabled = state.isPluginEnabled();
        this.magentoPath = state.getMagentoPath();
        this.defaultLicense = state.getDefaultLicenseName();
        this.mftfSupportEnabled = state.isMftfSupportEnabled();
        this.myDoNotAskContentConfigAgain = state.isDoNotAskContentConfigAgain();
        this.magentoVersion = state.getMagentoVersion();
        this.magentoEdition = state.getMagentoEdition();
        this.myMagentoFolders = state.getMagentoFolders();
    }

    public void addListener(final MagentoModuleDataListener listener) {
        this.myEventDispatcher.addListener(listener);
    }

    private void notifyListeners(final State state, final State oldState) {
        if (!state.equals(oldState)) {
            this.myEventDispatcher.getMulticaster().stateChanged(state, oldState);
        }
    }

    public void setDoNotAskContentConfigurationAgain(
            final boolean doNotAskContentConfigurationAgain
    ) {
        this.myDoNotAskContentConfigAgain = doNotAskContentConfigurationAgain;
    }

    public void setMagentoPath(final String magentoPath) {
        this.magentoPath = magentoPath;
    }

    public interface MagentoModuleDataListener extends EventListener {
        void stateChanged(State state, State oldState);
    }

    public static Settings getInstance(final Project project) {
        return project.getService(Settings.class);
    }

    public static boolean isEnabled(final @NotNull Project project) {
        return getInstance(project).pluginEnabled;
    }

    public static String getDefaultLicenseName(final @NotNull Project project) {
        return getInstance(project).defaultLicense;
    }

    public static boolean isMftfSupportEnabled(final @NotNull Project project) {
        return getInstance(project).mftfSupportEnabled;
    }

    @Nullable
    public static String getLastMagentoPath() {
        return PropertiesComponent.getInstance().getValue("magento.support.magentoPath");
    }

    @Nullable
    public static String getMagentoPath(final @NotNull Project project) {
        return getInstance(project).magentoPath;
    }

    @SuppressWarnings({"PMD.DataClass"})
    @Tag
    public static class State {
        public boolean pluginEnabled;
        public String defaultLicenseName;
        public String magentoPath;
        public boolean mftfSupportEnabled;
        public boolean myDoNotAskContentConfigAgain;
        public String magentoVersion;
        public String magentoEdition;
        public List<String> myMagentoFolders;

        public State() {//NOPMD
        }

        /**
         * Settings State.
         *
         * @param pluginEnabled boolean
         * @param magentoPath String
         * @param defaultLicenseName String
         * @param mftfSupportEnabled boolean
         * @param myDoNotAskContentConfigAgain boolean
         * @param magentoVersion String
         * @param magentoEdition String
         * @param myMagentoFolders List
         */
        public State(
                final boolean pluginEnabled,
                final String magentoPath,
                final String defaultLicenseName,
                final boolean mftfSupportEnabled,
                final boolean myDoNotAskContentConfigAgain,
                final String magentoVersion,
                final String magentoEdition,
                final List<String> myMagentoFolders
        ) {
            this.pluginEnabled = pluginEnabled;
            this.magentoPath = magentoPath;
            this.defaultLicenseName = defaultLicenseName;
            this.mftfSupportEnabled = mftfSupportEnabled;
            this.myDoNotAskContentConfigAgain = myDoNotAskContentConfigAgain;
            this.magentoVersion = magentoVersion;
            this.magentoEdition = magentoEdition;
            this.myMagentoFolders = myMagentoFolders;
        }

        @Attribute("enabled")
        public boolean isPluginEnabled() {
            return this.pluginEnabled;
        }

        public void setPluginEnabled(final boolean enabled) {
            this.pluginEnabled = enabled;
        }

        public String getMagentoPath() {
            return this.magentoPath;
        }

        @Tag("magentoPath")
        public void setMagentoPath(final String magentoPath) {
            this.magentoPath = magentoPath;
        }

        public String getMagentoVersion() {
            return this.magentoVersion;
        }

        @Tag("magentoVersion")
        public void setMagentoVersion(final String magentoVersion) {
            this.magentoVersion = magentoVersion;
        }

        public String getMagentoEdition() {
            return magentoEdition;
        }

        @Tag("magentoEdition")
        public void setMagentoEdition(final String magentoEdition) {
            this.magentoEdition = magentoEdition;
        }

        public List<String> getMagentoFolders() {
            return this.myMagentoFolders;
        }

        /**
         * Adds a Magento folder to the list of tracked Magento folders.
         *
         * @param magentoFolders the name of the Magento folder to be added
         */
        @Tag("magentoFolders")
        public void addMagentoFolder(final String magentoFolders) {
            if (this.myMagentoFolders == null) {
                this.myMagentoFolders = new SmartList<>();
            }
            this.myMagentoFolders.add(magentoFolders);
        }

        /**
         * Removes a specified Magento folder from the list of tracked Magento folders.
         *
         * @param magentoFolders the name of the Magento folder to be removed
         */
        public void removeMagentoFolder(final String magentoFolders) {
            if (this.myMagentoFolders != null) {
                this.myMagentoFolders.remove(magentoFolders);
            }
        }

        /**
         * Last Used Magento Path setter.
         *
         * @param magentoPath String
         */
        public void setMagentoPathAndUpdateLastUsed(final String magentoPath) {
            this.setMagentoPath(magentoPath);
            if (!StringUtil.isEmptyOrSpaces(magentoPath)) {
                PropertiesComponent.getInstance()
                        .setValue("magento.support.magentoPath", magentoPath);
            }
        }

        public String getDefaultLicenseName() {
            return this.defaultLicenseName;
        }

        public void setDefaultLicenseName(final String defaultLicenseName) {
            this.defaultLicenseName = defaultLicenseName;
        }

        public boolean isMftfSupportEnabled() {
            return this.mftfSupportEnabled;
        }

        public boolean isDoNotAskContentConfigAgain() {
            return this.myDoNotAskContentConfigAgain;
        }

        public void setMftfSupportEnabled(final boolean mftfSupportEnabled) {
            this.mftfSupportEnabled = mftfSupportEnabled;
        }

        @SuppressWarnings({
                "PMD.ConfusingTernary",
                "PMD.CognitiveComplexity",
                "PMD.CyclomaticComplexity"
        })
        @Override
        public boolean equals(final Object objectToCompare) {
            if (this == objectToCompare) {
                return true;
            } else if (objectToCompare != null && this.getClass() == objectToCompare.getClass()) {
                final State state = (State) objectToCompare;
                if (this.isPluginEnabled() != state.isPluginEnabled()) {
                    return false;
                } else if (this.isMftfSupportEnabled() != state.isMftfSupportEnabled()) {
                    return false;
                } else if (
                        this.isDoNotAskContentConfigAgain() != state.isDoNotAskContentConfigAgain()
                ) {
                    return false;
                } else if (!Objects.equals(this.myMagentoFolders, state.myMagentoFolders)) {
                    return false;
                } else {
                    if (this.magentoPath != null) {
                        return this.magentoPath.equals(state.magentoPath);
                    }
                    if (this.defaultLicenseName != null) {
                        return this.defaultLicenseName.equals(state.defaultLicenseName);
                    } else {
                        return state.defaultLicenseName == null;
                    }
                }
            } else {
                return false;
            }
        }

        @SuppressWarnings({"PMD.ConfusingTernary"})
        @Override
        public int hashCode() {
            int result = this.isPluginEnabled() ? 1 : 0;
            result = 31 * result + (this.magentoPath != null ? this.magentoPath.hashCode() : 0);
            result = 31 * result + (this.isMftfSupportEnabled() ? 1 : 0);
            result = 31 * result + (this.isDoNotAskContentConfigAgain() ? 1 : 0);
            result = 31 * result + (
                    this.defaultLicenseName != null ? this.defaultLicenseName.hashCode() : 0
                );
            result = 31 * result
                    + (this.myMagentoFolders != null ? this.myMagentoFolders.hashCode() : 0);
            return result;
        }
    }
}
