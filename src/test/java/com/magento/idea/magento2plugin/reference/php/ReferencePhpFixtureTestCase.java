/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.php;

import com.magento.idea.magento2plugin.reference.BaseReferenceTestCase;
import com.magento.idea.magento2plugin.magento.packages.File;

abstract public class ReferencePhpFixtureTestCase extends BaseReferenceTestCase {

    private static final String fixturesFolderPath = "php" + File.separator;

    protected String getFixturePath(String fileName) {
        return prepareFixturePath(fileName, fixturesFolderPath);
    }
}
