package com.sbonacho.seda.examples.insurance.persistence;

import com.sbonacho.seda.examples.insurance.model.CreateClientSaga;

import java.util.UUID;

public interface CreateClientSagaRepository {
    CreateClientSaga findByIdClient(UUID idClient);
    CreateClientSaga findByIdPortfolio(UUID idPortfolio);
    CreateClientSaga create(CreateClientSaga saga);
    CreateClientSaga update(CreateClientSaga saga);
    CreateClientSaga delete(CreateClientSaga saga);
}
