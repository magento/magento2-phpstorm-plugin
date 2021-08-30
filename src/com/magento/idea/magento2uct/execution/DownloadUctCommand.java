/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.execution;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandlerFactory;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.RegisterToolWindowTask;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
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
    public void execute() {
        final ConsoleView consoleView = createConsole();
        try {
            final OSProcessHandler processHandler = createProcessHandler();
            consoleView.attachToProcess(processHandler);
            processHandler.startNotify();
        } catch (ExecutionException exception) {
            //NOPMD
        }
    }

    /**
     * Create process handler.
     *
     * @return OSProcessHandler
     */
    private OSProcessHandler createProcessHandler() throws ExecutionException {
        final GeneralCommandLine commandLine = new GeneralCommandLine();
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
     * Create console view.
     *
     * @return ConsoleView
     */
    private ConsoleView createConsole() {
        ToolWindow toolWindow = ToolWindowManager
                .getInstance(project)
                .getToolWindow(ToolWindowId.RUN);

        if (toolWindow == null) {
            toolWindow = ToolWindowManager.getInstance(project).registerToolWindow(
                    new RegisterToolWindowTask(
                            ToolWindowId.RUN,
                            ToolWindowAnchor.BOTTOM,
                            null,
                            false,
                            true,
                            true,
                            true,
                            null,
                            null,
                            null
                    )
            );
        }
        final ConsoleView consoleView = TextConsoleBuilderFactory.getInstance()
                .createBuilder(project)
                .getConsole();
        final Content content = toolWindow
                .getContentManager()
                .getFactory()
                .createContent(consoleView.getComponent(), TAB_TITLE, true);
        toolWindow.getContentManager().addContent(content);
        toolWindow.show();

        return consoleView;
    }
}
