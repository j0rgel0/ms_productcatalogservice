package com.lox.productcatalog.api.kafka.topics;

import com.lox.productcatalog.common.kafka.KafkaConfig;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class KafkaTopics {

    public static final String PRODUCT_EVENTS_TOPIC = "product-events";

    private final KafkaConfig kafkaConfig;

    @Bean
    public NewTopic productEventsTopic() {
        return kafkaConfig.createTopic(PRODUCT_EVENTS_TOPIC, 3, (short) 1);
    }
}
