package com.dua3.utility.text;

import org.junit.Assert;
import org.junit.Test;

public class TextUtilTest {

	@Test
	public void testTransfrom() {
	    String template = "Hello ${NAME}.";

	    String expected = "Hello Axel.";
	    String actual = TextUtil.transform(template, s -> s.equals("NAME") ? "Axel" : null);

	    Assert.assertEquals(expected, actual);
	}

}
