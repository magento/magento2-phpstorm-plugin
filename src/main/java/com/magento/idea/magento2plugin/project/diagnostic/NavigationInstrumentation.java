/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project.diagnostic;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.project.Settings;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NavigationInstrumentation {
    private static final Logger LOGGER = Logger.getInstance(NavigationInstrumentation.class);
    private static final Set<String> LOGGED_KEYS = ConcurrentHashMap.newKeySet();
    private static final String PREFIX = "[Magento navigation] ";

    private NavigationInstrumentation() {
    }

    public static void infoOnce(
            final @NotNull String key,
            final @NotNull Supplier<String> message
    ) {
        if (LOGGED_KEYS.add(key)) {
            LOGGER.info(PREFIX + message.get());
        }
    }

    public static void info(final @NotNull String message) {
        LOGGER.info(PREFIX + message);
    }

    public static @NotNull String describeSettings(final @NotNull Project project) {
        return "enabled=" + Settings.isEnabled(project)
                + ", magentoPath=" + Settings.getMagentoPath(project);
    }

    public static @NotNull String describeFile(final @Nullable PsiFile psiFile) {
        if (psiFile == null) {
            return "<no psi file>";
        }
        final VirtualFile virtualFile = psiFile.getVirtualFile();

        if (virtualFile == null) {
            return psiFile.getName() + " (<no virtual file>)";
        }

        return virtualFile.getPath() + " [type=" + virtualFile.getFileType().getName() + "]";
    }

    public static @NotNull String describeElement(final @NotNull PsiElement element) {
        return element.getClass().getSimpleName()
                + " text=" + truncate(element.getText())
                + " file=" + describeFile(element.getContainingFile());
    }

    private static @NotNull String truncate(final @Nullable String text) {
        if (text == null) {
            return "<null>";
        }
        final String singleLine = text.replace('\n', ' ').replace('\r', ' ');

        return singleLine.length() > 120 ? singleLine.substring(0, 120) + "..." : singleLine;
    }
}
