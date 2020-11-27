package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.actions.generation.NewDataModelAction;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.AlphaWithDashRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.AlphaWithPeriodRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.AlphanumericWithUnderscoreRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.Lowercase;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NumericRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.PhpClassRule;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
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

public class NewMessageQueueDialog extends AbstractDialog {
    private static final String TOPIC_NAME = "Topic Name";
    private static final String HANDLER_NAME = "Handler Name";
    private static final String HANDLER_TYPE = "Handler Type";
    private static final String HANDLER_METHOD = "Handler Method";
    private static final String CONSUMER_NAME = "Consumer Name";
    private static final String MAX_MESSAGES = "Maximum Messages";
    private static final String CONSUMER_TYPE = "Consumer Type";
    private static final String CONNECTION_NAME = "Connection Name";
    private static final String EXCHANGE_NAME = "Exchange Name";
    private static final String QUEUE_NAME = "Queue Name";

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
    @FieldValidation(rule = RuleRegistry.PHP_CLASS,
            message = {PhpClassRule.MESSAGE, HANDLER_TYPE})
    private JTextField handlerType;

    /* TODO: Rule to validate PHP method? */
    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, HANDLER_METHOD})
    private JTextField handlerMethod;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, CONSUMER_NAME})
    @FieldValidation(rule = RuleRegistry.ALPHA_WITH_PERIOD,
            message = {AlphaWithPeriodRule.MESSAGE, CONSUMER_NAME})
    @FieldValidation(rule = RuleRegistry.LOWERCASE,
            message = {Lowercase.MESSAGE, CONSUMER_NAME})
    private JTextField consumerName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, MAX_MESSAGES})
    @FieldValidation(rule = RuleRegistry.NUMERIC,
            message = {NumericRule.MESSAGE, MAX_MESSAGES})
    private JTextField maxMessages;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, CONSUMER_TYPE})
    @FieldValidation(rule = RuleRegistry.PHP_CLASS,
            message = {PhpClassRule.MESSAGE, CONSUMER_TYPE})
    private JTextField consumerType;

    /* TODO: Can this be made a dropdown? */
    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, CONNECTION_NAME})
    @FieldValidation(rule = RuleRegistry.PHP_CLASS,
            message = {PhpClassRule.MESSAGE, CONNECTION_NAME})
    private JTextField connectionName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, EXCHANGE_NAME})
    @FieldValidation(rule = RuleRegistry.ALPHA_WITH_DASH,
            message = {AlphaWithDashRule.MESSAGE, EXCHANGE_NAME})
    private JTextField exchangeName;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, QUEUE_NAME})
    @FieldValidation(rule = RuleRegistry.ALPHA_WITH_PERIOD,
            message = {AlphaWithPeriodRule.MESSAGE, QUEUE_NAME})
    @FieldValidation(rule = RuleRegistry.LOWERCASE,
            message = {Lowercase.MESSAGE, QUEUE_NAME})
    private JTextField queueName;

    private JPanel contentPanel;
    private JButton buttonOK;
    private JButton buttonCancel;

    private final Project project;
    private final String moduleName;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;

    /**
     * Constructor.
     */
    public NewMessageQueueDialog(Project project, PsiDirectory directory) {
        super();

        this.project = project;
        this.moduleName = GetModuleNameByDirectoryUtil.execute(directory, project);
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();

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
    }

    private void generateConsumer() {
    }

    private void generateTopology() {
    }

    private void generatePublisher() {
    }
}
