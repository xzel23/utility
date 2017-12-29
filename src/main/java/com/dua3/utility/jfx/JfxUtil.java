package com.dua3.utility.jfx;
/*
 * Copyright 2017 Axel Howind (axel@dua3.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.aquafx_project.AquaFx;
import com.dua3.utility.io.NetUtil;

import javafx.scene.web.WebEngine;

/**
 * A Utility class for JavaFx.
 */
public class JfxUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(JfxUtil.class);

    /**
     * Set the Look&amp;Feel to the native Look&amp;Feel.
     *
     * On Mac OS, the global menubar is also enabled.
     */
    public static void setNativeLookAndFeel() {
        setNativeLookAndFeel(null);
    }

    /**
     * Set the Look&amp;Feel to the native Look&amp;Feel.
     *
     * On Mac OS, the global menubar is also enabled.
     *
     * @param applicationName
     *            the application name to set
     */
    public static void setNativeLookAndFeel(String applicationName) {
        if (System.getProperty("os.name").toUpperCase().startsWith("MAC")) {
            LOGGER.info("Applying Aquastyle.");
            AquaFx.style();
        }
    }

	/**
	 * Read JavaScript or CSS and inject into WebView.
	 * @param engine
	 *  the web engine
	 * @param resources
	 *  the resources to inject
	 * @throws IOException
	 *  if resources can not be read
	 */
	public static void injectResources(WebEngine engine, URL... resources) throws IOException {
	    for (URL resource : resources) {
	        String data = NetUtil.readContent(resource, StandardCharsets.UTF_8);
	        switch (resource.getPath().replaceAll("^.*\\.", "")) { // switch on extension
	        case "js":
	            engine.executeScript(data);
	            break;
	        case "css":
	            Document doc = engine.getDocument();
	            Element styleNode = doc.createElement("style");
	            styleNode.setAttribute("type", "text/css");
	            styleNode.setTextContent(data);
	            doc.getElementsByTagName("head").item(0).appendChild(styleNode);
	            break;
	        default:
	            throw new IllegalArgumentException("Resource has unsupported extension: " + resource);
	        }
	    }
	}

    // Utility class, should not be instantiated
    private JfxUtil() {
        // nop
    }

}
