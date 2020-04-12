package com.magento.idea.magento2plugin.actions.generation.dialog;

import javax.swing.*;
import java.awt.event.*;

public class NewCronjobDialog extends AbstractDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField cronjobName;
    private JTextField cronjobPath;
    private JRadioButton fixedScheduleRadioButton;
    private JRadioButton configurableScheduleRadioButton;
    private JRadioButton everyMinuteRadioButton;
    private JRadioButton customScheduleRadioButton;
    private JTextField scheduleMask;
    private JRadioButton atMidnightRadioButton;
    private JPanel fixedSchedulePanel;
    private JTextField configPathField;
    private JPanel configurableSchedulePanel;

    public NewCronjobDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle("Create a new Magento 2 cronjob..");
        pushToMiddle();

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        fixedScheduleRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                configurableSchedulePanel.setVisible(false);
                fixedSchedulePanel.setVisible(true);
            }
        });

        configurableScheduleRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fixedSchedulePanel.setVisible(false);
                configurableSchedulePanel.setVisible(true);
            }
        });

        everyMinuteRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scheduleMask.setEditable(false);
                scheduleMask.setText("* * * * *");
            }
        });

        atMidnightRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scheduleMask.setEditable(false);
                scheduleMask.setText("0 0 * * *");
            }
        });

        customScheduleRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scheduleMask.setText("* * * * *");
                scheduleMask.setEditable(true);
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    protected void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void open() {
        NewCronjobDialog dialog = new NewCronjobDialog();
        dialog.pack();
        dialog.setVisible(true);
    }
}
