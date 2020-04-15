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
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhpClassMemberReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        List<PsiReference> psiReferences = new ArrayList<>();
        final Collection<PhpClassMember> members = new THashSet<>();

        String origValue = element.getText();
        String value = StringUtil.unquoteString(element.getText());

        Matcher matcher = Pattern.compile(RegExUtil.XmlRegex.CLASS_MEMBER_NAME).matcher(value);
        if (!matcher.find()) {
            return PsiReference.EMPTY_ARRAY;
        }

        String elementName = matcher.group(1);
        String classFQN = value.substring(0, value.lastIndexOf("::"));

        PhpIndex phpIndex = PhpIndex.getInstance(element.getProject());
        for (final PhpClass phpClass : phpIndex.getAnyByFQN(classFQN)) {
            members.addAll(phpClass.getFields());
            members.addAll(phpClass.getMethods());
            members.removeIf(c -> !c.getName().equals(elementName));
        }

        if (members.size() > 0) {
            TextRange range = new TextRange(
                    origValue.indexOf(elementName),
                    origValue.indexOf(elementName) + elementName.length()
            );
            psiReferences.add(new PolyVariantReferenceBase(element, range, members));
        }
        return psiReferences.toArray(new PsiReference[psiReferences.size()]);
    }
}
