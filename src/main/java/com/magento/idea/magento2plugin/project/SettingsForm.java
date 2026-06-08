/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.magento.idea.magento2plugin.indexes.IndexManager;
import com.magento.idea.magento2plugin.project.MagentoSkillInstaller.AgentTarget;
import com.magento.idea.magento2plugin.magento.packages.MagentoComponentManager;
import com.magento.idea.magento2plugin.project.MagentoSkillInstaller.Skill;
import com.magento.idea.magento2plugin.project.indexing.MagentoAdditionalLibraryRootsProvider;
import com.magento.idea.magento2plugin.project.util.GetProjectBasePath;
import com.magento.idea.magento2plugin.project.validator.SettingsFormValidator;
import com.magento.idea.magento2plugin.util.magento.MagentoVersionUtil;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({
        "PMD.TooManyFields",
        "PMD.TooManyMethods"
})
public class SettingsForm implements SearchableConfigurable {

    private static final String DEFAULT_MAGENTO_EDITION_LABEL = "Platform Version:";

    private final Project project;
    private JCheckBox pluginEnabled;
    private JButton buttonReindex;
    private JPanel panel;
    private JButton regenerateUrnMapButton;
    private JTextField magentoVersion;
    private JTextField moduleDefaultLicenseName;
    private JCheckBox mftfSupportEnabled;
    private TextFieldWithBrowseButton magentoPath;
    private JTextField mcpCliToolCandidates;
    private JButton installMagentoScaffoldSkillButton;
    private JButton installMagentoInspectSkillButton;
    private JComboBox<AgentTarget> skillAgentTargetSelect;
    private final SettingsFormValidator validator = new SettingsFormValidator(this);
    private JLabel magentoVersionLabel;//NOPMD
    private JLabel magentoPathLabel;//NOPMD

    public SettingsForm(@NotNull final Project project) {
        this.project = project;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Magento";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        buttonReindex.addActionListener(event -> reindexFromForm());

        regenerateUrnMapButton.addMouseListener(
                new RegenerateUrnMapListener(project)
        );
        installMagentoScaffoldSkillButton.addActionListener(
                event -> installMagentoSkill(Skill.MAGENTO_SCAFFOLD)
        );
        installMagentoInspectSkillButton.addActionListener(
                event -> installMagentoSkill(Skill.MAGENTO_INSPECT)
        );
        skillAgentTargetSelect.removeAllItems();
        for (final AgentTarget target : AgentTarget.values()) {
            skillAgentTargetSelect.addItem(target);
        }
        skillAgentTargetSelect.setSelectedItem(AgentTarget.PROJECT_SKILLS);

        refreshFormStatus(getSettings().pluginEnabled);
        pluginEnabled.addActionListener(e -> refreshFormStatus(pluginEnabled.isSelected()));

        moduleDefaultLicenseName.setText(getSettings().defaultLicense);
        mftfSupportEnabled.setSelected(getSettings().mftfSupportEnabled);
        magentoPath.getTextField().setText(StringUtil.notNullize(Settings.getMagentoPath(project)));
        mcpCliToolCandidates.setText(
                Settings.getNormalizedMcpCliToolCandidates(getSettings().mcpCliToolCandidates)
        );
        resolveMagentoVersion();

        addPathListener();
        addMagentoVersionListener();
        updateMagentoVersion();

        return (JComponent) panel;
    }

    private void refreshFormStatus(final boolean isEnabled) {
        buttonReindex.setEnabled(isEnabled);
        regenerateUrnMapButton.setEnabled(isEnabled);
        magentoVersion.setEnabled(isEnabled);
        mftfSupportEnabled.setEnabled(isEnabled);
        magentoPath.setEnabled(isEnabled);
        mcpCliToolCandidates.setEnabled(isEnabled);
        moduleDefaultLicenseName.setEnabled(isEnabled);
        installMagentoScaffoldSkillButton.setEnabled(isEnabled);
        installMagentoInspectSkillButton.setEnabled(isEnabled);
        skillAgentTargetSelect.setEnabled(isEnabled);
    }

    protected void reindex() {
        MagentoAdditionalLibraryRootsProvider.refreshRoots(project);
        IndexManager.manualReindex();
        MagentoComponentManager.getInstance(project).flushModules();
    }

