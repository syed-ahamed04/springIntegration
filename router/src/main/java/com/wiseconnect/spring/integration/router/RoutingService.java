package com.wiseconnect.spring.integration.router;



import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.http.dsl.Http;
import org.springframework.integration.jms.dsl.Jms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.MessageHandler;

import jakarta.jms.ConnectionFactory;


@Configuration
public class RoutingService {
	
	 @Autowired
	 private JmsTemplate jmsTemplate;


   

 

    @Bean
    public IntegrationFlow jmsInboundFlow() {
        return IntegrationFlows.from(Jms.inboundAdapter(connectionFactory()).destination("jsonQueue"))  // Directly call the bean creation method
            .route(String.class, payload -> {
                if (payload.contains("order")) {
                    return "orderChannel"; // route to the 'order' service
                } else {
                    return "defaultChannel"; // route to a default service
                }
            })
            .get();
    }
  
    
    @Bean
    public IntegrationFlow orderServiceFlow() {
        return IntegrationFlows.from("orderChannel")
            .handle((payload, headers) -> {
                System.out.println("Routing to Transformation Service: " + payload);
                return payload;
            })
            .handle(Jms.outboundAdapter(jmsTemplate).destination("transformationQueue"))
            .get();
    }


   
    

    @Bean
    public IntegrationFlow defaultServiceFlow() {
        return IntegrationFlows.from("defaultChannel")
            .handle((MessageHandler) message -> {
                // Handle other messages here
                System.out.println("Default Service: Received message - " + message.getPayload());
            })
            .get();
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
        factory.setBrokerURL("tcp://localhost:61616");
        factory.setUserName("admin");
        factory.setPassword("admin");
        return factory;
    }
}