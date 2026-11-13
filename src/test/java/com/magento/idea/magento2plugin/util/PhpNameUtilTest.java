/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util;

import com.jetbrains.php.lang.PhpLangUtil;
import java.util.List;
import junit.framework.TestCase;

public class PhpNameUtilTest extends TestCase {
    public void testConfigurationNamesPreservePhpNormalization() {
        final String[] names = {
                "", "\\", "\\\\", "Vendor\\Module\\Model", "\\Vendor\\Module\\Model",
                "\\\\Vendor\\Module\\Model", "VirtualTypeAlias", " Vendor\\Module ",
                "Vendor\\Module\\", "vendor\\MODULE"
        };
        for (final String name : names) {
            assertEquals(name, PhpLangUtil.toPresentableFQN(name), PhpNameUtil.toPresentableFQN(name));
            assertEquals(name, PhpLangUtil.toFQN(name), PhpNameUtil.toFQN(name));
        }
    }

    public void testObjectTypeCandidatesKeepTheWrittenTypeFirst() {
        assertCandidates("Foo\\Bar", "Foo\\Bar");
        assertCandidates("Foo\\Bar\\Proxy", "Foo\\Bar\\Proxy", "Foo\\Bar");
        assertCandidates("Foo\\BarFactory", "Foo\\BarFactory", "Foo\\Bar");
        assertCandidates(
                "Foo\\BarFactory\\Proxy",
                "Foo\\BarFactory\\Proxy",
                "Foo\\BarFactory",
                "Foo\\Bar"
        );
        assertCandidates(
                "Foo\\Bar\\ProxyFactory",
                "Foo\\Bar\\ProxyFactory",
                "Foo\\Bar\\Proxy",
                "Foo\\Bar"
        );
    }

    public void testObjectTypeCandidatesFollowGeneratedChainsAtAnyDepth() {
        assertCandidates(
                "Foo\\BarFactoryFactory",
                "Foo\\BarFactoryFactory",
                "Foo\\BarFactory",
                "Foo\\Bar"
        );
        assertCandidates(
                "Foo\\Bar\\Proxy\\Proxy",
                "Foo\\Bar\\Proxy\\Proxy",
                "Foo\\Bar\\Proxy",
                "Foo\\Bar"
        );
        assertCandidates(
                "Foo\\BarFactory\\ProxyFactory",
                "Foo\\BarFactory\\ProxyFactory",
                "Foo\\BarFactory\\Proxy",
                "Foo\\BarFactory",
                "Foo\\Bar"
        );
    }

    public void testObjectTypeCandidatesStripOnlyTheEndOfTheName() {
        assertCandidates("Foo\\Factory\\Worker", "Foo\\Factory\\Worker");
        assertCandidates("Foo\\Proxy\\Worker", "Foo\\Proxy\\Worker");
        assertCandidates("Foo\\SimpleFactoryModel", "Foo\\SimpleFactoryModel");
    }

    public void testObjectTypeCandidatesSkipNamesWithoutAClass() {
        assertCandidates("Foo\\Factory", "Foo\\Factory");
        assertCandidates("\\Proxy", "\\Proxy");
        assertCandidates("");
    }

    private static void assertCandidates(final String type, final String... expected) {
        assertEquals(
                type,
                List.of(expected),
                List.copyOf(PhpNameUtil.getObjectTypeCandidates(type))
        );
    }
}
