package com.magento.idea.magento2plugin.xml.webapi.reference.fill;

import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.resolve.PhpResolveResult;
import com.magento.idea.magento2plugin.xml.reference.util.ReferenceResultsFiller;
import com.magento.idea.magento2plugin.xml.webapi.XmlHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

/**
 * Created by isentiabov on 20.12.2015.
 */
public class ServiceMethodResultsFiller implements ReferenceResultsFiller {
    @Override
    public void fillResolveResults(PsiElement psiElement, List<ResolveResult> results, String typeName) {
        XmlAttribute classAttribute = XmlHelper.getInterfaceAttributeByMethod(psiElement);
        if (classAttribute == null) {
            return;
        }

        PhpClass serviceInterface = getServiceInterface(classAttribute);
        if (serviceInterface == null) {
            return;
        }

        fillResults(serviceInterface, results, typeName);
    }



    @Nullable
    protected PhpClass getServiceInterface(XmlAttribute xmlAttribute) {
        XmlAttributeValue value = xmlAttribute.getValueElement();
        if (value == null) {
            return null;
        }

        PhpIndex phpIndex = PhpIndex.getInstance(xmlAttribute.getProject());

        Iterator<PhpClass> iterator = phpIndex.getInterfacesByFQN(value.getValue()).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }

        return null;
    }

    protected void fillResults(PhpClass serviceInterface, List<ResolveResult> results, String typeName) {
        for (Method method : serviceInterface.getMethods()) {
            if (method.getName().equals(typeName)) {
                results.add(
                    new PhpResolveResult(method)
                );

                break;
            }
        }
    }
}
