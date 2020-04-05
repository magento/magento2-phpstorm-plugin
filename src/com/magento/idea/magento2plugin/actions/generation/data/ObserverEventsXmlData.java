package com.magento.idea.magento2plugin.actions.generation.data;

public class ObserverEventsXmlData {
    private String area;
    private String observerModule;
    private String targetEvent;
    private String observerName;
    private String observerClassFqn;

    public ObserverEventsXmlData(
            String area,
            String observerModule,
            String targetEvent,
            String observerName,
            String observerClassFqn
    ) {
        this.area = area;
        this.observerModule = observerModule;
        this.targetEvent = targetEvent;
        this.observerName = observerName;
        this.observerClassFqn = observerClassFqn;
    }

    public String getObserverModule() {
        return observerModule;
    }

    public String getObserverClassFqn() {
        return observerClassFqn;
    }

    public String getTargetEvent() {
        return targetEvent;
    }

    public String getArea() {
        return area;
    }

    public String getObserverName() {
        return observerName;
    }
}
