/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import javax.swing.SwingWorker;
import org.jetbrains.annotations.NotNull;

/**
 * This worker is used to allow button on click action to be performed in the separate thread.
 * It releases UI to not be blocked.
 */
public final class ProcessWorker extends SwingWorker<Boolean, String> {

    private final Runnable processExecutorService;
    private final Runnable processReleaserService;
    private final InProgressFlag inProgressFlag;

    /**
     * Process worker constructor.
     *
     * @param processExecutorService Runnable
     * @param processReleaserService Runnable
     * @param inProgressFlag InProgressFlag
     */
    public ProcessWorker(
            final @NotNull Runnable processExecutorService,
            final @NotNull Runnable processReleaserService,
            final InProgressFlag inProgressFlag
    ) {
        super();
        this.processExecutorService = processExecutorService;
        this.processReleaserService = processReleaserService;
        this.inProgressFlag = inProgressFlag;
    }

    @Override
    protected Boolean doInBackground() {
        if (!inProgressFlag.isInProgress()) {
            inProgressFlag.setInProgress(true);
            // Run action event process
            ApplicationManager.getApplication().invokeAndWait(
                    processExecutorService,
                    ModalityState.defaultModalityState()
            );
            // Run release dialog action process
            ApplicationManager.getApplication().invokeAndWait(
                    processReleaserService,
                    ModalityState.defaultModalityState()
            );
        }
        return null;
    }

    /**
     * Inner class used only to stop actionPerformed method executing.
     */
    public static final class InProgressFlag {

        /**
         * Is action in progress flag.
         */
        private boolean inProgress;

        /**
         * Is action finished flag.
         */
        private boolean finished;

        /**
         * In progress flag constructor.
         *
         * @param inProgress boolean
         */
        public InProgressFlag(final boolean inProgress) {
            this.inProgress = inProgress;
            finished = false;
        }

        /**
         * Get is in progress value.
         *
         * @return boolean
         */
        public boolean isInProgress() {
            return inProgress;
        }

        /**
         * Set is in progress flag value.
         *
         * @param inProgress boolean
         */
        public void setInProgress(final boolean inProgress) {
            this.inProgress = inProgress;
        }

        /**
         * Get is progress finished flag value.
         */
        public boolean isFinished() {
            return finished;
        }

        /**
         * Set is progress finished flag value.
         *
         * @param finished boolean
         */
        public void setFinished(final boolean finished) {
            this.finished = finished;
        }
    }
}
