/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.maintenance;

import com.intellij.openapi.wm.StatusBarWidget;

/**
 * Inherits the platform's default methods without Kotlin forwarding the deprecated
 * getPresentation(PlatformType) overload into our widget class.
 */
abstract class MaintenanceStatusBarWidget implements StatusBarWidget {
}
