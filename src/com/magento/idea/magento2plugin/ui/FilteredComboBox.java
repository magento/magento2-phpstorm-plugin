/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.ui;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class FilteredComboBox extends JComboBox {
    private List<String> entries;

    public List<String> getEntries() {
        return entries;
    }

    public FilteredComboBox(List<String> entries) {
        super(entries.toArray());

        this.entries = entries;
        this.setEditable(true);

        final JTextField textfield = (JTextField) this.getEditor().getEditorComponent();

        textfield.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent ke) {
                SwingUtilities.invokeLater(() -> comboFilter(textfield.getText()));
            }
        });

    }

    public void comboFilter(String enteredText) {
        List<String> entriesFiltered = new ArrayList<>();

        for (String entry : getEntries()) {
            if (entry.toLowerCase().contains(enteredText.toLowerCase())) {
                entriesFiltered.add(entry);
            }
        }

        if (entriesFiltered.size() == 0) {
            this.hidePopup();
            return;
        }

        this.setModel(new DefaultComboBoxModel(entriesFiltered.toArray()));
        this.setSelectedItem(enteredText);
        this.showPopup();
    }
}
