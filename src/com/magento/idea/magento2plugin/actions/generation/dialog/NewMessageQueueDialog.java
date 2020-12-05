/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.actions.generation.NewDataModelAction;
import com.magento.idea.magento2plugin.actions.generation.NewMessageQueueAction;
import com.magento.idea.magento2plugin.actions.generation.data.QueueCommunicationData;
import com.magento.idea.magento2plugin.actions.generation.data.QueueConsumerData;
import com.magento.idea.magento2plugin.actions.generation.data.QueuePublisherData;
import com.magento.idea.magento2plugin.actions.generation.data.QueueTopologyData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.AlphaWithDashRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.AlphaWithPeriodRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.AlphanumericWithUnderscoreRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.Lowercase;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NumericRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.PhpClassFqnRule;
import com.magento.idea.magento2plugin.actions.generation.generator.QueueCommunicationGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.QueueConsumerGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.QueuePublisherGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.QueueTopologyGenerator;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

@SuppressWarnings({
        "PMD.TooManyFields",
        "PMD.ExcessiveImports",
})
public class NewMessageQueueDialog extends AbstractDialog {
    private static final String TOPIC_NAME = "Topic Name";
    private static final String HANDLER_NAME = "Handler Name";
    private static final String HANDLER_TYPE = "Handler Type";
    private static final String CONSUMER_NAME = "Consumer Name";
    private static final String QUEUE_NAME = "Queue Name";
    private static final String CONSUMER_TYPE = "Consumer Type";
    private static final String MAX_MESSAGES = "Maximum Messages";
    private static final String CONNECTION_NAME = "Connection Name";
    private static final String EXCHANGE_NAME = "Exchange Name";
    private static final String BINDING_ID = "Binding ID";
    private static final String BINDING_TOPIC = "Binding Topic";

