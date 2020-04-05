package com.magento.idea.magento2plugin.actions.generation.data;

/**
 * NewBlockData contains suggested information about new block
 */
public class NewBlockData {
    private String name;
    private String area;
    private String module;
    private String parentDirectory;

    public NewBlockData(String name, String area, String module, String parentDirectory) {
        this.name = name;
        this.area = area;
        this.module = module;
        this.parentDirectory = parentDirectory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getParentDirectory() {
        return parentDirectory;
    }

    public void setParentDirectory(String parentDirectory) {
        this.parentDirectory = parentDirectory;
    }
}
