package com.dua3.utility.json;

import com.dua3.utility.io.IOUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class JsonUtil {
    /**
     * Read JSON fro URL.
     * @param url
     *  the URL to read from
     * @return
     *  the JSONObject read
     * @throws IOException
     *  if the data could not be read
     * @throws JSONException
     *  if the JSON data could not be parsed
     */
    public static JSONObject read(URL url) throws IOException {
        String text = IOUtil.read(url, StandardCharsets.UTF_8);
        return new JSONObject(text);
    }

    /**
     * Read JSON fro URL.
     * @param path
     *  the path to read from
     * @return
     *  the JSONObject read
     * @throws IOException
     *  if the data could not be read
     * @throws JSONException
     *  if the JSON data could not be parsed
     */
    public static JSONObject read(Path path) throws IOException {
        String text = IOUtil.read(path, StandardCharsets.UTF_8);
        return new JSONObject(text);
    }

    /* utility class */
    private JsonUtil() {}
}
