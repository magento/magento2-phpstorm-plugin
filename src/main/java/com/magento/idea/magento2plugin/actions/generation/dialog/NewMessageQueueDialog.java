/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.ui.DocumentAdapter;
import com.magento.idea.magento2plugin.actions.generation.NewMessageQueueAction;
import com.magento.idea.magento2plugin.actions.generation.data.MessageQueueClassData;
import com.magento.idea.magento2plugin.actions.generation.data.QueueCommunicationData;
import com.magento.idea.magento2plugin.actions.generation.data.QueueConsumerData;
import com.magento.idea.magento2plugin.actions.generation.data.QueuePublisherData;
import com.magento.idea.magento2plugin.actions.generation.data.QueueTopologyData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.AlphaWithDashRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.AlphaWithPeriodRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.AlphanumericWithUnderscoreRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.DirectoryRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.Lowercase;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NumericRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.PhpClassFqnRule;
import com.magento.idea.magento2plugin.actions.generation.generator.MessageQueueClassGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.QueueCommunicationGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.QueueConsumerGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.QueuePublisherGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.QueueTopologyGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import com.magento.idea.magento2plugin.magento.files.MessageQueueClassPhp;
import com.magento.idea.magento2plugin.magento.packages.MessageQueueConnections;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({
        "PMD.TooManyFields",
        "PMD.TooManyMethods",
        "PMD.ExcessiveImports",
})
public class NewMessageQueueDialog extends AbstractDialog {

    private static final String TOPIC_NAME = "Topic Name";
    private static final String HANDLER_NAME = "Handler Name";
    private static final String HANDLER_TYPE = "Handler Class";
    private static final String HANDLER_DIR = "Handler Directory";
    private static final String CONSUMER_NAME = "Consumer Name";
    private static final String QUEUE_NAME = "Queue Name";
    private static final String CONSUMER_TYPE = "Consumer Class";
    private static final String CONSUMER_DIR = "Consumer Directory";
    private static final String MAX_MESSAGES = "Maximum Messages";
    private static final String EXCHANGE_NAME = "Exchange Name";
    private static final String BINDING_ID = "Binding ID";
    private static final String BINDING_TOPIC = "Binding Topic";

    private JComboBox connectionName;
    private final Project project;
    private final String moduleName;

