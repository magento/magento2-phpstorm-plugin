/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento;

import com.intellij.json.JsonFileType;
import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.magento.idea.magento2plugin.magento.files.ComposerLock;
import com.magento.idea.magento2plugin.magento.files.ComposerJson;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.project.util.GetMagentoVersionUtil;
import org.jetbrains.annotations.Nullable;

public final class MagentoVersionUtil {

    public static final String DEFAULT_VERSION = "any";

    private MagentoVersionUtil() {}

    /**
     * Detect Magento 2 version using the shared project detection.
     *
     * @param project Project
     * @param magentoPath String
     * @return String
     */
    public static String get(final Project project, final String magentoPath) {
        final Pair<String, String> version = getVersionData(
                project,
                magentoPath
        );

        return version.getFirst();
    }

    /**
     * Detect source checkouts from root metadata, or installed products from composer.lock.
     *
     * @param project Project
     * @param magentoPath String
     *
     * @return Pair[String, String]
     */
    public static Pair<String, String> getVersionData(
            final Project project,
            final String magentoPath
    ) {
        final Pair<String, String> sourceVersion = getSourceVersionData(project, magentoPath);
        return sourceVersion == null ? getLockedVersionData(project, magentoPath) : sourceVersion;
    }

    /**
     * Source checkouts keep core code in app/code and lib/internal. A regular project's
     * composer.json version is not an installed product version and must not be used as a fallback.
     */
    public static @Nullable Pair<String, String> getSourceVersionData(
            final Project project,
            final String magentoPath
    ) {
        return ApplicationManager.getApplication().runReadAction((Computable<Pair<String, String>>) () -> {
            final VirtualFile root = LocalFileSystem.getInstance().findFileByPath(magentoPath);
            if (root == null || root.findFileByRelativePath(Package.frameworkRootGit) == null
                    || root.findFileByRelativePath(Package.packagesRoot + "/Magento") == null) {
                return null;
            }
            final VirtualFile file = root.findChild(ComposerJson.FILE_NAME);
            final PsiFile psiFile = file == null ? null : PsiManager.getInstance(project).findFile(file);
            if (!(psiFile instanceof JsonFile)) {
                return null;
            }
            final JsonObject object = PsiTreeUtil.getChildOfType(psiFile, JsonObject.class);
            final Pair<String, String> version = object == null ? null : GetMagentoVersionUtil.getSourceVersion(object);
            if (version == null) {
                return null;
            }
            return Pair.create(version.first == null || version.first.isBlank() ? DEFAULT_VERSION : version.first, version.second);
        });
    }

    /** Read only the installed lock-file version, for checks which require a released package baseline. */
    public static Pair<String, String> getLockedVersionData(final Project project, final String magentoPath) {
        return ApplicationManager.getApplication().runReadAction((Computable<Pair<String, String>>) () -> {
            final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(
                    getFilePath(magentoPath)
            );
            final Pair<String, String> versionData = new Pair<>(DEFAULT_VERSION, null);

            if (file == null) {
                return versionData;
            }
            final PsiManager psiManager = PsiManager.getInstance(project);
            PsiFile composerFile = psiManager.findFile(file);
            // PHP-free IDEs may associate composer.lock with TextMate or plain text.
            // Use the platform JSON PSI parser without changing the user's file associations.
            if (composerFile != null && !(composerFile instanceof JsonFile)) {
                composerFile = PsiFileFactory.getInstance(project).createFileFromText(
                        ComposerLock.FILE_NAME, JsonFileType.INSTANCE, composerFile.getText()
                );
            }

            if (composerFile instanceof JsonFile) {
                final JsonFile composerJsonFile = (JsonFile) composerFile;
                final JsonObject jsonObject = PsiTreeUtil.getChildOfType(
                        composerJsonFile,
                        JsonObject.class
                );

                if (jsonObject == null) {
                    return versionData;
                }
                final Pair<String, String> version = GetMagentoVersionUtil.getVersion(jsonObject);

                return version == null ? versionData : version;
            }

            return versionData;
        });
    }

    private static String getFilePath(final String magentoPath) {
        return magentoPath + File.separator + ComposerLock.FILE_NAME;
    }

    /**
     * Compare two magento version.
     *
     * @param version1 String
     * @param version2 String
     * @return the value {@code true} if the argument version1 is greater than equal to version2;
     *         the value {@code false} if the argument version1 is less than to version2.
     */
    public static boolean compare(final String version1, final String version2) {
        if (DEFAULT_VERSION.equals(version1)) {
            return true;
        }
        if (version1.equals(version2)) {
            return true;
        }

        if (version1.isEmpty()) {
            return false;
        }

        final String[] version1s = version1.split("\\.");
        final String[] version2s = version2.split("\\.");
        for (int i = 0; i < 2; i++) {
            final int value1 = Integer.parseInt(version1s[i]);
            final int value2 = Integer.parseInt(version2s[i]);
            if (value1 > value2) {
                return true;
            }
        }
        return false;
    }
}
