package io.github.alathra.alathranwars.messaging;

import io.github.alathra.alathranwars.event.MockEvent;
import io.github.alathra.alathranwars.messaging.message.Message;

public class MockSyncMessageEvent extends MockEvent {
    private final Message<?> message;

    public MockSyncMessageEvent(Message<?> message) {
        this.message = message;
    }

    public Message<?> getMessage() {
        return message;
    }
}