    /* TODO: Improve validation */
    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, TOPIC_NAME})
    @FieldValidation(rule = RuleRegistry.ALPHA_WITH_PERIOD,
            message = {AlphaWithPeriodRule.MESSAGE, TOPIC_NAME})
    @FieldValidation(rule = RuleRegistry.LOWERCASE,
            message = {Lowercase.MESSAGE, TOPIC_NAME})
    private JTextField topicName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, HANDLER_NAME})
    @FieldValidation(rule = RuleRegistry.ALPHANUMERIC_WITH_UNDERSCORE,
            message = {AlphanumericWithUnderscoreRule.MESSAGE, HANDLER_NAME})
    private JTextField handlerName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, HANDLER_TYPE})
    @FieldValidation(rule = RuleRegistry.PHP_CLASS_FQN,
            message = {PhpClassFqnRule.MESSAGE, HANDLER_TYPE})
    private JTextField handlerType;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, CONSUMER_NAME})
    @FieldValidation(rule = RuleRegistry.ALPHA_WITH_PERIOD,
            message = {AlphaWithPeriodRule.MESSAGE, CONSUMER_NAME})
    @FieldValidation(rule = RuleRegistry.LOWERCASE,
            message = {Lowercase.MESSAGE, CONSUMER_NAME})
    private JTextField consumerName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, QUEUE_NAME})
    @FieldValidation(rule = RuleRegistry.ALPHA_WITH_PERIOD,
            message = {AlphaWithPeriodRule.MESSAGE, QUEUE_NAME})
    @FieldValidation(rule = RuleRegistry.LOWERCASE,
            message = {Lowercase.MESSAGE, QUEUE_NAME})
    private JTextField queueName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, CONSUMER_TYPE})
    @FieldValidation(rule = RuleRegistry.PHP_CLASS_FQN,
            message = {PhpClassFqnRule.MESSAGE, CONSUMER_TYPE})
    private JTextField consumerType;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, MAX_MESSAGES})
    @FieldValidation(rule = RuleRegistry.NUMERIC,
            message = {NumericRule.MESSAGE, MAX_MESSAGES})
    private JTextField maxMessages;

    // TODO: Can this be made a dropdown?
    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, CONNECTION_NAME})
    private JTextField connectionName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, EXCHANGE_NAME})
    @FieldValidation(rule = RuleRegistry.ALPHA_WITH_DASH,
            message = {AlphaWithDashRule.MESSAGE, EXCHANGE_NAME})
    private JTextField exchangeName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, BINDING_ID})
    @FieldValidation(rule = RuleRegistry.ALPHANUMERIC_WITH_UNDERSCORE,
            message = {AlphaWithDashRule.MESSAGE, BINDING_ID})
    private JTextField bindingId;

    // TODO: New validation rule
    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, BINDING_TOPIC})
    private JTextField bindingTopic;

    private JPanel contentPanel;
    private JButton buttonOK;
    private JButton buttonCancel;

    private final Project project;
    private final String moduleName;

    /**
     * Constructor.
     */
    public NewMessageQueueDialog(final Project project, final PsiDirectory directory) {
        super();

        this.project = project;
        this.moduleName = GetModuleNameByDirectoryUtil.execute(directory, project);

        setContentPane(contentPanel);
        setModal(true);
        setTitle(NewDataModelAction.ACTION_DESCRIPTION);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener((final ActionEvent event) -> onOK());
        buttonCancel.addActionListener((final ActionEvent event) -> onCancel());

        // call onCancel() on dialog close
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE KEY press
        contentPanel.registerKeyboardAction(
                (final ActionEvent event) -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
    }

    /**
     * Opens the dialog window.
     */
    public static void open(final Project project, final PsiDirectory directory) {
        final NewMessageQueueDialog dialog = new NewMessageQueueDialog(project, directory);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    @Override
    public void onCancel() {
        dispose();
    }

    private void onOK() {
        if (validateFormFields()) {
            generateCommunication();
            generateConsumer();
            generateTopology();
            generatePublisher();
            this.setVisible(false);
        }
    }

    private void generateCommunication() {
        new QueueCommunicationGenerator(project, new QueueCommunicationData(
                getTopicName(),
                getHandlerName(),
                getHandlerType(),
                getHandlerMethod(),
                getModuleName()
        )).generate(NewMessageQueueAction.ACTION_NAME, true);
    }

    private void generateConsumer() {
        new QueueConsumerGenerator(project, new QueueConsumerData(
                getConsumerName(),
                getQueueName(),
                getConsumerType(),
                getMaxMessages(),
                getConnectionName(),
                getModuleName()
        )).generate(NewMessageQueueAction.ACTION_NAME, true);
    }

    private void generateTopology() {
        new QueueTopologyGenerator(project, new QueueTopologyData(
                getExchangeName(),
                getConnectionName(),
                getBindingId(),
                getBindingTopic(),
                getQueueName(),
                getModuleName()
        )).generate(NewMessageQueueAction.ACTION_NAME, true);
    }

    private void generatePublisher() {
        new QueuePublisherGenerator(project, new QueuePublisherData(
                getTopicName(),
                getConnectionName(),
                getExchangeName(),
                getModuleName()
        )).generate(NewMessageQueueAction.ACTION_NAME, true);
    }

    public String getTopicName() {
        return topicName.getText().trim();
    }

    public String getHandlerName() {
        return handlerName.getText().trim();
    }

    public String getHandlerType() {
        return handlerType.getText().trim();
    }

    public String getHandlerMethod() {
        return "execute";
    }

    public String getConsumerName() {
        return consumerName.getText().trim();
    }

    public String getQueueName() {
        return queueName.getText().trim();
    }

    public String getConsumerType() {
        return consumerType.getText().trim();
    }

    public String getMaxMessages() {
        return maxMessages.getText().trim();
    }

    public String getConnectionName() {
        return connectionName.getText().trim();
    }

    public String getExchangeName() {
        return exchangeName.getText().trim();
    }

    public String getBindingId() {
        return bindingId.getText().trim();
    }

    public String getBindingTopic() {
        return bindingTopic.getText().trim();
    }

    public String getModuleName() {
        return moduleName;
    }
}
