package com.ticketwave.ticketorder.infrastructure.config;

import com.ticketwave.domain.bus.CommandBus;
import com.ticketwave.domain.bus.EventBus;
import com.ticketwave.ticketorder.infrastructure.bus.LocalCommandBus;
import com.ticketwave.ticketorder.infrastructure.bus.LocalEventBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Local development infrastructure: in-memory bus doubles so the application
 * starts offline with the {@code local} profile and no RabbitMQ broker.
 */
@Configuration
@Profile("local")
public class LocalBusConfig {

    @Bean
    public EventBus localEventBus() {
        return new LocalEventBus();
    }

    @Bean
    public CommandBus localCommandBus() {
        return new LocalCommandBus();
    }
}
