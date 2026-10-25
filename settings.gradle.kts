/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

rootProject.name = "Magento 2 and Adobe Commerce"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":php-mcp", ":php-graphql")
project(":php-mcp").projectDir = file("modules/php-mcp")
project(":php-graphql").projectDir = file("modules/php-graphql")
