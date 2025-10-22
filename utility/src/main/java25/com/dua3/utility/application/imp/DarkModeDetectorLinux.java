package com.dua3.utility.application.imp;

import com.dua3.utility.application.DarkModeDetector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dark mode detector implementation for Linux using DBus via the system "gdbus" tool.
 *
 * <p>No additional Java dependencies are required. We invoke the platform-provided
 * gdbus command to query xdg-desktop-portal for the current color-scheme and to
 * monitor the Settings::SettingChanged signal for updates (callback-like behavior,
 * no polling loop).</p>
 *
 * <p>Primary source: org.freedesktop.portal.Settings (namespace: org.freedesktop.appearance,
 * key: color-scheme). Values are uint32: 1 = prefer-dark, 2 = prefer-light, 0 = default.</p>
 *
 * <p>If gdbus or the portal are unavailable, detection is treated as unsupported.</p>
 */
public class DarkModeDetectorLinux extends DarkModeDetectorBase {
    private static final Logger LOG = LogManager.getLogger(DarkModeDetectorLinux.class);

    private static final String GD_BUS = "gdbus";
    private static final List<String> READ_CMD = List.of(
            GD_BUS, "call", "--session",
            "--dest", "org.freedesktop.portal.Desktop",
            "--object-path", "/org/freedesktop/portal/desktop",
            "--method", "org.freedesktop.portal.Settings.Read",
            "org.freedesktop.appearance", "color-scheme"
    );
    private static final List<String> MONITOR_CMD = List.of(
            GD_BUS, "monitor", "--session",
            "--dest", "org.freedesktop.portal.Desktop",
            "--object-path", "/org/freedesktop/portal/desktop"
    );

    // Patterns to extract the color-scheme value from gdbus outputs
    private static final Pattern UINT32_PATTERN = Pattern.compile("uint32\\s*(\\d+)");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("(\\d+)");

    private final AtomicBoolean watcherRunning = new AtomicBoolean(false);
    private Thread watcherThread;
    private Process monitorProcess;

    private static class Holder {
        private static final DarkModeDetector INSTANCE = create();
        private static DarkModeDetector create() {
            try {
                return new DarkModeDetectorLinux();
            } catch (Throwable t) {
                LOG.error("Failed to initialize DarkModeDetectorLinux", t);
                return new DarkModeDetectorImpUnsupported();
            }
        }
    }

    public static DarkModeDetector getInstance() {
        return Holder.INSTANCE;
    }

    private DarkModeDetectorLinux() {
        // nothing to init eagerly
    }

    @Override
    public boolean isDarkModeDetectionSupported() {
        // We consider it supported if gdbus exists and the portal responds.
        try {
            Integer v = readPortalColorScheme();
            return v != null; // portal reachable
        } catch (Exception e) {
            LOG.debug("Portal color-scheme read failed, detection unsupported.", e);
            return false;
        }
    }

    @Override
    public boolean isDarkMode() {
        try {
            Integer v = readPortalColorScheme();
            if (v == null) {
                // Unknown -> consider light by default
                return false;
            }
            // 1 = prefer-dark
            return v == 1;
        } catch (Exception e) {
            LOG.warn("Failed to detect dark mode via portal", e);
            return false;
        }
    }

    @Override
    protected void monitorSystemChanges(boolean enable) {
        if (enable) {
            startWatcher();
        } else {
            stopWatcher();
        }
    }

    private synchronized void startWatcher() {
        if (watcherRunning.get()) {
            return;
        }
        watcherRunning.set(true);
        watcherThread = new Thread(this::watchLoop, "DarkModeLinuxWatcher");
        watcherThread.setDaemon(true);
        watcherThread.start();
    }

    private synchronized void stopWatcher() {
        watcherRunning.set(false);
        if (monitorProcess != null) {
            monitorProcess.destroy();
            monitorProcess = null;
        }
    }

    private void watchLoop() {
        try {
            ProcessBuilder pb = new ProcessBuilder(MONITOR_CMD);
            pb.redirectErrorStream(true);
            monitorProcess = pb.start();

            try (InputStream is = monitorProcess.getInputStream();
                 BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while (watcherRunning.get() && (line = br.readLine()) != null) {
                    // Expect lines containing: org.freedesktop.portal.Settings::SettingChanged
                    if (!line.contains("org.freedesktop.portal.Settings::SettingChanged")) {
                        continue;
                    }
                    // We need to capture subsequent lines which include arguments; read a small block
                    StringBuilder block = new StringBuilder(line).append('\n');
                    br.mark(4096);
                    for (int i = 0; i < 4; i++) { // read a few more lines to find args
                        br.mark(4096);
                        String l = br.readLine();
                        if (l == null) break;
                        block.append(l).append('\n');
                        if (l.trim().startsWith(")")) break;
                    }
                    String content = block.toString();
                    if (content.toLowerCase(Locale.ROOT).contains("org.freedesktop.appearance")
                            && content.toLowerCase(Locale.ROOT).contains("color-scheme")) {
                        Integer v = extractUint32(content);
                        if (v != null) {
                            boolean dark = v == 1;
                            onChangeDetected(dark);
                        }
                    }
                }
            }
        } catch (IOException ioe) {
            if (watcherRunning.get()) {
                LOG.warn("gdbus monitor failed", ioe);
            }
        } finally {
            stopWatcher();
        }
    }

    private static Integer extractUint32(String s) {
        Matcher m = UINT32_PATTERN.matcher(s);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        // Fallback: first integer occurrence
        Matcher d = DIGIT_PATTERN.matcher(s);
        if (d.find()) {
            try {
                return Integer.parseInt(d.group(1));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static Integer readPortalColorScheme() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(READ_CMD);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        String out;
        try (InputStream is = p.getInputStream()) {
            out = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
        boolean finished = p.waitFor(Duration.ofSeconds(5).toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
        if (!finished) {
            p.destroyForcibly();
            throw new IOException("gdbus call timeout");
        }
        if (p.exitValue() != 0) {
            throw new IOException("gdbus call failed: exit=" + p.exitValue() + ", out=" + out);
        }
        // Example outputs may look like: "(<'uint32 1'>)" or "(uint32 1)" depending on gdbus version
        Integer v = extractUint32(out);
        return v;
    }
}
