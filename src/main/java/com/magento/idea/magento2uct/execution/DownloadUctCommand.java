/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.execution;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.PtyCommandLine;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandlerFactory;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.execution.Executor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NotNull;

public final class DownloadUctCommand {

    private static final String TAB_TITLE = "Create UCT project";
    private final Project project;

    /**
     * Download UCT command constructor.
     *
     * @param project Project
     */
    public DownloadUctCommand(final @NotNull Project project) {
        this.project = project;
    }

    /**
     * Start UCT downloading process.
     */
    public void execute() throws ExecutionException {
        final OSProcessHandler processHandler = createProcessHandler();
        final ConsoleView consoleView = createConsole(processHandler);
        consoleView.attachToProcess(processHandler);
        processHandler.startNotify();
    }

    /**
     * Create process handler.
     *
     * @return OSProcessHandler
     */
    private OSProcessHandler createProcessHandler() throws ExecutionException {
        final GeneralCommandLine commandLine = createGeneralCommandLine(true);
        commandLine.setWorkDirectory(project.getBasePath());
        commandLine.setExePath("composer");
        commandLine.addParameters(
                "create-project",
                "magento/upgrade-compatibility-tool",
                "uct",
                "--repository",
                "https://repo.magento.com",
                "--ansi"
        );

        final OSProcessHandler processHandler = ProcessHandlerFactory
                .getInstance()
                .createColoredProcessHandler(commandLine);

        ProcessTerminatedListener.attach(processHandler);

        return processHandler;
    }

    /**
     * Create general command line.
     *
     * @param withPty boolean
     *
     * @return GeneralCommandLine
     */
    private GeneralCommandLine createGeneralCommandLine(final boolean withPty) {
        GeneralCommandLine commandLine;

        if (withPty) {
            if (SystemInfo.isWindows) {
                commandLine = new GeneralCommandLine();
                commandLine.getEnvironment().putIfAbsent("TERM", "xterm");
            } else {
                commandLine = new PtyCommandLine().withInitialColumns(2500);
            }
        } else {
            commandLine = new GeneralCommandLine();
        }

        return commandLine;
    }

    /**
     * Create console view.
     *
     * @return ConsoleView
     */
    private ConsoleView createConsole(final @NotNull OSProcessHandler processHandler) {
        final ConsoleView consoleView = TextConsoleBuilderFactory.getInstance()
                .createBuilder(project)
                .getConsole();
        final RunContentDescriptor descriptor = new RunContentDescriptor(
                consoleView,
                processHandler,
                consoleView.getComponent(),
                TAB_TITLE
        );
        final Executor runExecutorInstance = DefaultRunExecutor.getRunExecutorInstance();

        descriptor.setActivateToolWindowWhenAdded(true);
        descriptor.setAutoFocusContent(true);

        RunContentManager.getInstance(project).showRunContent(runExecutorInstance, descriptor);

        return consoleView;
    }
}
