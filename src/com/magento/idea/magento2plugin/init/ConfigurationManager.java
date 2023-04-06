/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
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
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PlatformUtils;
import com.jetbrains.php.config.PhpProjectConfigurable;
import com.jetbrains.php.config.library.PhpIncludePathManager;
import com.jetbrains.php.ui.PhpUiUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import com.magento.idea.magento2plugin.project.Settings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
            if (magentoFile == null) {
                return;
            }

            if (VfsUtilCore.isAncestor(project.getBaseDir(), magentoFile, false)) {
                return;
            }

            boolean isModuleInsideMagento = VfsUtilCore.isAncestor(magentoFile, project.getBaseDir(), false);
            if (isModuleInsideMagento) {
                Module[] modules = ModuleManager.getInstance(project).getModules();
                if (modules.length == 1) {
                    Module module = modules[0];
                    boolean isMagentoIncluded = isFileInsideModule(magentoFile, module);
                    if (!isMagentoIncluded) {
                        suggestToChangeContentRoots(magentoFile, project, module);
                    }
                }
            } else {
                boolean isMagentoIncluded = isInIncludePath(magentoFile, project);
                if (!isMagentoIncluded) {
                    suggestToAddToIncludePath(magentoFile, project);
                }
            }
        }

    }

    private static void suggestToChangeContentRoots(@NotNull VirtualFile magentoFile, @NotNull Project project, @NotNull Module module) {
        String message = "For Magento 2 containing plugins inside it's better to add whole Magento 2 to project.";
        Function<Notification, AnAction> fixAction = (notification) -> {
            return new DumbAwareAction("Fix") {
                public void actionPerformed(@NotNull AnActionEvent e) {
                    notification.expire();
                    ModuleRootModificationUtil.updateModel(module, (model) -> {
                        ContentEntry[] contentEntries = model.getContentEntries();
                        ContentEntry rootEntry = null;
                        ContentEntry[] entries = contentEntries;
                        int length = contentEntries.length;

                        for (int i = 0; i < length; ++i) {
                            ContentEntry entry = entries[i];
                            VirtualFile entryFile = entry.getFile();
                            if (entryFile != null && VfsUtilCore.isAncestor(entryFile, project.getBaseDir(), false)) {
                                rootEntry = entry;
                                break;
                            }
                        }

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
                    if (PlatformUtils.isPhpStorm()) {
                        Runnable runnable = () -> {
                            ShowSettingsUtil.getInstance().showSettingsDialog(project, "Directories");
                        };
                        ApplicationManager.getApplication().invokeLater(runnable, ModalityState.NON_MODAL);
                    }
                }
            };
        };
        Function<Notification, AnAction> ignoreAction = (notification) -> {
            return new DumbAwareAction("Ignore") {
                public void actionPerformed(@NotNull AnActionEvent e) {
                    notification.expire();
                    Settings.getInstance(project).setDoNotAskContentConfigurationAgain(true);
                }
            };
        };
        showPopup(project, message, fixAction, ignoreAction);
    }

    private static void suggestToAddToIncludePath(@NotNull VirtualFile magentoFile, @NotNull Project project) {
        String message = "Magento installation is not added to PHP | Include path";
        Function<Notification, AnAction> fixAction = (notification) -> {
            return new DumbAwareAction("Fix") {
                public void actionPerformed(@NotNull AnActionEvent e) {
                    notification.expire();
                    Runnable runnable = () -> {
                        PhpIncludePathManager facade = PhpIncludePathManager.getInstance(project);
                        List<String> includePaths = facade.getIncludePath();
                        List<String> newIncludePaths = new ArrayList(includePaths.size() + 1);
                        newIncludePaths.addAll(includePaths);
                        String path = magentoFile.getPath();
                        newIncludePaths.add(path);
                        facade.setIncludePath(newIncludePaths);
                        ApplicationManager.getApplication().invokeLater(() -> {
                            if (!project.isDisposed()) {
                                PhpUiUtil.editConfigurable(project, new PhpProjectConfigurable(project));
                            }
                        });
                    };
                    ApplicationManager.getApplication().runWriteAction(runnable);
                }
            };
        };
        Function<Notification, AnAction> ignoreAction = (notification) -> {
            return new DumbAwareAction("Ignore") {
                public void actionPerformed(@NotNull AnActionEvent e) {
                    notification.expire();
                    Settings.getInstance(project).setDoNotAskContentConfigurationAgain(true);
                }
            };
        };
        Function<Notification, AnAction> showSettingsAction = (notification) -> {
            return new DumbAwareAction("Show settings") {
                public void actionPerformed(@NotNull AnActionEvent e) {
                    notification.expire();
                    PhpUiUtil.editConfigurable(project, new PhpProjectConfigurable(project));
                }
            };
        };
        showPopup(project, message, fixAction, ignoreAction, showSettingsAction);
    }

    public static void suggestToConfigureMagentoPath(@NotNull Project project) {
        String message = "Magento 2 support is disabled. Please configure Magento 2 installation path.";
        Function<Notification, AnAction> showSettingsAction = (notification) -> {
            return new DumbAwareAction("Show settings") {
                public void actionPerformed(@NotNull AnActionEvent e) {
                    notification.expire();
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, "Magento2.SettingsForm");
                }
            };
        };
        showPopup(project, message, showSettingsAction);
    }

    private static boolean isInIncludePath(@NotNull VirtualFile fileToCheck, @NotNull Project project) {
        List<VirtualFile> includePaths = PhpIncludePathManager.getInstance(project).getAllIncludedRoots();
        Iterator iterator = includePaths.iterator();

        VirtualFile file;
        do {
            if (!iterator.hasNext()) {
                return false;
            }

            file = (VirtualFile)iterator.next();
        } while(file == null || !VfsUtilCore.isAncestor(file, fileToCheck, false));

        return true;
    }

    @Nullable
    private static VirtualFile getMagentoFile(Settings.State state) {
        String path = state.getMagentoPath();
        if (StringUtil.isEmpty(path)) {
            return null;
        } else {
            VirtualFile file = LocalFileSystem.getInstance().findFileByPath(path);
            return file != null && file.isDirectory() ? file : null;
        }
    }

    private static boolean isFileInsideModule(@NotNull VirtualFile magentoFile, @NotNull Module module) {
        VirtualFile[] contentRoots = ModuleRootManager.getInstance(module).getContentRoots();
        VirtualFile[] roots = contentRoots;
        int length = contentRoots.length;

        for (int i = 0; i < length; ++i) {
            VirtualFile root = roots[i];
            if (VfsUtilCore.isAncestor(root, magentoFile, false)) {
                return true;
            }
        }

        return false;
    }

    private static void showPopup(Project project, String message, Function<Notification, AnAction>... actions) {
        Runnable runnable = () -> {
            notifyGlobally(project, "Magento 2 Support", message, NotificationType.INFORMATION, actions);
        };
        ApplicationManager.getApplication().invokeLater(runnable, ModalityState.NON_MODAL);
    }

    public static void notifyGlobally(@Nullable Project project, String title, String message, NotificationType notificationType, Function<Notification, AnAction>... actions) {
        Notification notification = new Notification("Magento 2 Support", title, message, notificationType);
        Function[] functions = actions;
        int length = actions.length;

        for (int i = 0; i < length; ++i) {
            Function<Notification, AnAction> generator = functions[i];
            notification.addAction(generator.apply(notification));
        }

        notification.notify(project);
    }
}
