package com.soprasteria.seda.examples.insurance.events;

public class ClientFailed extends AbstractEvent {

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;

}