    /* TODO: Improve validation */
    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, TOPIC_NAME})
    @FieldValidation(rule = RuleRegistry.ALPHA_WITH_PERIOD,
            message = {AlphaWithPeriodRule.MESSAGE, TOPIC_NAME})
    @FieldValidation(rule = RuleRegistry.LOWERCASE, message = {Lowercase.MESSAGE, TOPIC_NAME})
    private JTextField topicName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, HANDLER_NAME})
    @FieldValidation(rule = RuleRegistry.ALPHA_WITH_PERIOD,
            message = {AlphanumericWithUnderscoreRule.MESSAGE, HANDLER_NAME})
    private JTextField handlerName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, HANDLER_TYPE})
    @FieldValidation(rule = RuleRegistry.PHP_CLASS,
            message = {PhpClassFqnRule.MESSAGE, HANDLER_TYPE})
    private JTextField handlerClass;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, CONSUMER_NAME})
    @FieldValidation(rule = RuleRegistry.ALPHA_WITH_PERIOD,
            message = {AlphaWithPeriodRule.MESSAGE, CONSUMER_NAME})
    @FieldValidation(rule = RuleRegistry.LOWERCASE, message = {Lowercase.MESSAGE, CONSUMER_NAME})
    private JTextField consumerName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, QUEUE_NAME})
    @FieldValidation(rule = RuleRegistry.ALPHA_WITH_PERIOD,
            message = {AlphaWithPeriodRule.MESSAGE, QUEUE_NAME})
    @FieldValidation(rule = RuleRegistry.LOWERCASE, message = {Lowercase.MESSAGE, QUEUE_NAME})
    private JTextField queueName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, CONSUMER_TYPE})
    @FieldValidation(rule = RuleRegistry.PHP_CLASS,
            message = {PhpClassFqnRule.MESSAGE, CONSUMER_TYPE})
    private JTextField consumerClass;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, MAX_MESSAGES})
    @FieldValidation(rule = RuleRegistry.NUMERIC, message = {NumericRule.MESSAGE, MAX_MESSAGES})
    private JTextField maxMessages;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, EXCHANGE_NAME})
    @FieldValidation(rule = RuleRegistry.ALPHA_WITH_DASH,
            message = {AlphaWithDashRule.MESSAGE, EXCHANGE_NAME})
    private JTextField exchangeName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, BINDING_ID})
    @FieldValidation(rule = RuleRegistry.ALPHANUMERIC_WITH_UNDERSCORE,
            message = {AlphaWithDashRule.MESSAGE, BINDING_ID})
    private JTextField bindingId;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, BINDING_TOPIC})
    @FieldValidation(rule = RuleRegistry.ALPHA_WITH_PERIOD,
            message = {AlphanumericWithUnderscoreRule.MESSAGE, BINDING_TOPIC})
    private JTextField bindingTopic;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, CONSUMER_DIR})
    @FieldValidation(rule = RuleRegistry.DIRECTORY, message = {DirectoryRule.MESSAGE, CONSUMER_DIR})
    private JTextField consumerDirectory;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, HANDLER_DIR})
    @FieldValidation(rule = RuleRegistry.DIRECTORY, message = {DirectoryRule.MESSAGE, HANDLER_DIR})
    private JTextField handlerDirectory;

    private JPanel contentPanel;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel consumerDirectoryLabel;
    private JLabel consumerClassLabel;
    private JLabel maxMessagesLabel;
    private JLabel bindingTopicLabel;//NOPMD
    private JLabel handlerClassLabel;//NOPMD
    private JLabel consumerNameLabel;//NOPMD
    private JLabel handlerDirectoryLabel;//NOPMD

    private JLabel topicNameErrorMessage;//NOPMD
    private JLabel handlerNameErrorMessage;//NOPMD
    private JLabel handlerClassErrorMessage;//NOPMD
    private JLabel consumerNameErrorMessage;//NOPMD
    private JLabel queueNameErrorMessage;//NOPMD
    private JLabel consumerClassErrorMessage;//NOPMD
    private JLabel maxMessagesErrorMessage;//NOPMD
    private JLabel exchangeNameErrorMessage;//NOPMD
    private JLabel bindingIdErrorMessage;//NOPMD
    private JLabel bindingTopicErrorMessage;//NOPMD
    private JLabel consumerDirectoryErrorMessage;//NOPMD
    private JLabel handlerDirectoryErrorMessage;//NOPMD

    /**
     * Constructor.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public NewMessageQueueDialog(
            final @NotNull Project project,
            final @NotNull PsiDirectory directory
    ) {
        super();

        this.project = project;
        this.moduleName = GetModuleNameByDirectoryUtil.execute(directory, project);

        setContentPane(contentPanel);
        setModal(false);
        setTitle(NewMessageQueueAction.ACTION_DESCRIPTION);
        getRootPane().setDefaultButton(buttonOK);

        for (final String connection : MessageQueueConnections.getList()) {
            connectionName.addItem(connection);
        }

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

        this.topicName.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(final @NotNull DocumentEvent event) {
                updateIdentifiersTexts();
            }
        });
        this.handlerClass.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(final @NotNull DocumentEvent event) {
                updateBindingText();
            }
        });

        connectionName.addActionListener(e -> toggleConsumer());

        addComponentListener(new FocusOnAFieldListener(() -> topicName.requestFocusInWindow()));
    }

    private void toggleConsumer() {
        if (getConnectionName().equals(MessageQueueConnections.AMPQ.getType())) {
            consumerDirectoryLabel.setVisible(false);
            consumerDirectory.setVisible(false);
            consumerClass.setVisible(false);
            consumerClassLabel.setVisible(false);
            maxMessages.setVisible(false);
            maxMessagesLabel.setVisible(false);
            return;
        }
        consumerDirectoryLabel.setVisible(true);
        consumerDirectory.setVisible(true);
        consumerClass.setVisible(true);
        consumerClassLabel.setVisible(true);
        maxMessages.setVisible(true);
        maxMessagesLabel.setVisible(true);
    }

    /**
     * Opens the dialog window.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public static void open(
            final @NotNull Project project,
            final @NotNull PsiDirectory directory
    ) {
        final NewMessageQueueDialog dialog = new NewMessageQueueDialog(project, directory);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    private void onOK() {
        if (validateFormFields()) {
            generateCommunication();
            generateConsumer();
            generateTopology();
            generatePublisher();
            generateHandlerClass();

            if (getConnectionName().equals(MessageQueueConnections.DB.getType())) {
                generateConsumerClass();
            }
            exit();
        }
    }

    private void generateCommunication() {
        new QueueCommunicationGenerator(project, new QueueCommunicationData(
                getTopicName(),
                getHandlerName(),
                getHandlerClass(),
                getHandlerMethod(),
                getModuleName()
        )).generate(NewMessageQueueAction.ACTION_NAME, true);
    }

    private void generateConsumer() {
        new QueueConsumerGenerator(project, new QueueConsumerData(
                getConsumerName(),
                getQueueName(),
                getConsumerClass(),
                getMaxMessages(),
                getConnectionName(),
                getModuleName(),
                getHandlerClass().concat("::").concat(getHandlerMethod())
        )).generate(NewMessageQueueAction.ACTION_NAME, false);
    }

    private void generateTopology() {
        new QueueTopologyGenerator(project, new QueueTopologyData(
                getExchangeName(),
                getConnectionName(),
                getBindingId(),
                getBindingTopic(),
                getQueueName(),
                getModuleName()
        )).generate(NewMessageQueueAction.ACTION_NAME, false);
    }

    private void generatePublisher() {
        new QueuePublisherGenerator(project, new QueuePublisherData(
                getTopicName(),
                getConnectionName(),
                getExchangeName(),
                getModuleName()
        )).generate(NewMessageQueueAction.ACTION_NAME, false);
    }

    private void generateHandlerClass() {
        @NotNull final NamespaceBuilder handlerNamespaceBuilder = getHandlerNamespaceBuilder();
        new MessageQueueClassGenerator(new MessageQueueClassData(
            handlerClass.getText().trim(),
            handlerNamespaceBuilder.getNamespace(),
            handlerDirectory.getText().trim(),
            handlerNamespaceBuilder.getClassFqn(),
            MessageQueueClassPhp.Type.HANDLER
        ), getModuleName(), project).generate(NewMessageQueueAction.ACTION_NAME, false);
    }

    private void generateConsumerClass() {
        @NotNull final NamespaceBuilder consumerNamespaceBuilder = getConsumerNamespaceBuilder();
        new MessageQueueClassGenerator(new MessageQueueClassData(
            consumerClass.getText().trim(),
            consumerNamespaceBuilder.getNamespace(),
            consumerDirectory.getText().trim(),
            consumerNamespaceBuilder.getClassFqn(),
            MessageQueueClassPhp.Type.CONSUMER
        ), getModuleName(), project).generate(NewMessageQueueAction.ACTION_NAME, false);
    }

    public String getTopicName() {
        return topicName.getText().trim();
    }

    public String getHandlerName() {
        return handlerName.getText().trim();
    }

    @NotNull
    private NamespaceBuilder getHandlerNamespaceBuilder() {
        return new NamespaceBuilder(
            getModuleName(),
            handlerClass.getText().trim(),
            handlerDirectory.getText().trim()
        );
    }

    @NotNull
    private NamespaceBuilder getConsumerNamespaceBuilder() {
        return new NamespaceBuilder(
            getModuleName(),
            consumerClass.getText().trim(),
            consumerDirectory.getText().trim()
        );
    }

    public String getHandlerClass() {
        return getHandlerNamespaceBuilder().getClassFqn();
    }

    public String getConsumerClass() {
        return getConsumerNamespaceBuilder().getClassFqn();
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

    public String getMaxMessages() {
        return maxMessages.getText().trim();
    }

    public String getConnectionName() {
        return connectionName.getSelectedItem().toString();
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

    /**
     * Update identifier texts.
     */
    public void updateIdentifiersTexts() {
        final String topicNameText = this.topicName.getText();
        this.handlerName.setText(topicNameText.concat(".handler"));
        this.consumerName.setText(topicNameText);
        this.queueName.setText(topicNameText);
        this.bindingTopic.setText(topicNameText);
    }

    /**
     * Update identifier texts.
     */
    public void updateBindingText() {
        final String handlerTypeText = this.handlerClass.getText();
        this.consumerClass.setText(handlerTypeText.replace("Handler", "").concat("Consumer"));
        this.bindingId.setText(handlerTypeText.replace("Handler", "").concat("Binding"));
    }
}
