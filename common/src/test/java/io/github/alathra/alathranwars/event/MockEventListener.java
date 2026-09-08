package io.github.alathra.alathranwars.event;

@FunctionalInterface
public interface MockEventListener {
    void onEvent(MockEvent event);
}