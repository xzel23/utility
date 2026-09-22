package com.dua3.utility.data;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BearerTest {

    @Test
    void testBearer() {
        AtomicReference<String> storage = new AtomicReference<>("initial");
        Bearer<String> bearer = Bearer.create(storage::get, storage::set);

        assertEquals("initial", bearer.get());
        bearer.accept("updated");
        assertEquals("updated", bearer.get());
        assertEquals("updated", storage.get());
    }
}
