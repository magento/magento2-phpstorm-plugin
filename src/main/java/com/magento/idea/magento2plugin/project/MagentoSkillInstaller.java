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
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public final class MagentoSkillInstaller {

    private MagentoSkillInstaller() {
    }

    public enum Skill {
        MAGENTO_SCAFFOLD("Magento scaffold", "magento-scaffold"),
        MAGENTO_INSPECT("Magento inspect", "magento-inspect"),
        MAGENTO_MAGERUN("Magento Magerun", "magento-magerun");

        private final String displayName;
        private final String directoryName;

        Skill(final String displayName, final String directoryName) {
            this.displayName = displayName;
            this.directoryName = directoryName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDirectoryName() {
            return directoryName;
        }
    }

    public enum AgentTarget {
        PROJECT_SKILLS("Project", "skills"),
        AGENTS("Agents", ".agents/skills"),
        CODEX("Codex", ".codex/skills"),
        CLAUDE_CODE("Claude", ".claude/skills");

        private final String displayName;
        private final String skillRoot;

        AgentTarget(final String displayName, final String skillRoot) {
            this.displayName = displayName;
            this.skillRoot = skillRoot;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getProjectRelativePath(@NotNull final Skill skill) {
            return skillRoot + "/" + skill.directoryName + "/SKILL.md";
        }

        @Override
        public String toString() {
            return displayName;
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
            @NotNull final Skill skill,
            @NotNull final AgentTarget agentTarget
    ) throws IOException {
        final Path targetPath = getTargetPath(project, skill, agentTarget);

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
            @NotNull final Skill skill,
            @NotNull final AgentTarget agentTarget
    ) throws IOException {
        return install(project, skill, agentTarget, true);
    }

    /** Installs a skill without replacing customized content unless explicitly requested. */
    public static @NotNull String install(
            @NotNull final Project project,
            @NotNull final Skill skill,
            @NotNull final AgentTarget agentTarget,
            final boolean overwrite
    ) throws IOException {
        final Path targetPath = getTargetPath(project, skill, agentTarget);
        final String skillContent = readBundledSkill(skill);

        try {
            ApplicationManager.getApplication().runWriteAction(() -> {
                try {
                    // Recheck immediately before writing, including ancestor symlinks.
                    getTargetPath(project, skill, agentTarget);
                    if (Files.exists(targetPath, LinkOption.NOFOLLOW_LINKS)) {
                        if (Objects.equals(Files.readString(targetPath), skillContent)) {
                            return;
                        }
                        if (!overwrite) {
                            throw new FileAlreadyExistsException(targetPath.toString());
                        }
                    }
                    Files.createDirectories(targetPath.getParent());
                    Files.writeString(targetPath, skillContent, StandardCharsets.UTF_8,
                            StandardOpenOption.WRITE,
                            overwrite ? StandardOpenOption.CREATE : StandardOpenOption.CREATE_NEW,
                            StandardOpenOption.TRUNCATE_EXISTING, LinkOption.NOFOLLOW_LINKS);
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

    public static @NotNull Path getTargetPath(
            @NotNull final Project project,
            @NotNull final Skill skill,
            @NotNull final AgentTarget agentTarget
    ) throws IOException {
        final VirtualFile projectDir = project.getBasePath() == null ? ProjectUtil.guessProjectDir(project) : null;
        final String basePath = project.getBasePath() != null
                ? project.getBasePath() : projectDir == null ? null : projectDir.getPath();

        if (basePath == null) {
            throw new IOException("Unable to resolve project directory.");
        }

        final Path root = Path.of(basePath).toRealPath();
        final Path relative = Path.of(agentTarget.getProjectRelativePath(skill));
        Path current = root;
        for (final Path segment : relative) {
            current = current.resolve(segment);
            if (Files.isSymbolicLink(current)) {
                throw new IOException("Skill destination must not contain symbolic links: " + current);
            }
            if (Files.exists(current, LinkOption.NOFOLLOW_LINKS)
                    && !current.equals(root.resolve(relative)) && !Files.isDirectory(current)) {
                throw new IOException("Skill destination parent is not a directory: " + current);
            }
        }
        if (Files.exists(current, LinkOption.NOFOLLOW_LINKS)
                && !Files.isRegularFile(current, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("Skill destination is not a regular file: " + current);
        }
        return current;
    }

    private static @NotNull String readBundledSkill(@NotNull final Skill skill) throws IOException {
        try (InputStream stream = MagentoSkillInstaller.class.getResourceAsStream(
                "/skills/" + skill.directoryName + "/SKILL.md"
        )) {
            if (stream == null) {
                throw new IOException("Bundled skill resource was not found: "
                        + "/skills/" + skill.directoryName + "/SKILL.md");
            }

            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
