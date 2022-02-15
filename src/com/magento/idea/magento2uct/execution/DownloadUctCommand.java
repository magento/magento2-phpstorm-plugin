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
import com.intellij.execution.ui.ConsoleView;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.wm.RegisterToolWindowTask;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.magento.idea.magento2plugin.MagentoIcons;
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
        final ConsoleView consoleView = createConsole();
        final OSProcessHandler processHandler = createProcessHandler();
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
                            AllIcons.Actions.Execute,
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
        content.putUserData(ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);
        content.setIcon(MagentoIcons.PLUGIN_ICON_SMALL);
        toolWindow.getContentManager().addContent(content);
        toolWindow.show();

        return consoleView;
    }
}
