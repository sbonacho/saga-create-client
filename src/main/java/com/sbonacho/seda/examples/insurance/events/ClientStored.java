package com.sbonacho.seda.examples.insurance.events;

public class ClientStored extends AbstractEvent {

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;

}
