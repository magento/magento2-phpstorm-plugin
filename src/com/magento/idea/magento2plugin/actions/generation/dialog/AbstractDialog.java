/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.magento.idea.magento2plugin.bundles.CommonBundle;

import javax.swing.*;
import java.awt.*;

/**
 * All code generate dialog should extend this class
 */
public abstract class AbstractDialog extends JDialog {

    protected CommonBundle bundle;

    public AbstractDialog() {
        this.bundle = new CommonBundle();
    }

    protected void centerDialog(AbstractDialog dialog) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screenSize.width / 2  - dialog.getSize().width / 2;
        int y = screenSize.height / 2 - dialog.getSize().height / 2;
        dialog.setLocation(x, y);
    }

    protected void onCancel() {
        this.setVisible(false);
    }
}
