/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.completion;

import com.magento.idea.magento2plugin.BaseProjectTestCase;
import com.magento.idea.magento2plugin.magento.packages.File;
import java.util.Arrays;
import java.util.List;

abstract public class BaseCompletionTestCase extends BaseProjectTestCase {
    private static final String MESSAGE_NO_LOOKUP = "No lookup element was provided";
    private final String testDataFolderPath
            = "testData" + File.separator + "completion" + File.separator;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.setTestDataPath(testDataFolderPath);
    }

    private void configureFixture(final String filePath) {
        myFixture.configureByFile(filePath);
        myFixture.completeBasic();
    }

    public void assertCompletionContains(final String filePath, final String... lookupStrings) {
        configureFixture(filePath);

        final String messageEmptyLookup = "Failed that completion contains `%s`";
        final String messageComplationContains = "Failed that completion contains `%s` in `%s`";

        checkContainsCompletion(lookupStrings, messageEmptyLookup, messageComplationContains);
    }

    protected void assertFileContainsCompletions(
            final String filePath,
            final String... lookupStrings
    ) {
        configureFixture(filePath);

        final String messageEmptyLookup
                = "Failed that completion contains `%s` for file " + filePath;
        final String messageCompletionContains
                = "Failed that completion contains `%s` in `%s` for file " + filePath;

        checkContainsCompletion(lookupStrings, messageEmptyLookup, messageCompletionContains);
    }

    protected void assertFileNotContainsCompletions(
            final String filePath,
            final String... lookupStrings
    ) {
        configureFixture(filePath);

        final String messageEmptyLookup
                = "Failed that completion does not contain `%s` for file " + filePath;
        final String messageCompletionNotContains
                = "Failed that completion does not contain `%s` in `%s` for file " + filePath;

        checkDoesNotContainCompletion(
                lookupStrings, messageEmptyLookup, messageCompletionNotContains
        );
    }

    protected void assertCompletionNotShowing(final String filePath) {
        configureFixture(filePath);

        final List<String> lookupElements = myFixture.getLookupElementStrings();

        if (lookupElements != null && !lookupElements.isEmpty()) {
            final String messageCompletionDoesNotShow
                    = "Failed asserting that completion does not show up";

            fail(messageCompletionDoesNotShow);
        }
    }

    protected void checkContainsCompletion(
            final String[] lookupStrings,
            final String emptyLookupError,
            final String completionContainsError
    ) {
        if (lookupStrings.length == 0) {
            fail(MESSAGE_NO_LOOKUP);
        }

        final List<String> lookupElements = myFixture.getLookupElementStrings();

        if (lookupElements == null || lookupElements.isEmpty()) {
            fail(String.format(emptyLookupError, Arrays.toString(lookupStrings)));
        }

        for (final String lookupString : lookupStrings) {
            if (!lookupElements.contains(lookupString)) {
                fail(String.format(
                        completionContainsError, lookupString, lookupElements.toString())
                );
            }
        }
    }

    protected void checkDoesNotContainCompletion(
            final String[] lookupStrings,
            final String emptyLookupError,
            final String completionDoesNotContainError
    ) {
        if (lookupStrings.length == 0) {
            fail(MESSAGE_NO_LOOKUP);
        }

        final List<String> lookupElements = myFixture.getLookupElementStrings();

        if (lookupElements == null || lookupElements.isEmpty()) {
            fail(String.format(emptyLookupError, Arrays.toString(lookupStrings)));
        }

        for (final String lookupString : lookupStrings) {
            if (lookupElements.contains(lookupString)) {
                fail(String.format(
                        completionDoesNotContainError, lookupString, lookupElements.toString())
                );
            }
        }
    }

    protected abstract String getFixturePath(String fileName);
}
