/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

import java.util.Map;

@SuppressWarnings({"PMD.UnnecessaryModifier"})
public interface EavEntityDataInterface {

    void setCode(final String code);

    void setType(final String type);

    void setLabel(final String label);

    void setInput(final String input);

    void setNamespace(final String namespace);

    void setModuleName(final String moduleName);

    void setDirectory(final String directory);

    void setDataPatchName(final String dataPatchName);

    void setSource(final String source);

    void setSortOrder(final int sortOrder);

    void setOptions(final Map<Integer, String> options);

    void setOptionsSortOrder(final Map<Integer, String> optionsSortOrder);

    void setRequired(final boolean required);

    void setVisible(final boolean visible);

    void setBackendModel(final String model);

    String getCode();

    String getType();

    String getLabel();

    String getInput();

    String getNamespace();

    String getModuleName();

    String getDirectory();

    String getDataPatchName();

    String getEntityClass();

    String getSource();

    String getBackendModel();

    int getSortOrder();

    Map<Integer, String> getOptions();

    Map<Integer, String> getOptionsSortOrder();

    boolean isRequired();

    boolean isVisible();
}
