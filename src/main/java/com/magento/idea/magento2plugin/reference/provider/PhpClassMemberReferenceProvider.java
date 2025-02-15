/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.provider;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpClassMember;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import com.magento.idea.magento2plugin.util.RegExUtil;
import gnu.trove.THashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public class PhpClassMemberReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(
            @NotNull final PsiElement element,
            @NotNull final ProcessingContext context
    ) {
        final String value = StringUtil.unquoteString(element.getText());
        final Matcher matcher
                = Pattern.compile(RegExUtil.XmlRegex.CLASS_MEMBER_NAME).matcher(value);

        if (!matcher.find()) {
            return PsiReference.EMPTY_ARRAY;
        }

        final List<PsiReference> psiReferences = new ArrayList<>();
        final Collection<PhpClassMember> members = new THashSet<>();
        final String elementName = matcher.group(1);
        final String classFQN = value.substring(0, value.lastIndexOf("::"));
        final PhpIndex phpIndex = PhpIndex.getInstance(element.getProject());

        for (final PhpClass phpClass : phpIndex.getAnyByFQN(classFQN)) {
            members.addAll(phpClass.getFields());
            members.addAll(phpClass.getMethods());
            members.removeIf(c -> !c.getName().equals(elementName));
        }

        if (!members.isEmpty()) {
            final String origValue = element.getText();
            final TextRange range = new TextRange(
                    origValue.indexOf(elementName),
                    origValue.indexOf(elementName) + elementName.length()
            );
            psiReferences.add(new PolyVariantReferenceBase(element, range, members));
        }

        return psiReferences.toArray(new PsiReference[0]);
    }
}
