package com.sbonacho.seda.examples.insurance.events;

import java.util.UUID;

public class PortfolioStored extends AbstractEvent {

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public UUID getClientId() {
        return clientId;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    private String name;
    private UUID clientId;

}
