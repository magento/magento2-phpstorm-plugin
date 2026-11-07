/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.stubs.indexes.xml;

import static org.junit.Assert.assertEquals;

import java.util.Set;
import org.junit.Test;

public class PhpClassNameIndexReferencesTest {

    @Test
    public void exactClassRefersToItself() {
        assertEquals(Set.of("Foo\\Bar"), PhpClassNameIndex.getReferencedClasses("Foo\\Bar"));
    }

    @Test
    public void leadingBackslashAndSpacesAreIgnored() {
        assertEquals(Set.of("Foo\\Bar"), PhpClassNameIndex.getReferencedClasses(" \\Foo\\Bar "));
    }

    @Test
    public void staticReferenceRefersToTheClass() {
        assertEquals(
                Set.of("Foo\\Bar"),
                PhpClassNameIndex.getReferencedClasses("Foo\\Bar::getValue")
        );
    }

    @Test
    public void everyClassMemberFormRefersToTheClass() {
        assertEquals(
                Set.of("Foo\\Bar"),
                PhpClassNameIndex.getReferencedClasses("Foo\\Bar::getValue()")
        );
        assertEquals(
                Set.of("Foo\\Bar"),
                PhpClassNameIndex.getReferencedClasses("Foo\\Bar::$value")
        );
    }

    @Test
    public void proxyRefersToItselfAndItsClass() {
        assertEquals(
                Set.of("Foo\\Bar\\Proxy", "Foo\\Bar"),
                PhpClassNameIndex.getReferencedClasses("Foo\\Bar\\Proxy")
        );
    }

    @Test
    public void factoryRefersToItselfAndItsClass() {
        assertEquals(
                Set.of("Foo\\BarFactory", "Foo\\Bar"),
                PhpClassNameIndex.getReferencedClasses("Foo\\BarFactory")
        );
    }

    @Test
    public void factoryProxyRefersToAllThree() {
        assertEquals(
                Set.of("Foo\\BarFactory\\Proxy", "Foo\\BarFactory", "Foo\\Bar"),
                PhpClassNameIndex.getReferencedClasses("Foo\\BarFactory\\Proxy")
        );
    }

    @Test
    public void proxyFactoryRefersToAllThree() {
        assertEquals(
                Set.of("Foo\\Bar\\ProxyFactory", "Foo\\Bar\\Proxy", "Foo\\Bar"),
                PhpClassNameIndex.getReferencedClasses("Foo\\Bar\\ProxyFactory")
        );
    }

    @Test
    public void classNamedFactoryHasNoSourceClass() {
        assertEquals(
                Set.of("Foo\\Factory"),
                PhpClassNameIndex.getReferencedClasses("Foo\\Factory")
        );
    }

    @Test
    public void otherValuesReferToNothing() {
        assertEquals(Set.of(), PhpClassNameIndex.getReferencedClasses("Foo_Bar::resource"));
        assertEquals(Set.of(), PhpClassNameIndex.getReferencedClasses("Bar"));
        assertEquals(Set.of(), PhpClassNameIndex.getReferencedClasses("Foo\\Bar::a text"));
    }
}
