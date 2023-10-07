package com.wiseconnect.spring.integration.demo;



import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.integration.core.GenericHandler;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.http.dsl.Http;
import org.springframework.integration.jms.dsl.Jms;
import org.springframework.jms.core.JmsTemplate;

@Configuration
public class AdapterService {

    @Value("${activemq.broker-url}")
    private String brokerUrl;

    @Bean
    public ActiveMQConnectionFactory connectionFactory() {
        return new ActiveMQConnectionFactory(brokerUrl);
    }

    @Bean
    public JmsTemplate jmsTemplate() {
        return new JmsTemplate(connectionFactory());
    }

    @Bean
    public IntegrationFlow httpInboundFlow() {
        return IntegrationFlows.from(Http.inboundGateway("/consumeJson")
                .requestMapping(m -> m.methods(HttpMethod.POST))
                .requestPayloadType(String.class))
            .handle((GenericHandler<String>) (payload, headers) -> {
                System.out.println("Received message: " + payload);
                return payload;
            })
            .handle(Jms.outboundAdapter(jmsTemplate()).destination("jsonQueue"))
            .get();
    }
    
    
    
}			


    

