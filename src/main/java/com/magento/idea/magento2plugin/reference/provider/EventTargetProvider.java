/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.provider;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

/**
 * Contributes event targets from optional languages (e.g. PHP dispatch calls)
 * to event reference resolution in events.xml.
 */
public interface EventTargetProvider {
    ExtensionPointName<EventTargetProvider> EP_NAME = ExtensionPointName.create(
            "com.magento.idea.magento2plugin.eventTargetProvider"
    );

    void collectEventTargets(
            @NotNull Project project,
            @NotNull String eventName,
            @NotNull Collection<PsiElement> targets
    );
}
