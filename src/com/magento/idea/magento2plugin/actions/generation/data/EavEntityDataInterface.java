package com.magento.idea.magento2plugin.actions.generation.data;

@SuppressWarnings({"PMD.UnnecessaryModifier"})
public interface EavEntityDataInterface {
    public String getCode();

    public String getType();

    public String getLabel();

    public String getInput();

    public String getNamespace();

    public String getModuleName();

    public String getDirectory();

    public String getDataPatchName();

    public String getEntityClass();
}
