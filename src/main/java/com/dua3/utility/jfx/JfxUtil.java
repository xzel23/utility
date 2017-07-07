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


import java.util.logging.Logger;

import com.aquafx_project.AquaFx;

public class JfxUtil {
    private static final Logger LOG = Logger.getLogger(JfxUtil.class.getName());

    // Utility class, should not be instantiated
    private JfxUtil() {
        // nop
    }

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
     *  the application name to set
     */
	public static void setNativeLookAndFeel(String applicationName) {
		if(System.getProperty("os.name").toUpperCase().startsWith("MAC")) {
			LOG.info("Applying Aquastyle");
			AquaFx.style();
		}
	}

}
