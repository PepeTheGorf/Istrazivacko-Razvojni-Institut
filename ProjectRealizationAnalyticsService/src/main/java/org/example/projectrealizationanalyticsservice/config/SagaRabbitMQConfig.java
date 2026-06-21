package org.example.projectrealizationanalyticsservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SagaRabbitMQConfig {

    public static final String EXCHANGE = "saga.phase.transition.exchange";

    public static final String COMMAND_QUEUE = "phase.transition.command.queue";
    public static final String COMMAND_KEY = "phase.transition.command";

    public static final String REPLY_QUEUE = "phase.transition.reply.queue";
    public static final String REPLY_KEY = "phase.transition.reply";

    @Bean
    public DirectExchange phaseTransitionExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue phaseTransitionCommandQueue() {
        return QueueBuilder.durable(COMMAND_QUEUE).build();
    }

    @Bean
    public Queue phaseTransitionReplyQueue() {
        return QueueBuilder.durable(REPLY_QUEUE).build();
    }

    @Bean
    public Binding phaseTransitionCommandBinding() {
        return BindingBuilder.bind(phaseTransitionCommandQueue()).to(phaseTransitionExchange()).with(COMMAND_KEY);
    }

    @Bean
    public Binding phaseTransitionReplyBinding() {
        return BindingBuilder.bind(phaseTransitionReplyQueue()).to(phaseTransitionExchange()).with(REPLY_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        typeMapper.setTrustedPackages("*");
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory,
                                                                               Jackson2JsonMessageConverter converter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        return factory;
    }
}
