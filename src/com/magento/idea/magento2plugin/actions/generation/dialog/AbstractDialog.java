/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.dialog;

import javax.swing.*;
import java.awt.*;

/**
 * All code generate dialog should extend this class
 */
public abstract class AbstractDialog extends JDialog {
    protected void pushToMiddle() {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2  -this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
    }

    protected void onCancel() {
        this.setVisible(false);
    }
}
