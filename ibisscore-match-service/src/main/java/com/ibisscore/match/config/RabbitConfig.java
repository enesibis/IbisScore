package com.ibisscore.match.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String PREDICTIONS_EXCHANGE  = "ibisscore.predictions";
    public static final String PREDICTIONS_QUEUE     = "predictions.queue";
    public static final String PREDICTIONS_DLQ       = "predictions.dlq";
    public static final String PREDICTIONS_ROUTING   = "prediction.request";

    public static final String FIXTURES_EXCHANGE     = "ibisscore.fixtures";
    public static final String FIXTURES_QUEUE        = "fixtures.queue";
    public static final String FIXTURES_ROUTING      = "fixture.event";

    @Bean
    public TopicExchange predictionsExchange() {
        return new TopicExchange(PREDICTIONS_EXCHANGE);
    }

    @Bean
    public TopicExchange fixturesExchange() {
        return new TopicExchange(FIXTURES_EXCHANGE);
    }

    @Bean
    public Queue predictionsQueue() {
        return QueueBuilder.durable(PREDICTIONS_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", PREDICTIONS_DLQ)
                .build();
    }

    @Bean
    public Queue predictionsDlq() {
        return QueueBuilder.durable(PREDICTIONS_DLQ).build();
    }

    @Bean
    public Queue fixturesQueue() {
        return QueueBuilder.durable(FIXTURES_QUEUE).build();
    }

    @Bean
    public Binding predictionsBinding() {
        return BindingBuilder.bind(predictionsQueue())
                .to(predictionsExchange())
                .with(PREDICTIONS_ROUTING + ".#");
    }

    @Bean
    public Binding fixturesBinding() {
        return BindingBuilder.bind(fixturesQueue())
                .to(fixturesExchange())
                .with(FIXTURES_ROUTING + ".#");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
