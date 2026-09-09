package com.dua3.utility.fx;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FxLauncherOpenFilesDispatcherTest {

    @Test
    void deliversRequestsReceivedBeforeTheHandlerIsInstalled() {
        FxLauncher.OpenFilesDispatcher dispatcher = new FxLauncher.OpenFilesDispatcher(Consumer::accept);
        List<List<Path>> received = new ArrayList<>();
        List<Path> firstRequest = List.of(Path.of("first.dcompare"));
        List<Path> secondRequest = List.of(Path.of("second.dcompare"));

        dispatcher.accept(firstRequest);
        dispatcher.accept(secondRequest);
        dispatcher.setHandler(received::add);

        assertEquals(List.of(firstRequest, secondRequest), received);
    }

    @Test
    void deliversSubsequentRequestsToTheInstalledHandler() {
        FxLauncher.OpenFilesDispatcher dispatcher = new FxLauncher.OpenFilesDispatcher(Consumer::accept);
        List<List<Path>> received = new ArrayList<>();
        List<Path> request = List.of(Path.of("comparison.dcompare"));

        dispatcher.setHandler(received::add);
        dispatcher.accept(request);

        assertEquals(List.of(request), received);
    }
}
