/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions;

import com.intellij.ide.actions.CopyPathProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CopyMagentoPath extends CopyPathProvider {

    public static final String PHTML_EXTENSION = "phtml";
    public static final String JS_EXTENSION = "js";
    public static final String CSS_EXTENSION = "css";
    public static final String HTML_EXTENSION = "html";
    private static final List<String> SUPPORTED_IMAGE_EXTENSIONS
            = new ArrayList<>(Arrays.asList(ImageIO.getReaderFormatNames()));
    public static final String SEPARATOR = "::";
    private int index;

    private final String[] templatePaths = {
        "view/frontend/templates/",
        "view/adminhtml/templates/",
        "view/base/templates/",
        "templates/"
    };

    private final String[] webPaths = {
        "view/frontend/web/",
        "view/adminhtml/web/",
        "view/base/web/",
        "web/"
    };

    /**
     * Copy Magento Path actions for phtml, css, js, images extensions.
     */
    public CopyMagentoPath() {
        super();

        SUPPORTED_IMAGE_EXTENSIONS.add("svg");
    }

    @Override
    public @Nullable String getPathToElement(
            final @NotNull Project project,
            final @Nullable VirtualFile virtualFile,
            final @Nullable Editor editor
    ) {
        if (virtualFile == null) {
            return null;
        }
        final PsiFile file = PsiManager.getInstance(project).findFile(virtualFile);

        if (file == null) {
            return null;
        }
        final PsiDirectory directory = file.getContainingDirectory();
        if (directory == null) {
            return null;
        }
        final String moduleName = GetModuleNameByDirectoryUtil.execute(directory, project);

        if (moduleName == null) {
            return null;
        }
        final StringBuilder fullPath = new StringBuilder(virtualFile.getPath());

        index = -1;
        final String[] paths;

        if (PHTML_EXTENSION.equals(virtualFile.getExtension())) {
            paths = templatePaths;
        } else if (isMagentoFile(virtualFile)) {
            paths = webPaths;
        } else {
            return "";
        }

        try {
            return getResultPath(virtualFile, paths, fullPath, moduleName);
        } catch (ArrayIndexOutOfBoundsException exception) {
            return "";
        }
    }

    /**
     * Determines if the provided file is supported by Magento Path.
     *
     * @param virtualFile the virtual file to be checked
     * @return bool
     */
    private static boolean isMagentoFile(@NotNull final VirtualFile virtualFile) {
        return JS_EXTENSION.equals(virtualFile.getExtension())
                || CSS_EXTENSION.equals(virtualFile.getExtension())
                || HTML_EXTENSION.equals(virtualFile.getExtension())
                || SUPPORTED_IMAGE_EXTENSIONS.contains(virtualFile.getExtension());
    }

    /**
     * Constructs a result.
     *
     * @param virtualFile the virtual file being processed
     * @param paths an array of potential path segments to be checked
     * @param fullPath the full path of the virtual file as a mutable string builder
     * @param moduleName the name of the module associated with the file
     * @return the constructed result path
     */
    private @NotNull String getResultPath(
            @NotNull final VirtualFile virtualFile,
            final String[] paths,
            final StringBuilder fullPath,
            final String moduleName
    ) {
        final int endIndex = getIndexOf(paths, fullPath, paths[++index]);
        final int offset = paths[index].length();

        fullPath.replace(0, endIndex + offset, "");

        return PHTML_EXTENSION.equals(virtualFile.getExtension())
                ? moduleName + SEPARATOR + fullPath
                : moduleName + "/" + fullPath.substring(0, fullPath.lastIndexOf("."));
    }

    /**
     * Get index where web|template path starts in the fullPath.
     *
     * @param paths String[]
     * @param fullPath StringBuilder
     * @param path String
     *
     * @return int
     */
    private int getIndexOf(final String[] paths, final StringBuilder fullPath, final String path) {
        return fullPath.lastIndexOf(path) == -1
                ? getIndexOf(paths, fullPath, paths[++index])
                : fullPath.lastIndexOf(path);
    }
}