    private void reindexFromForm() {
        try {
            this.validator.validate();
            saveSettings();
            reindex();
            MagentoNotificationUtil.notifyGlobally(
                    project,
                    "Magento 2 and Adobe Commerce",
                    "Magento indexes rebuild was requested.",
                    NotificationType.INFORMATION
            );
        } catch (final ConfigurationException exception) {
            final String message = exception.getLocalizedMessage();
            Messages.showErrorDialog(
                    project,
                    message == null ? "Invalid Magento settings." : message,
                    "Magento Reindex"
            );
        }
    }

    @SuppressWarnings("unchecked")
    private void installMagentoSkill(final Skill skill) {
        final AgentTarget agentTarget = getSelectedSkillAgentTarget();

        try {
            if (MagentoSkillInstaller.hasDifferentExistingSkill(project, skill, agentTarget)
                    && !confirmSkillReplacement(skill, agentTarget)) {
                return;
            }

            final String installedPath = MagentoSkillInstaller.install(project, skill, agentTarget);
            MagentoNotificationUtil.notifyGlobally(
                    project,
                    "Magento 2 and Adobe Commerce",
                    skill.getDisplayName() + " skill was added for "
                            + agentTarget.getDisplayName() + " at " + installedPath,
                    NotificationType.INFORMATION
            );
        } catch (final IOException | IllegalStateException exception) {
            final String errorMessage = StringUtil.notNullize(
                    exception.getMessage(),
                    exception.getClass().getSimpleName()
            );
            MagentoNotificationUtil.notifyGlobally(
                    project,
                    "Magento 2 and Adobe Commerce",
                    "Unable to add " + skill.getDisplayName() + " skill for "
                            + agentTarget.getDisplayName() + ": "
                            + StringUtil.escapeXmlEntities(errorMessage),
                    NotificationType.WARNING
            );
        }
    }

    private AgentTarget getSelectedSkillAgentTarget() {
        final Object selectedItem = skillAgentTargetSelect.getSelectedItem();
        if (selectedItem instanceof AgentTarget) {
            return (AgentTarget) selectedItem;
        }

        return AgentTarget.PROJECT_SKILLS;
    }

    private boolean confirmSkillReplacement(final Skill skill, final AgentTarget agentTarget) {
        return Messages.showYesNoDialog(
                project,
                skill.getDisplayName()
                        + " skill already exists for " + agentTarget.getDisplayName()
                        + " in this project and has different content. "
                        + "Replace it with the bundled Magento skill?",
                "Replace Magento Skill",
                Messages.getQuestionIcon()
        ) == Messages.YES;
    }

    @Override
    public boolean isModified() {
        if (moduleDefaultLicenseName == null) {
            return true;
        }

        final boolean licenseChanged = !moduleDefaultLicenseName.getText().equals(
                getSettings().defaultLicense
        );
        final boolean versionChanged = !magentoVersion.getText().equals(
                getSettings().magentoVersion
        );
        final boolean statusChanged = !pluginEnabled.isSelected() == getSettings().pluginEnabled;
        final boolean mftfSupportChanged = mftfSupportEnabled.isSelected()
                != getSettings().mftfSupportEnabled;
        final boolean magentoPathChanged = isMagentoPathChanged();
        final boolean mcpCliToolCandidatesChanged = !Settings
                .getNormalizedMcpCliToolCandidates(mcpCliToolCandidates.getText())
                .equals(Settings.getNormalizedMcpCliToolCandidates(getSettings().mcpCliToolCandidates));

        return statusChanged || licenseChanged || mftfSupportChanged
                || magentoPathChanged || versionChanged || mcpCliToolCandidatesChanged;
    }

    private void resolveMagentoVersion() {
        if (getSettings().magentoVersion == null || getSettings().magentoEdition == null) {
            this.updateMagentoVersion();
        }
    }

    private boolean isMagentoPathChanged() {
        return !getMagentoPath().equals(
                StringUtil.notNullize(Settings.getMagentoPath(project))
        );
    }

    @Override
    public void apply() throws ConfigurationException {
        this.validator.validate();
        saveSettings();

        afterSettingsApplied(getSettings().getState());

        if (buttonReindex.isEnabled()) {
            reindex();
        }
    }

