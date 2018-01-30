package com.soprasteria.seda.examples.insurance.persistence;

import com.soprasteria.seda.examples.insurance.model.CreateClientSaga;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Service
public class CreateClientSagaRepositoryInMemory implements CreateClientSagaRepository{

    private Map<UUID, CreateClientSaga> store = new HashMap<>();
    private Map<UUID, UUID> byClient = new HashMap<>();
    private Map<UUID, UUID> byPortfolio = new HashMap<>();

    @Override
    public CreateClientSaga findByIdClient(UUID idClient) {
        return store.get(byClient.get(idClient));
    }

    @Override
    public CreateClientSaga findByIdPortfolio(UUID idPortfolio) {
        return store.get(byPortfolio.get(idPortfolio));
    }

    @Override
    public CreateClientSaga create(CreateClientSaga saga) {
        if (saga.getIdClientCreated() != null) byClient.put(saga.getIdClientCreated(), saga.getId());
        if (saga.getIdPortfolioStored() != null) byClient.put(saga.getIdPortfolioStored(), saga.getId());
        return store.put(saga.getId(), saga);
    }

    @Override
    public CreateClientSaga update(CreateClientSaga saga) {
        if (saga.getIdClientCreated() != null) byClient.put(saga.getIdClientCreated(), saga.getId());
        if (saga.getIdPortfolioStored() != null) byClient.put(saga.getIdPortfolioStored(), saga.getId());
        return store.replace(saga.getId(), saga);
    }

    @Override
    public CreateClientSaga delete(CreateClientSaga saga) {
        if (saga.getIdClientCreated() != null) byClient.remove(saga.getIdClientCreated());
        if (saga.getIdPortfolioStored() != null) byClient.remove(saga.getIdPortfolioStored());
        return store.remove(saga.getId());
    }
}