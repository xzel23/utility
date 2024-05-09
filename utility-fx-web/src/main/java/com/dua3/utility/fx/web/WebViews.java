// Copyright 2019 Axel Howind
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//     http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dua3.utility.fx.web;

import com.dua3.utility.fx.controls.Dialogs;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.function.Predicate;


public final class WebViews {
    private static final Logger LOG = LogManager.getLogger(WebViews.class);

    private WebViews() {
        // utility class
    }

    public static void setupEngine(WebEngine engine, String loggerName) {
        setAlertHandler(engine);
        setConfirmationHandler(engine);
        setPromptHandler(engine);
        setLogger(engine, LogManager.getLogger(loggerName));
    }

    public static void setAlertHandler(WebEngine engine) {
        engine.setOnAlert(e -> Dialogs.warning(null).header("%s", e.getData()).showAndWait());
    }

    public static void setConfirmationHandler(WebEngine engine) {
        engine.setConfirmHandler(s -> Dialogs.confirmation(null).header("%s", s).buttons(ButtonType.YES, ButtonType.NO)
                .defaultButton(ButtonType.NO).showAndWait().filter(b -> b == ButtonType.YES).isPresent());
    }

    public static void setPromptHandler(WebEngine engine) {
        engine.setPromptHandler(p -> Dialogs.prompt(null).header("%s", p.getMessage())
                .defaultValue("%s", p.getDefaultValue()).showAndWait().orElse(""));
    }

    public static boolean setLogger(WebEngine engine, Logger logger) {
        JSObject win = (JSObject) engine.executeScript("window");
        win.setMember("javaLogger", new JSLogger(logger));
        String script = """
                (function () {
                  console.log   = function() { window.javaLogger.info(arguments)  };
                  console.error = function() { window.javaLogger.error(arguments) };
                  console.warn  = function() { window.javaLogger.warn(arguments)  };
                  console.info  = function() { window.javaLogger.info(arguments)  };
                  console.debug = function() { window.javaLogger.debug(arguments) };
                  console.trace = function() { window.javaLogger.trace(arguments) };
                  console.log('logging initialised %s', 'success')
                  return true
                }) ();""";
        Object ret = engine.executeScript(script);

        boolean success = Boolean.TRUE.equals(ret);

        if (!success) {
            LOG.warn("could not set logger for WebView instance");
        }

        return success;
    }

    public static Object callMethod(JSObject object, String methodName, Object[] args) {
        try {
            return object.call(methodName, args);
        } catch (JSException e) {
            String msg = String.format(
                    "JSException: %s%n    when calling:    %s.%s()%n    with arguments:  %s",
                    e.getMessage(),
                    object,
                    methodName,
                    Arrays.toString(args));
            LOG.warn(msg);
            throw new JSException(msg);
        }
    }

    /**
     * Setup an event filter for WebView.
     *
     * @param wv          the WebView instance
     * @param filterKey   the KeyEvent filter
     * @param filterMouse the MouseEvent filter
     */
    public static void filterEvents(WebView wv, Predicate<KeyEvent> filterKey, Predicate<MouseEvent> filterMouse) {
        WebEventDispatcher dispatcher = new WebEventDispatcher(wv.getEventDispatcher(), filterKey, filterMouse);
        wv.setEventDispatcher(dispatcher);
    }

    public static class JSLogger {
        private final Logger logger;

        public JSLogger(Logger logger) {
            this.logger = logger;
        }

        public void error(JSObject args) {
            logger.error("{}", () -> formatMessage(args));
        }

        private static String formatMessage(JSObject args) {
            Object objLength = args.getMember("length");

            if (!(objLength instanceof Integer)) {
                return String.valueOf(args);
            }

            int length = (int) objLength;

            String msg = String.valueOf(args.getSlot(0));

            Object[] restArgs = new Object[Math.max(0, length - 1)];
            for (int i = 1; i < length; i++) {
                restArgs[i - 1] = args.getSlot(i);
            }

            return String.format(msg, restArgs);
        }

        public void warn(JSObject args) {
            logger.warn("{}", () -> formatMessage(args));
        }

        public void info(JSObject args) {
            logger.info("{}", () -> formatMessage(args));
        }

        public void debug(JSObject args) {
            logger.debug("{}", () -> formatMessage(args));
        }

        public void trace(JSObject args) {
            logger.trace("{}", () -> formatMessage(args));
        }
    }

    /**
     * EventDispatcher implementation for use by
     * {@link #filterEvents(WebView, Predicate, Predicate)}.
     */
    private static class WebEventDispatcher implements EventDispatcher {
        private final EventDispatcher originalDispatcher;
        private final Predicate<? super KeyEvent> filterKey;
        private final Predicate<? super MouseEvent> filterMouse;

        WebEventDispatcher(EventDispatcher originalDispatcher, Predicate<? super KeyEvent> filterKey, Predicate<? super MouseEvent> filterMouse) {
            this.originalDispatcher = originalDispatcher;
            this.filterKey = filterKey;
            this.filterMouse = filterMouse;
        }

        @Override
        public Event dispatchEvent(Event event, EventDispatchChain tail) {
            if (event instanceof KeyEvent keyEvent) {
                if (filterKey.test(keyEvent)) {
                    keyEvent.consume();
                }
            }
            if (event instanceof MouseEvent mouseEvent) {
                if (filterMouse.test(mouseEvent)) {
                    event.consume();
                }
            }
            return originalDispatcher.dispatchEvent(event, tail);
        }
    }

}
