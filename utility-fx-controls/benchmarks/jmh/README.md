# TextEditorPane JMH Baselines

This directory stores committed JMH baseline outputs for large-file editing and layout performance in `TextEditorPane`.

## Run Command

```bash
./gradlew :utility-fx-controls:jmh
```

Current JMH config is in [`utility-fx-controls/build.gradle.kts`](../../build.gradle.kts):
- JSON output
- GC profiler enabled (`gc.alloc.rate`, `gc.alloc.rate.norm`, `gc.count`, `gc.time`)
- headless JavaFX flags

Raw output from each run is written by Gradle to:
- `utility-fx-controls/build/reports/jmh/results.json`

Commit a timestamped copy from `build/reports/jmh/results.json` into this directory after each baseline run.

## Stored Baselines

- `2026-06-22-text-editor-pane-large-file-baseline.md`: first baseline (includes JavaFX fork-shutdown timeout note).
- `2026-06-22-text-editor-pane-large-file-baseline-fx-shutdown-fix.md`: rerun after JavaFX shutdown fix.
