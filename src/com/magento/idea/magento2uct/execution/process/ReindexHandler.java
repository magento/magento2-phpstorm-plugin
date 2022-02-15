/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.execution.process;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2uct.execution.ReindexUctCommand;
import com.magento.idea.magento2uct.packages.IndexRegistry;
import com.magento.idea.magento2uct.packages.SupportedVersion;
import java.io.OutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ReindexHandler extends ProcessHandler {

    private final Project project;
    private final PsiDirectory directory;

    /**
     * Default indexing handler constructor.
     *
     * @param project Project
     * @param directory PsiDirectory
     * @param version SupportedVersion
     * @param index IndexRegistry
     */
    public ReindexHandler(
            final @NotNull Project project,
            final @NotNull PsiDirectory directory,
            final @NotNull SupportedVersion version,
            final @NotNull IndexRegistry index
    ) {
        super();
        this.project = project;
        this.directory = directory;
        this.addProcessListener(
                new ProcessAdapter() {
                    @Override
                    public void startNotified(final @NotNull ProcessEvent event) {
                        ReindexHandler.this.execute(version, index);
                    }
                }
        );
    }

    /**
     * Run indexing process.
     *
     * @param version SupportedVersion
     * @param index IndexRegistry
     */
    public void execute(
            final @NotNull SupportedVersion version,
            final @NotNull IndexRegistry index
    ) {
        final ReindexUctCommand command = new ReindexUctCommand(
                project,
                directory,
                new OutputWrapper(this),
                this
        );
        command.execute(version, index);
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
