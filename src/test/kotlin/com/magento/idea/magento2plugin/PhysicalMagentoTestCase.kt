/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin

import com.intellij.testFramework.builders.EmptyModuleFixtureBuilder
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import java.nio.file.Path

/** A fresh project on disk for tools which intentionally validate and write filesystem paths. */
abstract class PhysicalMagentoTestCase : BasePlatformTestCase() {
    override fun createMyFixture(): CodeInsightTestFixture {
        val factory = IdeaTestFixtureFactory.getFixtureFactory()
        val directory = factory.createTempDirTestFixture()
        val root = Path.of(directory.tempDirPath)
        val builder = factory.createFixtureBuilder(root.fileName.toString(), root.parent, true)
        builder.addModule(EmptyModuleFixtureBuilder::class.java).addSourceContentRoot(root.toString())
        return factory.createCodeInsightFixture(builder.fixture, directory)
    }
}
