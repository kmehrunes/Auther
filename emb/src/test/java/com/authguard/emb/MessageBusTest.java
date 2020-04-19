package com.authguard.emb;

import com.authguard.config.ConfigContext;
import com.authguard.config.JacksonConfigContext;
import com.authguard.emb.model.Message;
import com.authguard.emb.rxjava.RxPublisherFactory;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MessageBusTest {
    private MessageBus messageBus;

    private List<Message> receivedFromAccounts = new ArrayList<>();
    private List<Message> receivedFromAuth = new ArrayList<>();

    @BeforeAll
    void setup()  {
        final ObjectNode configNode = new ObjectNode(JsonNodeFactory.instance);

        configNode.set("channels", new ArrayNode(JsonNodeFactory.instance)
                .add("accounts")
                .add("auth")
        );

        final ConfigContext configContext = new JacksonConfigContext(configNode);

        messageBus = new MessageBus(new RxPublisherFactory(), configContext);
    }

    @Test
    void pubSub() throws InterruptedException {
        messageBus.subscribe("accounts", message -> receivedFromAccounts.add(message));
        messageBus.subscribe("auth", message -> receivedFromAuth.add(message));

        messageBus.publish("accounts", Message.builder()
                .messageBody("Accounts Message")
                .bodyType(String.class)
                .build()
        );

        messageBus.publish("auth", Message.builder()
                .messageBody("Auth Message")
                .bodyType(String.class)
                .build()
        );

        Thread.sleep(1000);

        assertThat(receivedFromAccounts).containsExactly(Message.builder()
                .messageBody("Accounts Message")
                .bodyType(String.class)
                .build());

        assertThat(receivedFromAuth).containsExactly(Message.builder()
                .messageBody("Auth Message")
                .bodyType(String.class)
                .build());
    }
}