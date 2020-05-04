/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.magento.idea.magento2plugin.bundles.CommonBundle;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JDialog;

/**
 * All code generate dialog should extend this class.
 */
@SuppressWarnings({"PMD.ShortVariable", "PMD.MissingSerialVersionUID"})
public abstract class AbstractDialog extends JDialog {

    protected CommonBundle bundle;

    public AbstractDialog() {
        super();
        this.bundle = new CommonBundle();
    }

    protected void centerDialog(final AbstractDialog dialog) {
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final int x = screenSize.width / 2  - dialog.getSize().width / 2;
        final int y = screenSize.height / 2 - dialog.getSize().height / 2;
        dialog.setLocation(x, y);
    }

    protected void onCancel() {
        this.setVisible(false);
    }
}
