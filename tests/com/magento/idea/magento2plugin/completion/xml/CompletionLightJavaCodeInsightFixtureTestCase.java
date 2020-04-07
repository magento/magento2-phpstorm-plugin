/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.completion.xml;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.Arrays;
import java.util.List;

abstract public class CompletionLightJavaCodeInsightFixtureTestCase extends BasePlatformTestCase {

    public void assertCompletionContains(String filePath, String... lookupStrings) {
        myFixture.configureByFile(filePath);
        myFixture.completeBasic();

        checkContainsCompletion(lookupStrings);
    }

    protected void assertCompletionMuchWithFilePositiveCase(
            String positiveFilePath,
            String... lookupStrings
    ) {
        myFixture.configureByFile(positiveFilePath);
        myFixture.completeBasic();

        String messageEmptyLookup = "Failed that completion contains `%s` for file " + positiveFilePath;
        String messageCompletionContains = "Failed that completion contains `%s` in `%s` for file " + positiveFilePath;

        checkContainsCompletion(lookupStrings, messageEmptyLookup, messageCompletionContains);
    }

    protected void assertCompletionMuchWithFileNegativeCase(
            String negativeFilePath,
            String negativeFileText,
            String... lookupStrings
    ) {
        myFixture.configureByText(negativeFilePath, negativeFileText);
        myFixture.completeBasic();

        String messageCompletionNotContains = "Failed that completion not contains `%s` in `%s` for file "
                + negativeFilePath;

        checkNotContainsCompletion(lookupStrings, messageCompletionNotContains);
    }

    protected void checkContainsCompletion(String[] lookupStrings) {
        String defaultMessageEmptyLookup = "Failed that completion contains `%s`";
        String defaultMessageCompletionContains = "Failed that completion contains `%s` in `%s`";

        checkContainsCompletion(lookupStrings, defaultMessageEmptyLookup, defaultMessageCompletionContains);
    }

    protected void checkContainsCompletion(
            String[] lookupStrings,
            String messageEmptyLookup,
            String messageCompletionContains
    ) {
        if (lookupStrings.length == 0) {
            fail("No lookup element given");
        }
        List<String> lookupElements = myFixture.getLookupElementStrings();

        if (null == lookupElements || lookupElements.size() == 0) {
            fail(String.format(messageEmptyLookup, Arrays.toString(lookupStrings)));
        }

        for (String s : lookupStrings) {
            if (!lookupElements.contains(s)) {
                fail(String.format(messageCompletionContains, s, lookupElements.toString()));
            }
        }
    }

    protected void checkNotContainsCompletion(String[] lookupStrings) {
        String defaultMessageCompletionNotContains = "Failed that completion not contains `%s` in `%s`";

        checkNotContainsCompletion(lookupStrings, defaultMessageCompletionNotContains);
    }

    protected void checkNotContainsCompletion(String[] lookupStrings, String messageCompletionNotContains) {
        if (lookupStrings.length == 0) {
            fail("No lookup element given");
        }
        List<String> lookupElements = myFixture.getLookupElementStrings();

        if (null != lookupElements) {
            for (String s : lookupStrings) {
                if (lookupElements.contains(s)) {
                    fail(String.format(messageCompletionNotContains, s, lookupElements.toString()));
                }
            }
        }
    }
}
