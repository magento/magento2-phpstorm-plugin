/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

rootProject.name = "Magento 2 and Adobe Commerce"
val skipFoojayResolver =
    System.getenv("QODANA_SKIP_FOOJAY_RESOLVER") == "true" ||
        !org.gradle.api.JavaVersion.current().isCompatibleWith(org.gradle.api.JavaVersion.VERSION_21)

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" apply false
}

if (!skipFoojayResolver) {
    apply(plugin = "org.gradle.toolchains.foojay-resolver-convention")
}
