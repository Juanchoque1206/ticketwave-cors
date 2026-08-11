package com.ticketwave.ticketorder.infrastructure.config;

import com.ticketwave.domain.bus.CommandBus;
import com.ticketwave.domain.bus.EventBus;
import com.ticketwave.ticketorder.infrastructure.bus.LocalCommandBus;
import com.ticketwave.ticketorder.infrastructure.bus.LocalEventBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * In-memory bus doubles for development without a RabbitMQ broker. Default for
 * any profile except {@code rabbitmq} (real broker adapters) and {@code test}
 * (which supplies its own doubles via TestInfraConfig), so a CommandBus/EventBus
 * bean always exists when running locally with no active profile.
 */
@Configuration
@Profile({"!rabbitmq", "!test"})
public class InMemoryBusConfig {

    @Bean
    public EventBus inMemoryEventBus() {
        return new LocalEventBus();
    }

    @Bean
    public CommandBus inMemoryCommandBus() {
        return new LocalCommandBus();
    }
}
