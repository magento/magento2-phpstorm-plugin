package com.magento.idea.magento2plugin.reference.provider.mftf;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import com.magento.idea.magento2plugin.stubs.indexes.mftf.DataIndex;
import com.magento.idea.magento2plugin.xml.XmlPsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class DataReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        List<PsiReference> psiReferences = new ArrayList<>();

        String origValue = StringUtil.unquoteString(element.getText());
        String modifiedValue = origValue.replaceAll("\\{{2}([_A-Za-z0-9.]+)(\\([^}]+\\))?\\}{2}", "$1").toString();

        Collection<VirtualFile> containingFiles = FileBasedIndex.getInstance()
            .getContainingFiles(
                DataIndex.KEY,
                modifiedValue,
                GlobalSearchScope.getScopeRestrictedByFileTypes(
                    GlobalSearchScope.allScope(element.getProject()),
                    XmlFileType.INSTANCE
                )
            );

        PsiManager psiManager = PsiManager.getInstance(element.getProject());

        List<PsiElement> psiElements = new ArrayList<>();

        for (VirtualFile virtualFile: containingFiles) {
            XmlFile xmlFile = (XmlFile) psiManager.findFile(virtualFile);

            if (xmlFile == null) {
                continue;
            }

            if (!modifiedValue.contains(".")) {
                Collection<XmlAttributeValue> valueElements = XmlPsiTreeUtil
                    .findAttributeValueElements(xmlFile, "entity", "name", modifiedValue);

                psiElements.addAll(valueElements);
                continue;
            }


            String[] parts = modifiedValue.split("\\.");
            String entityName = parts[0];
            String dataName = parts[1];

            XmlTag rootTag = xmlFile.getRootTag();

            for (XmlTag entityTag: rootTag.findSubTags("entity")) {
                if (entityTag == null) {
                    continue;
                }

                XmlAttribute entityNameAttribute = entityTag.getAttribute("name");

                if (entityNameAttribute == null ||
                    entityNameAttribute.getValueElement() == null ||
                    !entityNameAttribute.getValueElement().getValue().equals(entityName)
                ) {
                    continue;
                }

                for (XmlTag dataTag: entityTag.findSubTags("data")) {
                    XmlAttribute keyNameAttribute = dataTag.getAttribute("key");

                    if (keyNameAttribute != null &&
                        keyNameAttribute.getValueElement() != null &&
                        keyNameAttribute.getValueElement().getValue().equals(dataName)
                    ) {
                        psiElements.add(keyNameAttribute.getValueElement());
                    }
                }
            }
        }

        if (psiElements.size() > 0) {
            psiReferences.add(new PolyVariantReferenceBase(element, psiElements));
        }

        return psiReferences.toArray(new PsiReference[psiReferences.size()]);
    }
}
