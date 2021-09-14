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
import com.magento.idea.magento2uct.versioning.indexes.IndexRepository;
import com.magento.idea.magento2uct.versioning.indexes.data.DeprecationStateIndex;
import com.magento.idea.magento2uct.versioning.processors.DeprecationIndexProcessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     */
    public void execute(final @NotNull SupportedVersion version) {
        if (project.getBasePath() == null) {
            return;
        }
        output.write("Indexing process...\n\n");

        final IndexRepository<String, Boolean> indexRepository = new IndexRepository<>(
                project.getBasePath(),
                IndexRegistry.DEPRECATION
        );
        final Map<String, Boolean> deprecationData = new HashMap<>();

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            ApplicationManager.getApplication().runReadAction(() -> {
                for (final ComponentData componentData : new ModuleScanner(directory)) {
                    if (process.isProcessTerminated()) {
                        return;
                    }
                    output.print(output.wrapInfo(componentData.getName()).concat("\n"));

                    for (final PsiFile psiFile : new ModuleFilesScanner(componentData)) {
                        deprecationData.putAll(
                                new DeprecationIndexProcessor().process(psiFile)
                        );
                    }
                }

                if (!deprecationData.isEmpty()) {
                    final List<SupportedVersion> previousVersions = new ArrayList<>();

                    for (final SupportedVersion supportedVersion : SupportedVersion.values()) {
                        if (supportedVersion.compareTo(version) < 0) {
                            previousVersions.add(supportedVersion);
                        }
                    }
                    final DeprecationStateIndex deprecationIndex = new DeprecationStateIndex();
                    deprecationIndex.load(previousVersions);
                    final Map<String, Boolean> previousData = deprecationIndex.getIndexData();
                    deprecationData.entrySet().removeAll(previousData.entrySet());

                    if (!deprecationData.isEmpty()) {
                        indexRepository.put(deprecationData, version.getVersion());
                    }
                }

                process.destroyProcess();
            });
        });
    }
}
