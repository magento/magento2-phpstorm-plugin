/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.xml;

import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.reference.BaseReferenceTestCase;

abstract public class ReferenceXmlFixtureTestCase extends BaseReferenceTestCase {

    private static final String fixturesFolderPath = "xml" + File.separator;

    protected String getFixturePath(String fileName) {
        return prepareFixturePath(fileName, fixturesFolderPath);
    }
}
