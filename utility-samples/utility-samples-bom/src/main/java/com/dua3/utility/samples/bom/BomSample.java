package com.dua3.utility.samples.bom;

import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.logging.LogUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A simple sample demonstrating the use of utility libraries with the utility-bom.
 */
public class BomSample {
    private static final Logger LOG = LogManager.getLogger(BomSample.class);

    public static void main(String[] args) {
        // Initialize logging
        LogUtil.assureInitialized();

        // Log a message
        LOG.info("BOM Sample started");

        // Use a utility from the utility library
        String text = "Hello, World!";
        String reversed = new StringBuilder(text).reverse().toString();

        LOG.info("Original text: {}", text);
        LOG.info("Reversed text: {}", reversed);

        LOG.info("BOM Sample completed");
    }
}
