package com.magento.idea.magento2plugin.xml.util;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.util.PsiContextMatcherI;
import com.magento.idea.magento2plugin.xml.di.completion.DiCompletionContributor;
import com.magento.idea.magento2plugin.xml.di.index.VirtualTypesNamesFileBasedIndex;

import java.util.List;

/**
* Created by dkvashnin on 12/25/15.
*/
public class VirtualTypeParentMatcher implements PsiContextMatcherI<String> {
    private ParentTypeMatcher parentTypeMatcher;
    private Project project;

    public VirtualTypeParentMatcher(ParentTypeMatcher parentTypeMatcher, Project project) {
        this.parentTypeMatcher = parentTypeMatcher;
        this.project = project;
    }

    @Override
    public boolean match(String virtualType) {
        List<PhpClass> superParentTypes = VirtualTypesNamesFileBasedIndex.getSuperParentTypes(project, virtualType);

        return parentTypeMatcher.match(
            superParentTypes.toArray(new PhpClass[superParentTypes.size()])
        );
    }
}
