/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.stubs.indexes.js;

import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileBasedIndexExtension;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.stubs.indexes.js.data.StringSetDataExternalizer;
import com.magento.idea.magento2plugin.util.magento.js.KnockoutTemplatePathResolver;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class KnockoutTemplateIndex extends FileBasedIndexExtension<String, Set<String>> {
    public static final ID<String, Set<String>> KEY = ID.create(
            "com.magento.idea.magento2plugin.stubs.indexes.knockout_templates"
    );

    @Override
    public @NotNull ID<String, Set<String>> getName() {
        return KEY;
    }

    @Override
    public @NotNull DataIndexer<String, Set<String>, FileContent> getIndexer() {
        return inputData -> {
            final Map<String, Set<String>> map = new HashMap<>();

            if (!Settings.isEnabled(inputData.getProject())) {
                return map;
            }
            final JSFile jsFile = (JSFile) inputData.getPsiFile();
            final String componentUrl = inputData.getFile().getUrl();

            for (final String templatePath : KnockoutTemplatePathResolver.getInstance()
                    .collectTemplatePaths(jsFile)) {
                map.computeIfAbsent(templatePath, ignored -> new LinkedHashSet<>()).add(componentUrl);
            }

            return map;
        };
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return new EnumeratorStringDescriptor();
    }

    @Override
    public @NotNull DataExternalizer<Set<String>> getValueExternalizer() {
        return StringSetDataExternalizer.INSTANCE;
    }

    @Override
    public @NotNull FileBasedIndex.InputFilter getInputFilter() {
        return virtualFile -> virtualFile.getFileType().equals(JavaScriptFileType.INSTANCE);
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return 2;
    }
}
