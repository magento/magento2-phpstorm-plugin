/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class MagentoSkillInstaller {

    private MagentoSkillInstaller() {
    }

    public enum Skill {
        MAGENTO_SCAFFOLD("Magento scaffold", "magento-scaffold"),
        MAGENTO_INSPECT("Magento inspect", "magento-inspect");

        private final String displayName;
        private final String directoryName;

        Skill(final String displayName, final String directoryName) {
            this.displayName = displayName;
            this.directoryName = directoryName;
        }

        public String getDisplayName() {
            return displayName;
        }

        private String getResourcePath() {
            return "/skills/" + directoryName + "/SKILL.md";
        }

        private String getProjectRelativePath() {
            return "skills/" + directoryName + "/SKILL.md";
        }
    }

    /**
     * Checks whether the project already has a customized copy of the skill.
     *
     * @param project current project
     * @param skill skill to check
     * @return true if the file exists and differs from the bundled skill
     * @throws IOException when the existing file cannot be read
     */
    public static boolean hasDifferentExistingSkill(
            @NotNull final Project project,
            @NotNull final Skill skill
    ) throws IOException {
        final Path targetPath = getTargetPath(project, skill);

        return Files.exists(targetPath)
                && !Objects.equals(Files.readString(targetPath), readBundledSkill(skill));
    }

    /**
     * Installs or updates a bundled Magento agent skill in the current project.
     *
     * @param project current project
     * @param skill skill to install
     * @return absolute path of the installed skill file
     * @throws IOException when the skill cannot be written
     */
    public static @NotNull String install(
            @NotNull final Project project,
            @NotNull final Skill skill
    ) throws IOException {
        final Path targetPath = getTargetPath(project, skill);
        final String skillContent = readBundledSkill(skill);

        try {
            ApplicationManager.getApplication().runWriteAction(() -> {
                try {
                    Files.createDirectories(targetPath.getParent());
                    Files.writeString(targetPath, skillContent, StandardCharsets.UTF_8);
                    LocalFileSystem.getInstance().refreshAndFindFileByNioFile(targetPath);
                } catch (final IOException exception) {
                    throw new UncheckedIOException(exception);
                }
            });
        } catch (final UncheckedIOException exception) {
            throw exception.getCause();
        }

        return targetPath.toString();
    }

    private static @NotNull Path getTargetPath(
            @NotNull final Project project,
            @NotNull final Skill skill
    ) {
        final VirtualFile projectDir = ProjectUtil.guessProjectDir(project);
        final String basePath = projectDir == null ? project.getBasePath() : projectDir.getPath();

        if (basePath == null) {
            throw new IllegalStateException("Unable to resolve project directory.");
        }

        return Path.of(basePath).resolve(skill.getProjectRelativePath());
    }

    private static @NotNull String readBundledSkill(@NotNull final Skill skill) throws IOException {
        try (InputStream stream = MagentoSkillInstaller.class.getResourceAsStream(
                skill.getResourcePath()
        )) {
            if (stream == null) {
                throw new IOException("Bundled skill resource was not found: "
                        + skill.getResourcePath());
            }

            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
