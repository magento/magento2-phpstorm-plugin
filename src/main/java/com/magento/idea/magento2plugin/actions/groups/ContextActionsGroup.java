/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.groups;

import com.intellij.ide.actions.NonEmptyActionGroup;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ContextActionsGroup extends NonEmptyActionGroup {

    @Override
    public void update(final @NotNull AnActionEvent event) {
        if (getChildrenCount() > 0) {
            final AnAction[] actions = getChildren(event);
            final List<AnAction> originalActionList = new LinkedList<>(Arrays.asList(actions));
            final List<AnAction> sortedActionList = new LinkedList<>(Arrays.asList(actions));
            sortedActionList.sort(new ContextActionsComparator());

            if (!originalActionList.equals(sortedActionList)) {
                removeAll();
                addAll(sortedActionList.toArray(new AnAction[0]));
            }
        }

        super.update(event);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    private static class ContextActionsComparator implements Comparator<AnAction> {

        @Override
        public int compare(final AnAction action1, final AnAction action2) {
            if (action1.getTemplateText() == null || action2.getTemplateText() == null) {
                return 0;
            }

            return action1.getTemplateText().compareTo(action2.getTemplateText());
        }
    }
}
