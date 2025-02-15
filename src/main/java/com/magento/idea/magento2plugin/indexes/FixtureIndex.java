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
import com.magento.idea.magento2plugin.magento.files.TestFixture;
import com.magento.idea.magento2plugin.magento.packages.File;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class FixtureIndex {
    private final Project project;

    public FixtureIndex(final Project project) {
        this.project = project;
    }

    /**
     * Getter for data fixtures.
     */
    public List<PhpFile> getDataFixtures(final String fixtureIdentifier) {
        final List<PhpFile> result = new ArrayList<>();

        final String[] fixturePathParts = fixtureIdentifier.split(File.separator);
        final String fixtureName = fixturePathParts[fixturePathParts.length - 1];
        final String exactFilePath = TestFixture.FIXTURES_LOCATION.concat(fixtureIdentifier);

        @NotNull final PsiFile[] psiFiles = FilenameIndex.getFilesByName(
                project,
                fixtureName,
                GlobalSearchScope.allScope(project)
        );

        for (final PsiFile psiFile: psiFiles) {
            @NotNull final String filePath = psiFile.getVirtualFile().getPath();
            if (!filePath.contains(TestFixture.FIXTURES_EXCLUDE_PATH)
                    && filePath.contains(exactFilePath)) {
                result.add((PhpFile) psiFile);
            }
        }

        return result;
    }
}
