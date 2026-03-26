/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.project.startup;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jetbrains.annotations.NotNull;

public final class DeferredProjectOpenActions {
    private static final Logger LOGGER = Logger.getInstance(DeferredProjectOpenActions.class);

    private final Project project;
    private final Queue<Runnable> pendingActions = new ConcurrentLinkedQueue<>();

    public DeferredProjectOpenActions(final @NotNull Project project) {
        this.project = project;
    }

    public static DeferredProjectOpenActions getInstance(final @NotNull Project project) {
        return project.getService(DeferredProjectOpenActions.class);
    }

    public void runAfterProjectOpened(final @NotNull Runnable action) {
        if (project.isDisposed()) {
            return;
        }
        if (project.isInitialized()) {
            runAction(action);
            return;
        }
        pendingActions.add(action);
    }

    public void runPendingActions() {
        Runnable action;
        while ((action = pendingActions.poll()) != null) {
            if (project.isDisposed()) {
                return;
            }
            runAction(action);
        }
    }

    private void runAction(final @NotNull Runnable action) {
        try {
            action.run();
        } catch (RuntimeException exception) {
            LOGGER.warn("Failed to run deferred post-open action", exception);
        }
    }
}
