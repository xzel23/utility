package com.dua3.utility.io;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
public class LoaderTestSecondProvider implements Loader<String> {

    private static final byte[] MAGIC_BYTES = "LOADTEST".getBytes(StandardCharsets.US_ASCII);
    private static final long MAGIC = ByteBuffer.wrap(MAGIC_BYTES).getLong();

    static long lastMagic;
    static Object[] lastOptions;
    static int isSupportedCalls;
    static int loadCalls;

    public static void reset() {
        lastMagic = 0L;
        lastOptions = null;
        isSupportedCalls = 0;
        loadCalls = 0;
    }

    public static byte[] magicBytes() {
        return MAGIC_BYTES.clone();
    }

    public static long expectedMagic() {
        return MAGIC;
    }

    @Override
    public boolean isSupported(Class<?> cls, long magic, Object[] options) {
        isSupportedCalls++;
        lastMagic = magic;
        lastOptions = options;
        return cls == String.class && magic == MAGIC;
    }

    @Override
    public String load(Payload payload, Object[] options) throws IOException {
        loadCalls++;
        return "second-loader-result";
    }
}
