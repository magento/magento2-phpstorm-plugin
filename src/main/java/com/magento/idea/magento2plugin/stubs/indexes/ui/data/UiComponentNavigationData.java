/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.stubs.indexes.ui.data;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UiComponentNavigationData {
    public static final String KIND_DISPLAY_AREA = "displayArea";
    public static final String KIND_TEMPLATE = "template";
    public static final String KIND_CHILD_TEMPLATE = "childTemplate";
    public static final String KIND_TEMPLATES = "templates";
    public static final String KIND_ELEMENT_TEMPLATE = "elementTmpl";
    public static final String KIND_GET_REGION = "getRegion";
    public static final String KIND_COMPONENT = "component";

    private final String fileUrl;
    private final String kind;
    private final String value;
    private final String componentName;
    private final String parentComponentName;
    private final String componentJsPath;
    private final String parentComponentJsPath;
    private final String templateKey;
    private final int valueOffset;

    public UiComponentNavigationData(
            final @NotNull String fileUrl,
            final @NotNull String kind,
            final @NotNull String value,
            final @Nullable String componentName,
            final @Nullable String parentComponentName,
            final @Nullable String componentJsPath,
            final @Nullable String parentComponentJsPath,
            final @Nullable String templateKey,
            final int valueOffset
    ) {
        this.fileUrl = fileUrl;
        this.kind = kind;
        this.value = value;
        this.componentName = componentName;
        this.parentComponentName = parentComponentName;
        this.componentJsPath = componentJsPath;
        this.parentComponentJsPath = parentComponentJsPath;
        this.templateKey = templateKey;
        this.valueOffset = valueOffset;
    }

    public @NotNull String getFileUrl() {
        return fileUrl;
    }

    public @NotNull String getKind() {
        return kind;
    }

    public @NotNull String getValue() {
        return value;
    }

    public @Nullable String getComponentName() {
        return componentName;
    }

    public @Nullable String getParentComponentName() {
        return parentComponentName;
    }

    public @Nullable String getComponentJsPath() {
        return componentJsPath;
    }

    public @Nullable String getParentComponentJsPath() {
        return parentComponentJsPath;
    }

    public @Nullable String getTemplateKey() {
        return templateKey;
    }

    public int getValueOffset() {
        return valueOffset;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UiComponentNavigationData)) {
            return false;
        }
        final UiComponentNavigationData that = (UiComponentNavigationData) o;

        return valueOffset == that.valueOffset
                && fileUrl.equals(that.fileUrl)
                && kind.equals(that.kind)
                && value.equals(that.value)
                && Objects.equals(componentName, that.componentName)
                && Objects.equals(parentComponentName, that.parentComponentName)
                && Objects.equals(componentJsPath, that.componentJsPath)
                && Objects.equals(parentComponentJsPath, that.parentComponentJsPath)
                && Objects.equals(templateKey, that.templateKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                fileUrl,
                kind,
                value,
                componentName,
                parentComponentName,
                componentJsPath,
                parentComponentJsPath,
                templateKey,
                valueOffset
        );
    }
}
