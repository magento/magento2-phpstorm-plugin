/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.completion.php;

import com.magento.idea.magento2plugin.completion.BaseCompletionTestCase;
import com.magento.idea.magento2plugin.magento.packages.File;

public abstract class CompletionPhpFixtureTestCase extends BaseCompletionTestCase {
    private static final String FIXTURES_FOLDER_PATH = "php" + File.separator;

    @Override
    protected String getFixturePath(final String fileName) {
        return prepareFixturePath(fileName, FIXTURES_FOLDER_PATH);
    }
}
