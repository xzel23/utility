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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.dua3.utility.io.NetUtil;

import javafx.scene.web.WebEngine;

/**
 * A Utility class for JavaFx.
 */
public class JfxUtil {
    private static final Logger LOG = LogManager.getLogger(JfxUtil.class);

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
	        		LOG.debug("injecting JavaScript into WebView: {}", resource);
	            engine.executeScript(data);
	            break;
	        case "css":
	        		LOG.debug("injecting stylesheet into WebView: {}", resource);
	            Document doc = engine.getDocument();
	            Element styleNode = doc.createElement("style");
	            styleNode.setAttribute("type", "text/css");
	            styleNode.setTextContent(data);
	            doc.getElementsByTagName("head").item(0).appendChild(styleNode);
	            break;
	        default:
	        		LOG.warn("don't know how to inject resource into WebView: {}", resource);
	            throw new IllegalArgumentException("Resource has unsupported extension: " + resource);
	        }
	    }
	}

    // Utility class, should not be instantiated
    private JfxUtil() {
        // nop
    }

}
