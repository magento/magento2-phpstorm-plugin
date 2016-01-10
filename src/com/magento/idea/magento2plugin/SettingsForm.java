package com.magento.idea.magento2plugin;

import com.intellij.javaee.ExternalResourceManager;
import com.intellij.javaee.ExternalResourceManagerEx;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.magento.idea.magento2plugin.php.module.ComposerPackageModel;
import com.magento.idea.magento2plugin.php.module.MagentoComponent;
import com.magento.idea.magento2plugin.php.module.MagentoComponentManager;
import com.magento.idea.magento2plugin.php.module.MagentoModule;
import com.magento.idea.magento2plugin.util.IndexUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

/**
 * Created by dkvashnin on 1/9/16.
 */
public class SettingsForm implements Configurable {
    private Project project;
    private JCheckBox pluginEnabled;
    private JButton buttonReindex;
    private JPanel panel1;
    private JButton regenerateUrnMapButton;

    public SettingsForm(@NotNull final Project project) {
        this.project = project;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Magento2 plugin";
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
        return (JComponent) panel1;
    }

    private void reindex() {
        IndexUtil.manualReindex();
        MagentoComponentManager.getInstance(project).flushModules();
    }

    @Override
    public boolean isModified() {
        return !pluginEnabled.isSelected() == getSettings().pluginEnabled;
    }

    @Override
    public void apply() throws ConfigurationException {
        getSettings().pluginEnabled = pluginEnabled.isSelected();
        buttonReindex.setEnabled(getSettings().pluginEnabled);
        regenerateUrnMapButton.setEnabled(getSettings().pluginEnabled);

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
