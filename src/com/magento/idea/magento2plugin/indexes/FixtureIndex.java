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

public class FixtureIndex {
    private static FixtureIndex INSTANCE;
    private Project project;

    private FixtureIndex() {}

    public static FixtureIndex getInstance(Project project) {
        if (INSTANCE == null) {
            INSTANCE = new FixtureIndex();
        }

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

        final @NotNull PsiFile[] psiFiles = FilenameIndex.getFilesByName(project, fixtureName, GlobalSearchScope.allScope(project));

        for (PsiFile psiFile: psiFiles) {
            if (!psiFile.getVirtualFile().getPath().contains("vendor")) {
                result.add((PhpFile) psiFile);
            }
        }

        return result;
    }
}
