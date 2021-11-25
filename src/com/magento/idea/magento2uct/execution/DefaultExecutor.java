/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.execution;

import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.Executor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.magento.idea.magento2uct.execution.filters.UctPhpFileFilter;
import com.magento.idea.magento2uct.execution.filters.UctResultFileFilter;
import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;

public class DefaultExecutor implements Disposable {

    private static final String RUN_CONTENT_TITLE = "UCT analysis";

    private final Project myProject;
    private final ProcessHandler myProcess;

    /**
     * Executor constructor.
     *
     * @param project Project
     * @param process ProcessHandler
     */
    public DefaultExecutor(
            final @NotNull Project project,
            final @NotNull ProcessHandler process
    ) {
        myProject = project;
        myProcess = process;
    }

    /**
     * Run execution process.
     */
    public void run() {
        FileDocumentManager.getInstance().saveAllDocuments();

        final ConsoleView view = createConsole();
        view.attachToProcess(myProcess);

        myProcess.startNotify();
    }

    /**
     * Get process.
     *
     * @return ProcessHandler
     */
    public ProcessHandler getMyProcess() {
        return myProcess;
    }

    /**
     * Create console view.
     *
     * @return ConsoleView
     */
    private ConsoleView createConsole() {
        final TextConsoleBuilder consoleBuilder = TextConsoleBuilderFactory
                .getInstance()
                .createBuilder(myProject);
        consoleBuilder.addFilter(new UctPhpFileFilter(myProject));
        consoleBuilder.addFilter(new UctResultFileFilter(myProject));

        final ConsoleView console = consoleBuilder.getConsole();

        final DefaultActionGroup actions = new DefaultActionGroup();

        final JComponent consolePanel = createConsolePanel(console, actions);
        final RunContentDescriptor descriptor = new RunContentDescriptor(
                console,
                myProcess,
                consolePanel,
                RUN_CONTENT_TITLE
        );
        final Executor runExecutorInstance = DefaultRunExecutor.getRunExecutorInstance();

        descriptor.setActivateToolWindowWhenAdded(true);
        descriptor.setAutoFocusContent(true);

        Disposer.register(descriptor, this);
        Disposer.register(descriptor, console);
        actions.add(new StopAction());

        RunContentManager
                .getInstance(myProject)
                .showRunContent(
                        runExecutorInstance,
                        descriptor
                );

        return console;
    }

    /**
     * Create console panel.
     *
     * @param view ConsoleView
     * @param actionGroup DefaultActionGroup
     *
     * @return JComponent
     */
    private static JComponent createConsolePanel(
            final ConsoleView view,
            final DefaultActionGroup actionGroup
    ) {
        final JPanel panel = new JPanel();
        final ActionToolbar actionToolbar = ActionManager
                .getInstance()
                .createActionToolbar(
                        "RunContentExecutor",
                        actionGroup,
                        false
                );
        actionToolbar.setTargetComponent(panel);
        panel.setLayout(new BorderLayout());
        panel.add(view.getComponent(), BorderLayout.CENTER);
        panel.add(actionToolbar.getComponent(), BorderLayout.WEST);

        return panel;
    }

    @Override
    public void dispose() {//NOPMD
    }

    private class StopAction extends AnAction implements DumbAware {

        public StopAction() {
            super(
                    ExecutionBundle.messagePointer("action.AnAction.text.stop"),
                    ExecutionBundle.messagePointer("action.AnAction.description.stop"),
                    AllIcons.Actions.Suspend
            );
        }

        @Override
        public void actionPerformed(final @NotNull AnActionEvent event) {
            DefaultExecutor.this.getMyProcess().destroyProcess();
        }

        @Override
        public void update(final @NotNull AnActionEvent event) {
            event.getPresentation().setVisible(true);
            event.getPresentation().setEnabled(
                    !DefaultExecutor.this.getMyProcess().isProcessTerminated()
            );
        }
    }
}
