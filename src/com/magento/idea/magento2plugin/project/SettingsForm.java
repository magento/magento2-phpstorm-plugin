/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.project;

import com.intellij.javaee.ExternalResourceManager;
import com.intellij.javaee.ExternalResourceManagerEx;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.jetbrains.php.frameworks.PhpFrameworkConfigurable;
import com.magento.idea.magento2plugin.actions.generation.util.MagentoVersion;
import com.magento.idea.magento2plugin.indexes.IndexManager;
import com.magento.idea.magento2plugin.init.ConfigurationManager;
import com.magento.idea.magento2plugin.php.module.*;
import com.magento.idea.magento2plugin.util.magento.MagentoBasePathUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Stack;

public class SettingsForm implements PhpFrameworkConfigurable {

    private final static String DISPLAY_NAME = "Magento";
    private final Project project;
    private JCheckBox pluginEnabled;
    private JButton buttonReindex;
    private JPanel jPanel;
    private JButton regenerateUrnMapButton;
    private JLabel magentoVersion;
    private JTextField moduleDefaultLicenseName;
    private JCheckBox mftfSupportEnabled;
    private JLabel magentoPathLabel;
    private TextFieldWithBrowseButton magentoPath;
    private MagentoVersion magentoVersionModel = MagentoVersion.getInstance();

    public SettingsForm(@NotNull final Project project) {
        this.project = project;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return SettingsForm.DISPLAY_NAME;
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        buttonReindex.addMouseListener(
            new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    reindex();
                    super.mouseClicked(e);
                }
            }
        );

        buttonReindex.setEnabled(getSettings().pluginEnabled);
        regenerateUrnMapButton.setEnabled(getSettings().pluginEnabled);

        regenerateUrnMapButton.addMouseListener(
            new RegenerateUrnMapListener(project)
        );

        String version = magentoVersionModel.get();
        if (version != null) {
            magentoVersion.setText("Magento version: " . concat(version));
        }

        moduleDefaultLicenseName.setText(getSettings().DEFAULT_LICENSE);
        mftfSupportEnabled.setSelected(getSettings().mftfSupportEnabled);
        magentoPath.getTextField().setText(getSettings().magentoPath);
        addPathListener();

        return (JComponent) jPanel;
    }

    private void reindex() {
        IndexManager.manualReindex();
        MagentoComponentManager.getInstance(project).flushModules();
    }

    @Override
    public boolean isModified() {
        boolean licenseChanged = !moduleDefaultLicenseName.getText().equals(getSettings().DEFAULT_LICENSE);
        boolean statusChanged = !pluginEnabled.isSelected() == getSettings().pluginEnabled;
        boolean mftfSupportChanged = mftfSupportEnabled.isSelected() != getSettings().mftfSupportEnabled;
        boolean magentoPathChanged = isMagentoPathChanged();

        return statusChanged || licenseChanged || mftfSupportChanged || magentoPathChanged;
    }

    private boolean isMagentoPathChanged() {
        return !magentoPath.getTextField().getText().equals(getSettings().magentoPath);
    }

    @Override
    public void apply() throws ConfigurationException {
        getSettings().pluginEnabled = pluginEnabled.isSelected();
        getSettings().DEFAULT_LICENSE = moduleDefaultLicenseName.getText();
        getSettings().mftfSupportEnabled = mftfSupportEnabled.isSelected();
        getSettings().magentoPath = magentoPath.getTextField().getText().trim();
        buttonReindex.setEnabled(getSettings().pluginEnabled);
        regenerateUrnMapButton.setEnabled(getSettings().pluginEnabled);

        if (getSettings().pluginEnabled && !MagentoBasePathUtil.isMagentoFolderValid(getSettings().magentoPath)) {
            throw new ConfigurationException("Please specify valid magento installation path!");
        }
        ConfigurationManager.getInstance().refreshIncludePaths(getSettings().getState(), project);

        if (buttonReindex.isEnabled()) {
            reindex();
        }
    }

    @Override
    public void reset() {
        pluginEnabled.setSelected(getSettings().pluginEnabled);
    }

    @Override
    public void disposeUIResources() {

    }

    private Settings getSettings() {
        return Settings.getInstance(project);
    }

    private void addPathListener() {
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        ComponentWithBrowseButton.BrowseFolderActionListener<JTextField> browseFolderListener = new ComponentWithBrowseButton.BrowseFolderActionListener<JTextField>(
                "Magento Root Directory",
                "Choose Magento root directory",
                this.magentoPath,
                null,
                descriptor,
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
        ) {
            @Nullable
            protected VirtualFile getInitialFile() {
                return super.getInitialFile();
            }
        };
        this.magentoPath.addActionListener(browseFolderListener);
    }

    @Override
    public boolean isBeingUsed() {
        return this.pluginEnabled.isSelected();
    }

    @NotNull
    @Override
    public String getId() {
        return "Magento2.SettingsForm";
    }
}

