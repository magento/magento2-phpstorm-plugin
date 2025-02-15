/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.generator.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.xml.util.XmlUtil;

public class XmlFilePositionUtil {
    private static XmlFilePositionUtil INSTANCE = null;

    public static XmlFilePositionUtil getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new XmlFilePositionUtil();
        }

        return INSTANCE;
    }

    public int getRootInsertPosition(XmlFile xmlFile) {
        int insertPos = -1;
        XmlTag rootTag = xmlFile.getRootTag();
        if (rootTag == null) {
            return insertPos;
        }
        return getEndPositionOfTag(rootTag);
    }

    public int getEndPositionOfTag(XmlTag tag) {
        PsiElement tagEnd = XmlUtil.getTokenOfType(tag, XmlTokenType.XML_END_TAG_START);
        if (tagEnd == null) {
            return -1;
        }

        return tagEnd.getTextOffset();
    }
}
