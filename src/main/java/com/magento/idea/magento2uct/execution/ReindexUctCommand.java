/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.execution;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2uct.execution.process.OutputWrapper;
import com.magento.idea.magento2uct.execution.scanner.ModuleFilesScanner;
import com.magento.idea.magento2uct.execution.scanner.ModuleScanner;
import com.magento.idea.magento2uct.execution.scanner.data.ComponentData;
import com.magento.idea.magento2uct.packages.IndexRegistry;
import com.magento.idea.magento2uct.packages.SupportedVersion;
import org.jetbrains.annotations.NotNull;

public class ReindexUctCommand {

    private final Project project;
    private final PsiDirectory directory;
    private final OutputWrapper output;
    private final ProcessHandler process;

    /**
     * Command constructor.
     *
     * @param project Project
     * @param directory PsiDirectory
     * @param output OutputWrapper
     */
    public ReindexUctCommand(
            final @NotNull Project project,
            final @NotNull PsiDirectory directory,
            final @NotNull OutputWrapper output,
            final @NotNull ProcessHandler process
    ) {
        this.project = project;
        this.directory = directory;
        this.output = output;
        this.process = process;

        this.process.addProcessListener(new ProcessAdapter() {

            @Override
            public void processTerminated(final @NotNull ProcessEvent event) {
                super.processTerminated(event);
                output.write("\nProcess finished with exit code " + event.getExitCode() + "\n");
            }
        });
    }

    /**
     * Execute command.
     *
     * @param version SupportedVersion
     * @param index IndexRegistry
     */
    @SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops"})
    public void execute(
            final @NotNull SupportedVersion version,
            final @NotNull IndexRegistry index
    ) {
        if (project.getBasePath() == null) {
            return;
        }
        output.write("Indexing process...\n\n");

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            ApplicationManager.getApplication().runReadAction(() -> {
                index.getProcessor().clearData();

                for (final ComponentData componentData : new ModuleScanner(directory)) {
                    if (process.isProcessTerminated()) {
                        return;
                    }
                    output.print(output.wrapInfo(componentData.getName()).concat("\n"));

                    for (final PsiFile psiFile : new ModuleFilesScanner(componentData)) {
                        index.getProcessor().process(psiFile);
                    }
                }
                index.getProcessor().save(project.getBasePath(), version);

                process.destroyProcess();
            });
        });
    }
}
