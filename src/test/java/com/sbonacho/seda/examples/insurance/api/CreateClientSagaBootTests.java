package com.sbonacho.seda.examples.insurance.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbonacho.seda.examples.insurance.events.ClientCreated;
import com.sbonacho.seda.examples.insurance.events.ClientPortfolioCompleted;
import com.sbonacho.seda.examples.insurance.events.ClientStored;
import com.sbonacho.seda.examples.insurance.events.PortfolioStored;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.config.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, topics = { CreateClientSagaBootTests.saga })
public class CreateClientSagaBootTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateClientSagaBootTests.class);

	private static final String app = "createClient";
	protected static final String domain1 = "production";
	protected static final String domain2 = "comercial";
    protected static final String saga = "saga";

	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;

    private ObjectMapper mapper = new ObjectMapper();

	private KafkaMessageListenerContainer<String, Object> container;

	private BlockingQueue<ConsumerRecord<String, Object>> records;

	@Autowired
	private KafkaEmbedded embeddedKafka;

	@Before
	public void setUp() throws Exception {
		// set up the Kafka consumer properties
		Map<String, Object> consumerProperties = KafkaTestUtils.consumerProps("testSaga", "false", embeddedKafka);
		consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

		// create a Kafka consumer factory
		JsonDeserializer des = new JsonDeserializer<>(Object.class);
		des.addTrustedPackages("com.sbonacho.seda.examples.insurance.events");
		DefaultKafkaConsumerFactory<String, Object> consumerFactory = new DefaultKafkaConsumerFactory<String, Object>(consumerProperties, new StringDeserializer(), des);

		// set the topic that needs to be consumed
		ContainerProperties containerProperties = new ContainerProperties(saga);

		// create a Kafka MessageListenerContainer
		container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);

		// create a thread safe queue to store the received message
		records = new LinkedBlockingQueue<>();

		// setup a Kafka message listener
		container.setupMessageListener(new MessageListener<String, Object>() {
			@Override
			public void onMessage(ConsumerRecord<String, Object> record) {
			    LOGGER.info("Tests Listener -> topic: {} - {}", record.topic(), record.value());
			    records.add(record);
			}
		});

		// start the container and underlying message listener
		container.start();

        // wait until the container has the required number of assigned partitions
        //for (MessageListenerContainer messageListenerContainer : kafkaListenerEndpointRegistry.getListenerContainers()) {
        ContainerTestUtils.waitForAssignment(container, embeddedKafka.getPartitionsPerTopic());
        //}
	}

	@After
	public void tearDown() {
		// stop the container
		container.stop();
	}

	@Test
	public void clientCreateCompleteSaga() throws Exception {

		ClientCreated created = mapper.readValue("{\"type\": \"ClientCreated\", \"name\": \"John Doe\", \"address\": \"Bendford st 10\", \"interest\": \"Microservices\", \"id\":\"82565d32-45ea-40c5-9f56-9d2a93a648a0\"}", ClientCreated.class);
        ClientStored clientStored = mapper.readValue("{\"type\": \"ClientStored\", \"name\": \"John Doe\", \"address\": \"Bendford st 10\", \"interest\": \"Microservices\", \"id\":\"82565d32-45ea-40c5-9f56-9d2a93a648a0\"}", ClientStored.class);
        PortfolioStored portfolioStored = mapper.readValue("{\"type\": \"PortfolioSored\", \"name\": \"John Doe\", \"address\": \"Bendford st 10\", \"interest\": \"Microservices\", \"id\":\"ccaaf032-32fe-4476-9ef2-9a70d18a58b0\",\"clientId\":\"82565d32-45ea-40c5-9f56-9d2a93a648a0\"}", PortfolioStored.class);
		String exceptedType = "ClientPortfolioCompleted";

		// Send mocked events --------------------

        Thread.sleep(100);
		kafkaTemplate.send(app, created);
		Thread.sleep(100);
        kafkaTemplate.send(domain1, clientStored);
        Thread.sleep(100);
        kafkaTemplate.send(domain2, portfolioStored);

		// Check results events

		ConsumerRecord<String, Object> rec = records.poll(10, TimeUnit.SECONDS);

		assertThat(rec).isNotNull();

        ClientPortfolioCompleted completed = (ClientPortfolioCompleted) rec.value();

		assertThat(completed).isNotNull();
		assertThat(completed.getType()).isEqualTo(exceptedType);
		assertThat(completed.getClientId()).isEqualByComparingTo(created.getId());
		assertThat(completed.getName()).isEqualTo(created.getName());
        assertThat(completed.getAddress()).isEqualTo(created.getAddress());
        assertThat(completed.getInterest()).isEqualTo(created.getInterest());

	}
}
