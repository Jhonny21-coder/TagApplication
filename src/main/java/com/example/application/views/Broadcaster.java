package com.example.application.views;

import com.vaadin.flow.component.UI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import com.vaadin.flow.shared.Registration;

public class Broadcaster {
    // A thread pool to handle broadcasting updates
    private static final Executor broadcasterExecutor = Executors.newSingleThreadExecutor();

    // A thread-safe list of listeners (UIs that are registered to receive updates)
    private static final List<Consumer<UI>> listeners = new CopyOnWriteArrayList<>();

    // Register a new UI listener
    public static synchronized Registration register(UI ui, Consumer<UI> listener) {
        listeners.add(listener);

        return () -> unregister(listener);
    }

    // Unregister a UI listener when it's no longer needed
    public static synchronized void unregister(Consumer<UI> listener) {
        listeners.remove(listener);
    }

    // Broadcast the update to all registered listeners (UIs)
    public static synchronized void broadcast() {
        for (Consumer<UI> listener : listeners) {
            broadcasterExecutor.execute(() -> {
                // Execute the listener in the UI thread
                UI ui = UI.getCurrent();
                if (ui != null) {
                    ui.access(() -> listener.accept(ui));
                }
            });
        }
    }
}
