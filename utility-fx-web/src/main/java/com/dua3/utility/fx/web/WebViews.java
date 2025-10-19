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
import com.dua3.utility.text.MessageFormatter;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.scene.control.Alert.AlertType;
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


/**
 * The {@code WebViews} class provides utility methods for setting up {@code WebEngine} instances and handling events in JavaFX WebViews.
 * It contains methods for setting alert, confirmation, and prompt handlers, as well as a logger for JavaScript log messages.
 * Additionally, it provides a method for calling JavaScript methods on a {@code JSObject} and filtering events in a {@code WebView}.
 */
public final class WebViews {
    private static final Logger LOG = LogManager.getLogger(WebViews.class);

    private WebViews() { /* utility class */ }

    /**
     * Sets up the WebEngine with alert, confirmation, prompt handlers, and a logger.
     *
     * @param engine      the WebEngine instance
     * @param loggerName  the name of the logger to be used
     */
    public static void setupEngine(WebEngine engine, String loggerName) {
        setAlertHandler(engine);
        setConfirmationHandler(engine);
        setPromptHandler(engine);
        setLogger(engine, LogManager.getLogger(loggerName));
    }

    /**
     * Sets the alert handler for the given WebEngine. The alert handler is responsible
     * for displaying alert dialogs in response to JavaScript `alert` statements
     * in the loaded web page.
     *
     * @param engine the WebEngine for which to set the alert handler
     * @throws NullPointerException if the engine is null
     */
    public static void setAlertHandler(WebEngine engine) {
        engine.setOnAlert(e -> Dialogs.alert(null, AlertType.WARNING, MessageFormatter.standard()).header("%s", e.getData()).showAndWait());
    }

    /**
     * Sets the confirmation handler for the given WebEngine.
     *
     * @param engine the WebEngine to set the confirmation handler for
     */
    public static void setConfirmationHandler(WebEngine engine) {
        engine.setConfirmHandler(s -> Dialogs.alert(null, AlertType.CONFIRMATION, MessageFormatter.standard())
                .header("%s", s)
                .setButtons(ButtonType.YES, ButtonType.NO)
                .defaultButton(ButtonType.NO)
                .showAndWait()
                .filter(b -> b == ButtonType.YES)
                .isPresent());
    }

    /**
     * Sets the prompt handler for the given WebEngine. The prompt handler is responsible for handling prompt dialogs
     * that may be triggered by JavaScript code running in the WebEngine.
     *
     * @param engine the WebEngine to set the prompt handler for
     */
    public static void setPromptHandler(WebEngine engine) {
        engine.setPromptHandler(p -> Dialogs.prompt(null, MessageFormatter.standard()).header("%s", p.getMessage())
                .defaultValue("%s", p.getDefaultValue()).showAndWait().orElse(""));
    }

    /**
     * Sets a logger for the provided WebEngine instance.
     *
     * @param engine the WebEngine instance for which to set the logger
     * @param logger the logger to be set
     * @return true if the logger was set successfully, false otherwise
     */
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

    /**
     * Calls a method on a JSObject with the specified arguments.
     *
     * @param object the JSObject on which the method will be called
     * @param methodName the name of the method to call
     * @param args the arguments to pass to the method
     * @return the result of the method call
     * @throws JSException if an exception occurs while calling the method
     */
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
     * Set up an event filter for WebView.
     *
     * @param wv          the WebView instance
     * @param filterKey   the KeyEvent filter
     * @param filterMouse the MouseEvent filter
     */
    public static void filterEvents(WebView wv, Predicate<? super KeyEvent> filterKey, Predicate<? super MouseEvent> filterMouse) {
        WebEventDispatcher dispatcher = new WebEventDispatcher(wv.getEventDispatcher(), filterKey, filterMouse);
        wv.setEventDispatcher(dispatcher);
    }

    /**
     * The JSLogger class is a utility class for logging messages from JavaScript code.
     * It wraps an instance of the Logger class from the SLF4J logging framework and provides
     * methods for logging messages at different levels (error, warn, info, debug, and trace).
     */
    public static class JSLogger {
        private final Logger logger;

        /**
         * The JSLogger class is a utility class for logging messages from JavaScript code.
         * It wraps an instance of the Logger class from the SLF4J logging framework and provides
         * methods for logging messages at different levels (error, warn, info, debug, and trace).
         *
         * @param logger the logger the JavaScript log messages are written to
         */
        public JSLogger(Logger logger) {
            this.logger = logger;
        }

        /**
         * Logs an error message with the formatted message obtained from the provided JSObject argument.
         *
         * @param args the JSObject argument containing the message and any additional arguments
         */
        public void error(JSObject args) {
            logger.error("{}", () -> formatMessage(args));
        }

        /**
         * Logs a warning message with the formatted message obtained from the provided JSObject argument.
         *
         * @param args the JSObject argument containing the message and any additional arguments
         */
        public void warn(JSObject args) {
            logger.warn("{}", () -> formatMessage(args));
        }

        /**
         * Logs an information message with the formatted message obtained from the provided JSObject argument.
         *
         * @param args the JSObject argument containing the message and any additional arguments
         */
        public void info(JSObject args) {
            logger.info("{}", () -> formatMessage(args));
        }

        /**
         * Logs a debug message with the provided JSObject argument.
         *
         * @param args the JSObject argument containing the message and any additional arguments
         */
        public void debug(JSObject args) {
            logger.debug("{}", () -> formatMessage(args));
        }

        /**
         * Logs a trace message with the formatted message obtained from the provided JSObject argument.
         *
         * @param args the JSObject argument containing the message and any additional arguments.
         */
        public void trace(JSObject args) {
            logger.trace("{}", () -> formatMessage(args));
        }

        private static String formatMessage(JSObject args) {
            Object objLength = args.getMember("length");

            if (!(objLength instanceof Integer integerObjLength)) {
                return String.valueOf(args);
            }

            int length = integerObjLength;

            String msg = String.valueOf(args.getSlot(0));

            Object[] restArgs = new Object[Math.max(0, length - 1)];
            for (int i = 1; i < length; i++) {
                restArgs[i - 1] = args.getSlot(i);
            }

            return String.format(msg, restArgs);
        }
    }

    /**
     * EventDispatcher implementation for use by
     * {@link #filterEvents(WebView, Predicate, Predicate)}.
     */
    private record WebEventDispatcher(
            EventDispatcher originalDispatcher,
            Predicate<? super KeyEvent> filterKey,
            Predicate<? super MouseEvent> filterMouse
    ) implements EventDispatcher {
        @Override
        public Event dispatchEvent(Event event, EventDispatchChain tail) {
            switch (event) {
                case KeyEvent keyEvent when filterKey.test(keyEvent) -> keyEvent.consume();
                case MouseEvent mouseEvent when filterMouse.test(mouseEvent) -> event.consume();
                default -> { /* do nothing */ }
            }
            return originalDispatcher.dispatchEvent(event, tail);
        }
    }
}
