/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.execution.process;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.project.Project;
import com.magento.idea.magento2uct.execution.GenerateUctReportCommand;
import java.io.OutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultAnalysisHandler extends ProcessHandler {

    private final Project project;

    /**
     * Default analysis handler constructor.
     *
     * @param project Project
     */
    public DefaultAnalysisHandler(final @NotNull Project project) {
        super();
        this.project = project;
        this.addProcessListener(
                new ProcessAdapter() {
                    @Override
                    public void startNotified(final @NotNull ProcessEvent event) {
                        DefaultAnalysisHandler.this.execute();
                    }
                }
        );
    }

    /**
     * Run the main analysis process.
     */
    public void execute() {
        final GenerateUctReportCommand command = new GenerateUctReportCommand(
                project,
                new OutputWrapper(this),
                this
        );
        command.execute();
    }

    @Override
    protected void destroyProcessImpl() {
        notifyProcessTerminated(0);
    }

    @Override
    protected void detachProcessImpl() {
        notifyProcessDetached();
    }

    @Override
    public boolean detachIsDefault() {
        return false;
    }

    @Override
    public @Nullable OutputStream getProcessInput() {
        return null;
    }
}