class RegenerateUrnMapListener extends MouseAdapter {
    private final static String MODULE = "urn:magento:module:";
    private final static String FRAMEWORK = "urn:magento:framework:";

    private Project project;

    public RegenerateUrnMapListener(@NotNull Project project) {
        this.project = project;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        ApplicationManager.getApplication().runWriteAction(
            new Runnable() {
                @Override
                public void run() {
                    ExternalResourceManager externalResourceManager = ExternalResourceManager.getInstance();
                    PsiManager psiManager = PsiManager.getInstance(project);
                    MagentoComponentManager componentManager = MagentoComponentManager.getInstance(project);

                    Collection<VirtualFile> xsdFiles = FilenameIndex.getAllFilesByExt(project, "xsd");
                    Collection<MagentoComponent> components = componentManager.getAllComponents();

                    for (VirtualFile virtualFile: xsdFiles) {
                        PsiFile psiFile = psiManager.findFile(virtualFile);
                        if (psiFile == null) {
                            continue;
                        }

                        MagentoComponent xsdOwner = findComponentForXsd(psiFile, components);
                        if (xsdOwner == null) {
                            continue;
                        }

                        String urnKey = buildUrnKeyForFile(psiFile, xsdOwner);
                        if (urnKey == null) {
                            continue;
                        }

                        // we need to attach resource to a project scope
                        // but with ExternalResourceManager itself it's not possible unfortunately
                        if (externalResourceManager instanceof ExternalResourceManagerEx) {
                            ((ExternalResourceManagerEx)externalResourceManager).addResource(
                                urnKey, virtualFile.getCanonicalPath(), project
                            );
                        } else {
                            externalResourceManager.addResource(urnKey, virtualFile.getCanonicalPath());
                        }
                    }
                }
            }
        );

        super.mouseClicked(e);
    }

    @Nullable
    private MagentoComponent findComponentForXsd(@NotNull PsiFile psiFile, Collection<MagentoComponent> components) {
        for (MagentoComponent component: components) {
            if (component.isFileInContext(psiFile)) {
                return component;
            }
        }

        return null;
    }

    @Nullable
    private String buildUrnKeyForFile(@NotNull PsiFile psiFile, @NotNull MagentoComponent magentoComponent) {
        String prefix = null;

        if (magentoComponent instanceof MagentoModule) {
            prefix = MODULE + ((MagentoModule)magentoComponent).getMagentoName() + ":";
        } else {
            ComposerPackageModel composerPackageModel = magentoComponent.getComposerModel();
            if ("magento2-library".equals(composerPackageModel.getType())) {
                prefix = FRAMEWORK;
            }
        }

        if (prefix == null) {
            return null;
        }

        Stack<String> relativePath = new Stack<>();
        relativePath.push(psiFile.getName());

        PsiManager psiManager = magentoComponent.getDirectory().getManager();
        PsiDirectory parentDir = psiFile.getParent();
        while (parentDir != null && !psiManager.areElementsEquivalent(parentDir, magentoComponent.getDirectory())) {
            relativePath.push("/");
            relativePath.push(parentDir.getName());
            parentDir = parentDir.getParentDirectory();
        }

        StringBuilder stringBuilder = new StringBuilder(prefix);
        while (!relativePath.empty()) {
            stringBuilder.append(relativePath.pop());
        }

        return stringBuilder.toString();
    }
}
