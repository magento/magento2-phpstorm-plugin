package com.magento.idea.magento2plugin.init;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PlatformUtils;
import com.jetbrains.php.config.library.PhpIncludePathManager;
import com.jetbrains.php.ui.PhpUiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class ConfigurationManager {
    private static final ConfigurationManager INSTANCE = new ConfigurationManager();

    public static ConfigurationManager getInstance() {
        return INSTANCE;
    }

    private ConfigurationManager() {
    }

    public void refreshIncludePaths(Settings.State newState, Project project) {
        if (!project.isDefault() && newState.isPluginEnabled() && !newState.isDoNotAskContentConfigAgain()) {
            VirtualFile magentoFile = getMagentoFile(newState);
            if (magentoFile == null || VfsUtilCore.isAncestor(project.getBaseDir(), magentoFile, false)) {
                return;
            }

            if (VfsUtilCore.isAncestor(magentoFile, project.getBaseDir(), false)) {
                Module[] modules = ModuleManager.getInstance(project).getModules();
                if (modules.length == 1) {
                    Module module = modules[0];
                    if (!isFileInsideModule(magentoFile, module)) {
                        suggestToChangeContentRoots(magentoFile, project, module);
                    }
                }
            } else if (!isInIncludePath(magentoFile, project)) {
                suggestToAddToIncludePath(magentoFile, project);
            }
        }
    }

    private static void suggestToChangeContentRoots(@NotNull VirtualFile magentoFile, @NotNull Project project, @NotNull Module module) {
        String message = "For Magento 2 containing plugins, it's better to add whole Magento 2 to the project.";
        showPopupWithActions(project, message, 
            createFixAction(() -> updateContentRoots(magentoFile, project, module)),
            createIgnoreAction(() -> Settings.getInstance(project).setDoNotAskContentConfigurationAgain(true))
        );
    }

    private static void suggestToAddToIncludePath(@NotNull VirtualFile magentoFile, @NotNull Project project) {
        String message = "Magento installation is not added to PHP | Include path";
        showPopupWithActions(project, message,
            createFixAction(() -> addToIncludePath(magentoFile, project)),
            createIgnoreAction(() -> Settings.getInstance(project).setDoNotAskContentConfigurationAgain(true)),
            createShowSettingsAction(() -> PhpUiUtil.editConfigurable(project, new PhpProjectConfigurable(project)))
        );
    }

    public static void suggestToConfigureMagentoPath(@NotNull Project project) {
        String message = "Magento 2 support is disabled. Please configure Magento 2 installation path.";
        showPopupWithActions(project, message, createShowSettingsAction(() -> 
            ShowSettingsUtil.getInstance().showSettingsDialog(project, "Magento2.SettingsForm"))
        );
    }

    private static boolean isInIncludePath(@NotNull VirtualFile fileToCheck, @NotNull Project project) {
        List<VirtualFile> includePaths = PhpIncludePathManager.getInstance(project).getAllIncludedRoots();
        return includePaths.stream().anyMatch(path -> VfsUtilCore.isAncestor(path, fileToCheck, false));
    }

    @Nullable
    private static VirtualFile getMagentoFile(Settings.State state) {
        String path = state.getMagentoPath();
        if (StringUtil.isEmpty(path)) {
            return null;
        }
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(path);
        return file != null && file.isDirectory() ? file : null;
    }

    private static boolean isFileInsideModule(@NotNull VirtualFile magentoFile, @NotNull Module module) {
        VirtualFile[] contentRoots = ModuleRootManager.getInstance(module).getContentRoots();
        return contentRoots != null && VfsUtilCore.isAncestor(contentRoots[0], magentoFile, false);
    }

    private static void updateContentRoots(@NotNull VirtualFile magentoFile, @NotNull Project project, @NotNull Module module) {
        ModuleRootModificationUtil.updateModel(module, model -> {
            ContentEntry rootEntry = findRootEntry(model, project.getBaseDir());
            if (rootEntry != null) {
                VirtualFile rootEntryFile = rootEntry.getFile();
                if (rootEntryFile == null || !VfsUtilCore.isAncestor(rootEntryFile, magentoFile, false)) {
                    model.addContentEntry(magentoFile);
                    model.removeContentEntry(rootEntry);
                }
            } else {
                model.addContentEntry(magentoFile);
            }
        });
    }

    private static ContentEntry findRootEntry(ModuleRootManager.Model model, VirtualFile baseDir) {
        for (ContentEntry entry : model.getContentEntries()) {
            VirtualFile entryFile = entry.getFile();
            if (entryFile != null && VfsUtilCore.isAncestor(entryFile, baseDir, false)) {
                return entry;
            }
        }
        return null;
    }

    private static void addToIncludePath(@NotNull VirtualFile magentoFile, @NotNull Project project) {
        PhpIncludePathManager facade = PhpIncludePathManager.getInstance(project);
        List<String> includePaths = new ArrayList<>(facade.getIncludePath());
        includePaths.add(magentoFile.getPath());
        facade.setIncludePath(includePaths);
    }

    private static void showPopupWithActions(Project project, String message, Function<Notification, AnAction>... actions) {
        ApplicationManager.getApplication().invokeLater(() -> notifyGlobally(project, "Magento 2 Support", message, NotificationType.INFORMATION, actions));
    }

    private static Function<Notification, AnAction> createFixAction(Runnable action) {
        return notification -> new DumbAwareAction("Fix") {
            public void actionPerformed(@NotNull AnActionEvent e) {
                notification.expire();
                action.run();
            }
        };
    }

    private static Function<Notification, AnAction> createIgnoreAction(Runnable action) {
        return notification -> new DumbAwareAction("Ignore") {
            public void actionPerformed(@NotNull AnActionEvent e) {
                notification.expire();
                action.run();
            }
        };
    }

    private static Function<Notification, AnAction> createShowSettingsAction(Runnable action) {
        return notification -> new DumbAwareAction("Show settings") {
            public void actionPerformed(@NotNull AnActionEvent e) {
                notification.expire();
                action.run();
            }
        };
    }

    public static void notifyGlobally(@Nullable Project project, String title, String message, NotificationType notificationType, Function<Notification, AnAction>... actions) {
        Notification notification = new Notification("Magento 2 Support", title, message, notificationType);
        for (Function<Notification, AnAction> generator : actions) {
            notification.addAction(generator.apply(notification));
        }
        notification.notify(project);
    }
}
