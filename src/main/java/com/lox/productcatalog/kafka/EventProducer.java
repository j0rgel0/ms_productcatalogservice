// EventProducer.java
package com.lox.productcatalog.kafka;

import com.lox.productcatalog.models.events.Event;
import java.util.concurrent.CompletableFuture;

public interface EventProducer {

    CompletableFuture<Void> sendEvent(String topic, Event event);
}
