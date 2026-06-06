package com.dua3.utility.io;

import java.io.IOException;

public class LoaderTestFirstProvider implements Loader<String> {
    static int isSupportedCalls;
    static int loadCalls;

    public static void reset() {
        isSupportedCalls = 0;
        loadCalls = 0;
    }

    @Override
    public boolean isSupported(Class<?> cls, long magic, Object[] options) {
        isSupportedCalls++;
        return false;
    }

    @Override
    public String load(Payload payload, Object[] options) throws IOException {
        loadCalls++;
        return "first-loader-result";
    }
}
