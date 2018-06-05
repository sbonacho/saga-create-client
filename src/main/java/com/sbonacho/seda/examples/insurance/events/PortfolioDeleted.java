package com.sbonacho.seda.examples.insurance.events;

import java.util.UUID;

public class PortfolioDeleted extends AbstractEvent {

    public UUID getClientId() {
        return clientId;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    private UUID clientId;
    
}
