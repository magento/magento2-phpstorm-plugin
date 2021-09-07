/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.execution;

import com.intellij.execution.Executor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;

public class RunAnalysisExecutor implements Disposable {

    private static final String RUN_CONTENT_TITLE = "UCT analysis";

    private final Project myProject;
    private final ProcessHandler myProcess;

    /**
     * Executor constructor.
     *
     * @param project Project
     * @param process ProcessHandler
     */
    public RunAnalysisExecutor(
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

        ConsoleView view = createConsole();
        view.attachToProcess(myProcess);

        myProcess.startNotify();
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
        // TODO: Add filters.
        final ConsoleView console = consoleBuilder.getConsole();
        final JComponent consolePanel = createConsolePanel(console);
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
     *
     * @return JComponent
     */
    private static JComponent createConsolePanel(final ConsoleView view) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(view.getComponent(), BorderLayout.CENTER);

        return panel;
    }

    @Override
    public void dispose() {
    }
}
