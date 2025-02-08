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
    public static final Icon PLUGIN_ICON_SMALL =
            IconLoader.getIcon("/icons/pluginIcon16x16.svg", MagentoIcons.class);
    public static final Icon PLUGIN_ICON_MEDIUM =
            IconLoader.getIcon("/icons/pluginIcon64x64.svg", MagentoIcons.class);
    public static final Icon GRAPHQL = IconLoader.getIcon("/icons/graphql.svg", MagentoIcons.class);
}
