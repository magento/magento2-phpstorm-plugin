package com.magento.idea.magento2plugin.xml.di.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.XmlPatterns;
import com.intellij.util.ProcessingContext;
import com.magento.idea.magento2plugin.xml.di.XmlHelper;
import com.magento.idea.magento2plugin.xml.di.index.VirtualTypesNamesFileBasedIndex;
import org.jetbrains.annotations.NotNull;

/**
 * Created by dkvashnin on 10/14/15.
 */
public class VirtualTypeCompletionContributor extends CompletionContributor {
    public VirtualTypeCompletionContributor() {
        extend(CompletionType.BASIC,
            XmlPatterns.or(
                XmlHelper.getArgumentValuePatternForType("object"),
                XmlHelper.getItemValuePatternForType("object"),
                XmlHelper.getTagAttributePattern(XmlHelper.TYPE_TAG, XmlHelper.NAME_ATTRIBUTE),
                XmlHelper.getTagAttributePattern(XmlHelper.PLUGIN_TAG, XmlHelper.TYPE_ATTRIBUTE)
            ),
            new CompletionProvider<CompletionParameters>() {
                public void addCompletions(@NotNull CompletionParameters parameters,
                                           ProcessingContext context,
                                           @NotNull CompletionResultSet resultSet) {
                    String[] allVirtualTypesNames = VirtualTypesNamesFileBasedIndex.getAllVirtualTypesNames(
                        parameters.getOriginalPosition().getProject()
                    );

                    for (String name: allVirtualTypesNames) {
                        resultSet.addElement(LookupElementBuilder.create(name));
                    }
                }
            }
        );
    }
}
