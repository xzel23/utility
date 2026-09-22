package com.dua3.utility.fx.web;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebView;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Supplier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class WebViewsTest {

    @BeforeAll
    static void initJavaFx() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException e) {
            latch.countDown();
        }
        assertTrue(latch.await(10, TimeUnit.SECONDS));
    }

    static class MockJSObject extends JSObject {
        private final Map<String, Object> members = new HashMap<>();
        private final Map<Integer, Object> slots = new HashMap<>();
        boolean throwOnCall = false;

        @Override
        public Object call(String methodName, Object... args) throws JSException {
            if (throwOnCall) {
                throw new JSException("simulated JS error");
            }
            return "called_" + methodName;
        }

        @Override
        public Object eval(String s) throws JSException {
            return null;
        }

        @Override
        public Object getMember(String name) throws JSException {
            return members.get(name);
        }

        @Override
        public void setMember(String name, Object value) throws JSException {
            members.put(name, value);
        }

        @Override
        public void removeMember(String name) throws JSException {
            members.remove(name);
        }

        @Override
        public Object getSlot(int index) throws JSException {
            return slots.get(index);
        }

        @Override
        public void setSlot(int index, Object value) throws JSException {
            slots.put(index, value);
        }

        @Override
        public String toString() {
            return "MockJSObject";
        }
    }

    @Test
    void testCallMethod() {
        MockJSObject jsObj = new MockJSObject();
        Object res = WebViews.callMethod(jsObj, "myFunc", new Object[]{"a", 1});
        assertEquals("called_myFunc", res);

        jsObj.throwOnCall = true;
        assertThrows(JSException.class, () -> WebViews.callMethod(jsObj, "myFunc", new Object[]{"a", 1}));
    }

    @Test
    void testJSLogger() {
        List<String> logs = new ArrayList<>();
        Logger logger = (Logger) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{Logger.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if (args != null && args.length >= 2) {
                        if (args[1] instanceof Supplier<?> supplier) {
                            logs.add(name + ": " + supplier.get());
                        } else if (args[1] instanceof Supplier<?>[] suppliers && suppliers.length > 0) {
                            logs.add(name + ": " + suppliers[0].get());
                        }
                    }
                    return null;
                }
        );

        WebViews.JSLogger jsLogger = new WebViews.JSLogger(logger);

        MockJSObject args = new MockJSObject();
        args.setMember("length", 3);
        args.setSlot(0, "Message with %s and %d");
        args.setSlot(1, "param1");
        args.setSlot(2, 42);

        jsLogger.error(args);
        jsLogger.warn(args);
        jsLogger.info(args);
        jsLogger.debug(args);
        jsLogger.trace(args);

        assertEquals(5, logs.size());
        assertEquals("error: Message with param1 and 42", logs.get(0));
        assertEquals("warn: Message with param1 and 42", logs.get(1));
        assertEquals("info: Message with param1 and 42", logs.get(2));
        assertEquals("debug: Message with param1 and 42", logs.get(3));
        assertEquals("trace: Message with param1 and 42", logs.get(4));

        // Test non-integer length fallback
        MockJSObject fallbackArgs = new MockJSObject();
        jsLogger.info(fallbackArgs);
        assertEquals("info: MockJSObject", logs.get(5));
    }

    @Test
    void testFilterEvents() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean keyFiltered = new AtomicBoolean(false);
        AtomicBoolean mouseFiltered = new AtomicBoolean(false);
        java.util.concurrent.atomic.AtomicReference<Throwable> error = new java.util.concurrent.atomic.AtomicReference<>();

        Platform.runLater(() -> {
            try {
                WebView wv = new WebView();
                WebViews.filterEvents(wv,
                        k -> {
                            keyFiltered.set(true);
                            return true;
                        },
                        m -> {
                            mouseFiltered.set(true);
                            return true;
                        }
                );

                EventDispatcher dispatcher = wv.getEventDispatcher();
                EventDispatchChain chain = new EventDispatchChain() {
                    @Override
                    public EventDispatchChain append(EventDispatcher eventDispatcher) {
                        return this;
                    }

                    @Override
                    public EventDispatchChain prepend(EventDispatcher eventDispatcher) {
                        return this;
                    }

                    @Override
                    public Event dispatchEvent(Event event) {
                        return event;
                    }
                };

                KeyEvent keyEvent = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.A, false, false, false, false);
                dispatcher.dispatchEvent(keyEvent, chain);
                assertTrue(keyEvent.isConsumed());

                MouseEvent mouseEvent = new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, false, false, false, false, false, false, false, false, false, false, null);
                dispatcher.dispatchEvent(mouseEvent, chain);
                assertTrue(mouseEvent.isConsumed());

                Event genericEvent = new Event(Event.ANY);
                Event returned = dispatcher.dispatchEvent(genericEvent, chain);
                assertSame(genericEvent, returned);
                assertFalse(genericEvent.isConsumed());
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new RuntimeException(error.get());
        }
        assertTrue(keyFiltered.get());
        assertTrue(mouseFiltered.get());
    }

    @Test
    void testSetupEngineHandlers() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            WebView wv = new WebView();
            var engine = wv.getEngine();
            WebViews.setAlertHandler(engine);
            assertNotNull(engine.getOnAlert());

            WebViews.setConfirmationHandler(engine);
            assertNotNull(engine.getConfirmHandler());

            WebViews.setPromptHandler(engine);
            assertNotNull(engine.getPromptHandler());

            WebViews.setupEngine(engine, "TestLogger");
            latch.countDown();
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS));
    }
}
