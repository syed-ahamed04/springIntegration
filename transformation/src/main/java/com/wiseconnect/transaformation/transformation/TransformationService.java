package com.wiseconnect.transaformation.transformation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.jms.dsl.Jms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import jakarta.jms.ConnectionFactory;

@Service
public class TransformationService {

    private final JmsTemplate jmsTemplate;
    private final ConnectionFactory connectionFactory;

    @Autowired
    public TransformationService(JmsTemplate jmsTemplate, ConnectionFactory connectionFactory) {
        this.jmsTemplate = jmsTemplate;
        this.connectionFactory = connectionFactory;
    }

    @Bean
    public IntegrationFlow transformationFlow() {
        return IntegrationFlows.from(Jms.inboundAdapter(connectionFactory).destination("transformationQueue"))
            .handle((MessageHandler) message -> {
                String jsonPayload = (String) message.getPayload();
                // Logic to convert the JSON to XML goes here
                String xmlPayload = convertJsonToXml(jsonPayload);
                System.out.println("Transformed message: " + xmlPayload);
            })
            .get();
    }

    private String convertJsonToXml(String json) {
        try {
            // Parse JSON
            ObjectMapper jsonMapper = new ObjectMapper();
            JsonNode node = jsonMapper.readTree(json);

            // Convert JSON to XML
            XmlMapper xmlMapper = new XmlMapper();
            return xmlMapper.writer().withRootName("order").writeValueAsString(node);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
