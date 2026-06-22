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
- `2026-06-22-text-editor-pane-large-file-baseline-non-text-history.md`: rerun after adding non-text history benchmarks.
- `2026-06-22-text-editor-pane-large-file-baseline-format-history-range-entry.md`: rerun after switching formatting history from snapshot to range entry.
- `2026-06-22-text-editor-pane-large-file-baseline-block-payload-cache.md`: rerun after caching per-block rich text payload in logical blocks.
- `2026-06-22-text-editor-pane-large-file-baseline-step1-richtext-splice.md`: step 1 rerun after replacing full-text rebuilds with segmented `RichText.replace(...)` in text-edit paths.