    protected void afterSettingsApplied(final @NotNull Settings.State state) {
        // PHP-specific settings integrations are added by PhpFrameworkSettingsForm.
    }

    private void saveSettings() {
        getSettings().pluginEnabled = pluginEnabled.isSelected();
        getSettings().defaultLicense = moduleDefaultLicenseName.getText();
        getSettings().mftfSupportEnabled = mftfSupportEnabled.isSelected();
        getSettings().setMagentoPath(getMagentoPath());
        getSettings().mcpCliToolCandidates = Settings.getNormalizedMcpCliToolCandidates(
                mcpCliToolCandidates.getText()
        );
        buttonReindex.setEnabled(getSettings().pluginEnabled);
        regenerateUrnMapButton.setEnabled(getSettings().pluginEnabled);
    }

    @NotNull
    public String getMagentoVersion() {
        return magentoVersion.getText().trim();
    }

    @NotNull
    public String getMagentoPath() {
        return StringUtil.notNullize(
                Settings.normalizeMagentoPath(magentoPath.getTextField().getText())
        );
    }

    @Override
    public void reset() {
        pluginEnabled.setSelected(getSettings().pluginEnabled);
        moduleDefaultLicenseName.setText(getSettings().defaultLicense);
        mftfSupportEnabled.setSelected(getSettings().mftfSupportEnabled);
        magentoPath.getTextField().setText(StringUtil.notNullize(Settings.getMagentoPath(project)));
        mcpCliToolCandidates.setText(
                Settings.getNormalizedMcpCliToolCandidates(getSettings().mcpCliToolCandidates)
        );
        refreshFormStatus(pluginEnabled.isSelected());
    }

    @Override
    public void disposeUIResources() {
        //do nothing
    }

    public Settings getSettings() {
        return Settings.getInstance(project);
    }

    private void addPathListener() {
        final FileChooserDescriptor descriptor =
                FileChooserDescriptorFactory.createSingleFolderDescriptor();
        this.magentoPath.addActionListener(event -> {
            final VirtualFile chosenFile = FileChooser.chooseFile(
                    descriptor,
                    project,
                    getInitialMagentoPath()
            );

            if (chosenFile != null) {
                this.magentoPath.setText(chosenFile.getPath());
            }
        });
    }

    @Nullable
    private VirtualFile getInitialMagentoPath() {
        final String text = getMagentoPath();
        if (!StringUtil.isEmptyOrSpaces(text)) {
            final VirtualFile currentFile = LocalFileSystem.getInstance().findFileByPath(text);
            if (currentFile != null) {
                return currentFile;
            }
        }

        return GetProjectBasePath.execute(project);
    }

    private void addMagentoVersionListener() {
        final DocumentListener onPathChange = new DocumentListener() {
            @Override
            public void insertUpdate(final DocumentEvent documentEvent) {
                updateMagentoVersion();
            }

            @Override
            public void removeUpdate(final DocumentEvent documentEvent) {
                updateMagentoVersion();
            }

            @Override
            public void changedUpdate(final DocumentEvent documentEvent) {
                updateMagentoVersion();
            }
        };
        this.magentoPath.getTextField().getDocument().addDocumentListener(onPathChange);
    }

    /**
     * Updates Magento version according to root composer.json.
     */
    public void updateMagentoVersion() {
        final String magentoPathValue = this.magentoPath.getTextField().getText();
        final Pair<String, String> version = MagentoVersionUtil.getVersionData(
                project,
                magentoPathValue
        );
        final String resolvedVersion = version.getFirst();
        final String resolvedEdition = version.getSecond() == null
                ? DEFAULT_MAGENTO_EDITION_LABEL
                : version.getSecond();
        magentoVersion.setText(resolvedVersion);
        magentoVersionLabel.setText(resolvedEdition);

        getSettings().magentoVersion = resolvedVersion;
        getSettings().magentoEdition = resolvedEdition;
    }

    public boolean isBeingUsed() {
        return isMagentoSupportEnabled();
    }

    protected boolean isMagentoSupportEnabled() {
        return this.pluginEnabled.isSelected();
    }

    protected @NotNull Project getProject() {
        return project;
    }

    @NotNull
    @Override
    public String getId() {
        return "Magento2.SettingsForm";
    }
}
