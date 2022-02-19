/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.event.eavdialog;

import com.magento.idea.magento2plugin.magento.packages.eav.AttributeSourceModel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JPanel;

public class AttributeSourceRelationsItemListener implements ItemListener {
    private final JPanel customSourcePanel;

    public AttributeSourceRelationsItemListener(final JPanel customSourcePanel) {
        this.customSourcePanel = customSourcePanel;
    }

    @Override
    public void itemStateChanged(final ItemEvent itemEvent) {
        final String selectedSource = itemEvent.getItem().toString();

        customSourcePanel.setVisible(
                selectedSource.equals(AttributeSourceModel.GENERATE_SOURCE.getSource())
        );
    }
}
