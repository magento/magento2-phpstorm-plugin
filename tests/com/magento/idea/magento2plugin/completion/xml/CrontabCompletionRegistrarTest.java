/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.completion.xml;

import com.magento.idea.magento2plugin.magento.files.CrontabXmlTemplate;

public class CrontabCompletionRegistrarTest extends CompletionXmlFixtureTestCase {
    private static final String EXPECTED_INSTANCE = "Magento\\Catalog\\Cron\\RefreshSpecialPrices";
    private static final String EXPECTED_METHOD = "execute";
    private static final String WRONG_FILE_NAME = "wrong_named_crontab.xml";

    /**
     * The `instance` attribute of the `job` tag in crontab.xml must
     * have completion based on PHP classes index.
     */
    public void testCronJobInstanceMustHaveCompletion() {
        final String filePath = this.getFixturePath(CrontabXmlTemplate.FILE_NAME);
        assertCompletionContains(filePath, EXPECTED_INSTANCE);
    }

    /**
     * The `instance` attribute of the `job` tag in the
     * non crontab.xml file must not have completion.
     */
    public void testNotCrontabXmlMustHaveNotCompletion() {
        final String filePath = this.getFixturePath(WRONG_FILE_NAME);
        assertFileNotContainsCompletions(filePath, EXPECTED_INSTANCE);
    }

    /**
     * The non `instance` attribute of the `job` tag in crontab.xml must
     * not have completion.
     */
    public void testNotInstanceAttrMustHaveNotCompletion() {
        final String filePath = this.getFixturePath(CrontabXmlTemplate.FILE_NAME);
        assertFileNotContainsCompletions(filePath, EXPECTED_INSTANCE);
    }

    /**
     * The `instance` attribute that isn't in the `job` tag in crontab.xml must
     * not have completion.
     */
    public void testNotJobTagMustHaveNotCompletion() {
        final String filePath = this.getFixturePath(CrontabXmlTemplate.FILE_NAME);
        assertFileNotContainsCompletions(filePath, EXPECTED_INSTANCE);
    }

    /**
     * The `method` attribute of the `job` tag in crontab.xml must
     * have completion based on PHP Job method completion provider.
     */
    public void testCronJobMethodMustHaveCompletion() {
        final String filePath = this.getFixturePath(CrontabXmlTemplate.FILE_NAME);
        assertCompletionContains(filePath, EXPECTED_METHOD);
    }

    /**
     * The `method` attribute must not have completion
     * if it isn`t in the crontab.xml file.
     */
    public void testNotCrontabXmlMethodMustHaveNotCompletion() {
        final String filePath = this.getFixturePath(WRONG_FILE_NAME);
        assertFileNotContainsCompletions(filePath, EXPECTED_METHOD);
    }
}
