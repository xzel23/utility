package test.com.dua3.utility.xml;

import com.dua3.utility.io.IoUtil;
import com.dua3.utility.text.TextUtil;
import com.dua3.utility.xml.XmlUtil;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class XmlUtilTest {

    private static final XmlUtil XML_UTIL = XmlUtil.defaultInstance();
    
    private static final String XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Countries>
                <Country LongName="Canada" ShortName="CA">
                    <Provinces>
                        <Province Name="Alberta"/>
                        <Province Name="Manitoba"/>
                    </Provinces>
                </Country>
                <Country LongName="United States" ShortName="US">
                    <Provinces>
                        <Province Name="Alaska"/>
                        <Province Name="Alabama"/>
                    </Provinces>
                </Country>
            </Countries>
            """;

    private final String XML_UNFORMATTED = XML.replaceAll("^\\s+", "");
    
    @Test
    void parse() throws Exception {
        Document document = XML_UTIL.parse(XML);
        assertNotNull(document);
    }

    @Test
    void testParse() throws Exception {
        Document document = XML_UTIL.parse(IoUtil.stringInputStream(XML));
        assertNotNull(document);
    }
    
    @Test
    void prettyPrint() throws Exception {
        Document document = XML_UTIL.parse(XML_UNFORMATTED);
        String text = XML_UTIL.prettyPrint(document);
        assertEquals(TextUtil.toSystemLineEnds(XML), text);
    }

}