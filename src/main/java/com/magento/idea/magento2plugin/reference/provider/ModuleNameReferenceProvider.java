/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.provider;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.stubs.indexes.xml.ModuleXmlIndex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public class ModuleNameReferenceProvider extends PsiReferenceProvider {

    private static final Pattern MODULE_NAME = Pattern.compile("([A-Z][a-zA-Z0-9]+)_([A-Z][a-zA-Z0-9]+)");

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(
            final @NotNull PsiElement element,
            final @NotNull ProcessingContext context
    ) {
        final Matcher matcher = MODULE_NAME.matcher(element.getText());
        if (!matcher.find()) {
            return PsiReference.EMPTY_ARRAY;
        }

        return new PsiReference[]{new ModuleNameReference(
                element, new TextRange(matcher.start(), matcher.end()), matcher.group()
        )};
    }

    private static final class ModuleNameReference extends PsiPolyVariantReferenceBase<PsiElement> {
        private final String moduleName;

        private ModuleNameReference(
                final PsiElement element,
                final TextRange range,
                final String moduleName
        ) {
            // Matching text need not name an installed module; do not report unresolved-reference errors.
            super(element, range, true);
            this.moduleName = moduleName;
        }

        @Override
        public ResolveResult @NotNull [] multiResolve(final boolean incompleteCode) {
            if (!myElement.isValid() || DumbService.isDumb(myElement.getProject())) {
                return ResolveResult.EMPTY_ARRAY;
            }

            // Reference collection also runs during highlighting. Only resolution may query indexes.
            final Collection<VirtualFile> moduleXmlFiles = FileBasedIndex.getInstance().getContainingFiles(
                    ModuleXmlIndex.KEY, moduleName, GlobalSearchScope.allScope(myElement.getProject())
            );
            final PsiManager psiManager = PsiManager.getInstance(myElement.getProject());
            final List<ResolveResult> results = new ArrayList<>();
            for (final VirtualFile moduleXmlFile : moduleXmlFiles) {
                final VirtualFile etcDirectory = moduleXmlFile.getParent();
                final VirtualFile moduleRoot = etcDirectory == null ? null : etcDirectory.getParent();
                if (moduleRoot == null || !moduleRoot.isDirectory()) {
                    continue;
                }

                final PsiDirectory directory = psiManager.findDirectory(moduleRoot);
                if (directory != null) {
                    results.add(new PsiElementResolveResult(directory));
                }
            }
            return results.toArray(ResolveResult.EMPTY_ARRAY);
        }
    }
}
