/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2uct.analysis

import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

internal object UctReleaseFixture {
    fun archive(version: String, extra: Map<String, String> = emptyMap(), metadataVersion: String = version): ByteArray {
        val files = linkedMapOf(
            "composer.json" to """{"name":"magento/magento2ce","version":"$metadataVersion"}""",
            "app/code/Magento/Catalog/etc/module.xml" to "<config><module name=\"Magento_Catalog\"/></config>",
            "lib/internal/Magento/Framework/AppInterface.php" to """
                <?php namespace Magento\Framework;
                /** @api */ interface AppInterface { public function launch(); }
            """.trimIndent(),
            "app/code/Magento/Catalog/Model/Sample.php" to """
                <?php namespace Magento\Catalog\Model;
                /** @api */ class Sample {
                    public const PUBLIC_VALUE = 1;
                    private const PRIVATE_VALUE = 2;
                    public ${'$'}value;
                    private ${'$'}hidden;
                    public function __construct() {}
                    /** @deprecated */ public function oldMethod() {}
                    private function secret() {}
                }
            """.trimIndent(),
            "app/code/Magento/Catalog/Test/Invalid.php" to "<?php class {",
            "dev/tests/Invalid.php" to "<?php class {",
            "vendor/third-party/Invalid.php" to "<?php class {"
        )
        files.putAll(extra)
        return ByteArrayOutputStream().also { buffer ->
            ZipOutputStream(buffer).use { zip ->
                files.forEach { (path, text) ->
                    zip.putNextEntry(ZipEntry("magento2-$version/$path"))
                    zip.write(text.toByteArray(Charsets.UTF_8)); zip.closeEntry()
                }
            }
        }.toByteArray()
    }

    fun index(version: String = "2.4.9", existing: Set<String> = emptySet(),
              api: Set<String> = existing, deprecated: Set<String> = emptySet()): UctReleaseIndex {
        val framework = "\\Magento\\Framework\\AppInterface"
        return UctReleaseIndex(version, UctReleaseSource.url(version), "a".repeat(64), 2,
            (existing + framework).associateWith { true }, (api + framework).associateWith { true },
            deprecated.associateWith { true })
    }
}
