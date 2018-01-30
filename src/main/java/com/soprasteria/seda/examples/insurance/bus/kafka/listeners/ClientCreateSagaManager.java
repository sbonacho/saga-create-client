package com.soprasteria.seda.examples.insurance.bus.kafka.listeners;

import com.soprasteria.seda.examples.insurance.architecture.Saga;
import com.soprasteria.seda.examples.insurance.architecture.SagaManager;
import com.soprasteria.seda.examples.insurance.bus.producer.Sender;
import com.soprasteria.seda.examples.insurance.events.*;
import com.soprasteria.seda.examples.insurance.model.CreateClientSaga;
import com.soprasteria.seda.examples.insurance.persistence.CreateClientSagaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ClientCreateSagaManager implements SagaManager<CreateClientSaga> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientCreateSagaManager.class);

    @Autowired
    private CreateClientSagaRepository repository;

    @Autowired
    private Sender<AbstractEvent> sender;

    @Value("${connector.topics.saga}")
    private String outTopic;

    @Value("#{'${connector.topics.domains}'.split(',')}")
    private String[] domains;

    @KafkaListener(topics = "${connector.topics.app}")
    public void clientCreated(AbstractEvent e) {
        if (e instanceof ClientCreated) {
            ClientCreated event = (ClientCreated)e;
            LOGGER.info("ClientCreated Event Received -> {}", event);

            CreateClientSaga saga = repository.findByIdClient(event.getId());
            if (saga != null) {
                LOGGER.warn("Saga {} ALREADY EXITS!!", saga.getId());
            }

            saga = new CreateClientSaga(event.getId());
            saga.setName(event.getName());
            saga.setAddress(event.getAddress());
            saga.setInterest(event.getInterest());
            repository.create(saga);

            LOGGER.info("Saga '{}' Created!", saga.getId());
            this.check(saga);
        }
    }

    private Saga createSaga(AbstractEvent event) {
        CreateClientSaga saga = new CreateClientSaga(event.getId());
        repository.create(saga);
        return saga;
    }

    @KafkaListener(topics = "#{'${connector.topics.domains}'.split(',')}")
    public void domainEvents(AbstractEvent event) {

        if (event instanceof ClientStored) {
            LOGGER.info("ClientStored Event Received -> {}", event);

            CreateClientSaga saga = repository.findByIdClient(event.getId());
            if (saga != null) {
                saga.setIdClientStored(event.getId());
                repository.update(saga);
                LOGGER.info("Saga '{}' Client Stored!", saga.getId());
            } else {
                LOGGER.error("There is no Saga for {} client Id", event.getId());
                saga = new CreateClientSaga(event.getId());
                saga.setIdClientStored(event.getId());
                repository.create(saga);
                LOGGER.info("New Saga '{}' Created!", saga.getId());
            }
            this.check(saga);
        } else if (event instanceof PortfolioStored) {
            LOGGER.info("PortfolioStored Event Received -> {}", event);

            UUID idClient = ((PortfolioStored) event).getClientId();
            CreateClientSaga saga = repository.findByIdClient(((PortfolioStored) event).getClientId());
            if (saga != null) {
                saga.setIdPortfolioStored(event.getId());
                repository.update(saga);
                LOGGER.info("Saga '{}' Portfolio Stored!", saga.getId());
            } else {
                LOGGER.error("There is no Saga for {} client Id", idClient);
                saga = new CreateClientSaga(idClient);
                saga.setIdPortfolioStored(event.getId());
                repository.create(saga);
                LOGGER.info("New Saga '{}' Created!", saga.getId());
            }

            this.check(saga);
        } else if (event instanceof ClientFailed) {
            LOGGER.info("ClientFailed Event Received -> {}", event);

            CreateClientSaga saga = repository.findByIdClient(event.getId());
            saga.markAsFailed();

            PortfolioDeleted deleted = new PortfolioDeleted();
            deleted.setClientId(event.getId());
            for (String domain: domains)
                sender.send(deleted, domain);

            this.check(saga);
        } else if (event instanceof PortfolioFailed) {
            LOGGER.info("PortfolioFailed Event Received -> {}", event);

            CreateClientSaga saga = repository.findByIdClient(((PortfolioStored) event).getClientId());
            saga.markAsFailed();

            ClientDeleted deleted = new ClientDeleted();
            deleted.setId(event.getId());
            for (String domain: domains)
                sender.send(deleted, domain);

            this.check(saga);
        }
    }

    @Override
    public void check(CreateClientSaga saga) {
        LOGGER.info("Checking Saga -> {}", saga);
        if (saga.isFailed()) {
            ClientPortfolioFailed failed = new ClientPortfolioFailed();
            failed.setId(saga.getIdClientStored());
            failed.setName(saga.getName());

            sender.send(failed, outTopic);
            LOGGER.info("Saga '{}' Failed!", saga.getId());
            repository.delete(saga);
        } else if (saga.isComplete()) {
            ClientPortfolioCompleted completed = new ClientPortfolioCompleted();
            completed.setClientId(saga.getIdClientStored());
            completed.setName(saga.getName());
            completed.setAddress(saga.getAddress());
            completed.setInterest(saga.getInterest());
            completed.setPortfolioId(saga.getIdPortfolioStored());

            sender.send(completed, outTopic);
            LOGGER.info("Saga '{}' Completed!", saga.getId());
            repository.delete(saga);
        }
    }
}
