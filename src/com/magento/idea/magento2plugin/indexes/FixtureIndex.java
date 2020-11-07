/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.indexes;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.php.lang.psi.PhpFile;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class FixtureIndex {
    private static final FixtureIndex INSTANCE = new FixtureIndex();
    private Project project;

    private FixtureIndex() {}

    /**
     * Getter for class instance.
     */
    public static FixtureIndex getInstance(final Project project) {
        INSTANCE.project = project;

        return INSTANCE;
    }

    /**
     * Getter for data fixtures.
     */
    public List<PhpFile> getDataFixtures(final String fixturePath) {
        final List<PhpFile> result = new ArrayList<>();

        final String[] fixturePathParts = fixturePath.split("/");
        final String fixtureName = fixturePathParts[fixturePathParts.length - 1];

        final @NotNull PsiFile[] psiFiles = FilenameIndex.getFilesByName(
                project,
                fixtureName,
                GlobalSearchScope.allScope(project)
        );

        for (final PsiFile psiFile: psiFiles) {
            @NotNull final String filePath = psiFile.getVirtualFile().getPath();
            if (!filePath.contains("vendor") && filePath.contains(fixturePath)) {
                result.add((PhpFile) psiFile);
            }
        }

        return result;
    }
}
