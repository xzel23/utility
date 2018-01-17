/**
 * 
 */
/**
 * @author axel
 *
 */
module com.dua3.utility {
	exports com.dua3.utility.math;
	exports com.dua3.utility.text;
	exports com.dua3.utility.jfx;
	exports com.dua3.utility.lang;
	exports com.dua3.utility.swing;
	exports com.dua3.utility;
	exports com.dua3.utility.io;

	requires java.datatransfer;
	requires java.desktop;
	requires java.logging;
	requires java.xml;
	requires javafx.web;
	requires junit;
	requires slf4j.api;
}