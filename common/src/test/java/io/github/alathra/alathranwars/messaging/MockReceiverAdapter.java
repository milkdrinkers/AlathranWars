package io.github.alathra.alathranwars.messaging;

import io.github.alathra.alathranwars.event.MockEventSystem;
import io.github.alathra.alathranwars.messaging.adapter.receiver.ReceiverAdapter;
import io.github.alathra.alathranwars.messaging.message.Message;

public class MockReceiverAdapter extends ReceiverAdapter {
    @Override
    public void accept(Message<?> message) {
        MockEventSystem.fireEvent(new MockSyncMessageEvent(message));
    }
}
