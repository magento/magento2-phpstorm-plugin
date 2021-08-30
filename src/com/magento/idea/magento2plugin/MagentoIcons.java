/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin;

import com.intellij.openapi.util.IconLoader;
import javax.swing.Icon;

@SuppressWarnings({"PMD.ClassNamingConventions"})
public class MagentoIcons {

    public static final Icon WEB_API = IconLoader.getIcon("/icons/webapi.png", MagentoIcons.class);
    public static final Icon MODULE = IconLoader.getIcon("/icons/module.png", MagentoIcons.class);
    public static final Icon PLUGIN_ICON = IconLoader.getIcon(
            "/META-INF/pluginIcon.svg",
            MagentoIcons.class
    );
}
