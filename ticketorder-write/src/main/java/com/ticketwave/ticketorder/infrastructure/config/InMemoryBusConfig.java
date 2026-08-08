package com.ticketwave.ticketorder.infrastructure.config;

import com.ticketwave.domain.bus.CommandBus;
import com.ticketwave.domain.bus.EventBus;
import com.ticketwave.ticketorder.infrastructure.bus.LocalCommandBus;
import com.ticketwave.ticketorder.infrastructure.bus.LocalEventBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * In-memory bus doubles for development without a RabbitMQ broker. Active under
 * the {@code local} (H2) and {@code postgres} profiles; the {@code rabbitmq}
 * profile uses the real broker adapters instead.
 */
@Configuration
@Profile({"local", "postgres"})
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
