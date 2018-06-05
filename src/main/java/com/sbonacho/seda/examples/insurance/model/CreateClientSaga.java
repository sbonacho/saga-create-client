package com.sbonacho.seda.examples.insurance.model;

import com.sbonacho.seda.examples.insurance.architecture.Saga;

import java.util.UUID;

public class CreateClientSaga implements Saga {

    private final UUID id;
    private final UUID idClientCreated;
    private UUID idClientStored;
    private UUID idPortfolioStored;
    private String name;
    private Boolean failed;
    private String address;
    private String interest;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    public UUID getIdClientCreated() {
        return idClientCreated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CreateClientSaga(UUID idClientCreated) {
        this.id = UUID.randomUUID();
        this.idClientCreated = idClientCreated;
        this.failed = false;
    }

    public void markAsFailed() {
        this.failed = true;
    }

    public UUID getId() {
        return id;
    }

    public UUID getIdClientStored() {
        return idClientStored;
    }

    public void setIdClientStored(UUID idClientStored) {
        this.idClientStored = idClientStored;
    }

    public UUID getIdPortfolioStored() {
        return idPortfolioStored;
    }

    public void setIdPortfolioStored(UUID idPortfolioStored) {
        this.idPortfolioStored = idPortfolioStored;
    }

    @Override
    public Boolean isComplete() {
        return idClientStored != null && idPortfolioStored != null;
    }

    @Override
    public Boolean isFailed() {
        return failed;
    }

    @Override
    public String toString() {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
