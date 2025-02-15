/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.groups;

import com.intellij.ide.actions.NonTrivialActionGroup;
import com.intellij.openapi.util.IconLoader;
import com.magento.idea.magento2plugin.MagentoIcons;

public class NewEavAttributeGroup extends NonTrivialActionGroup {

    /**
     * Group for Eav attribute generation actions.
     */
    public NewEavAttributeGroup() {
        super();

        this.getTemplatePresentation().setIcon(
                IconLoader.createLazy(() -> MagentoIcons.MODULE)
        );
    }
}
