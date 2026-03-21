/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.indexes;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.magento.idea.magento2plugin.magento.files.TestFixture;
import com.magento.idea.magento2plugin.magento.packages.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public final class FixtureIndex {
    private final Project project;

    public FixtureIndex(final Project project) {
        this.project = project;
    }

    /**
     * Getter for data fixtures.
     */
    public List<PsiFile> getDataFixtures(final String fixtureIdentifier) {
        final List<PsiFile> result = new ArrayList<>();
        final String normalizedFixtureIdentifier = fixtureIdentifier
                .trim()
                .replace('\\', '/')
                .replaceFirst("^/+", "");
        if (normalizedFixtureIdentifier.isEmpty()) {
            return result;
        }
        final String[] fixturePathParts = normalizedFixtureIdentifier.split(File.separator);
        final String fixtureName = fixturePathParts[fixturePathParts.length - 1];
        final List<String> expectedFilePaths = List.of(
                TestFixture.FIXTURES_LOCATION.concat(normalizedFixtureIdentifier),
                TestFixture.FIXTURES_LOCATION_API.concat(normalizedFixtureIdentifier)
        );
        result.addAll(getFixturesByProjectPath(expectedFilePaths));
        if (!result.isEmpty()) {
            return result;
        }

        @NotNull final PsiFileSystemItem[] psiFiles = FilenameIndex.getFilesByName(
                project,
                fixtureName,
                GlobalSearchScope.allScope(project),
                true
        );

        for (final PsiFileSystemItem psiFile: psiFiles) {
            if (!(psiFile instanceof PsiFile)) {
                continue;
            }
            @NotNull final String filePath = psiFile.getVirtualFile().getPath().replace('\\', '/');
            if (!filePath.contains(TestFixture.FIXTURES_EXCLUDE_PATH)
                    && expectedFilePaths.stream().anyMatch(filePath::endsWith)) {
                result.add((PsiFile) psiFile);
            }
        }

        return result;
    }

    private @NotNull List<PsiFile> getFixturesByProjectPath(
            final @NotNull List<String> expectedFilePaths
    ) {
        final Set<PsiFile> result = new LinkedHashSet<>();
        final VirtualFile projectDir = ProjectUtil.guessProjectDir(project);
        if (projectDir == null) {
            return new ArrayList<>(result);
        }

        final PsiManager psiManager = PsiManager.getInstance(project);
        for (final String expectedFilePath : expectedFilePaths) {
            final VirtualFile virtualFile = projectDir.findFileByRelativePath(expectedFilePath);
            if (virtualFile == null) {
                continue;
            }
            final PsiFile psiFile = psiManager.findFile(virtualFile);
            if (psiFile != null) {
                result.add(psiFile);
            }
        }

        return new ArrayList<>(result);
    }
}
