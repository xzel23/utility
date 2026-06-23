# Baseline: 2026-06-22 (Non-Text History Benchmarks)

- Timestamp (UTC): `2026-06-22T08:15:17Z`
- Command: `./gradlew :utility-fx-controls:jmh`
- Gradle: `9.5.1`
- JVM: `BellSoft Liberica 25.0.3+11-LTS`
- OS: `macOS 26.5.1 (arm64)`
- Raw results: [`2026-06-22-text-editor-pane-large-file-results-non-text-history.json`](./2026-06-22-text-editor-pane-large-file-results-non-text-history.json)

## Scope

- Added benchmarks for non-text undo/redo history operations:
    - `applyBoldToSelectionThenUndo`
    - `applyTextColorToSelectionThenUndoRedo`

## Primary Metrics

`score` is average latency in `ms/op`.  
`alloc` is `gc.alloc.rate.norm` in `B/op`.

| Benchmark | lines | wrap | score (ms/op) | alloc (B/op) |
|---|---:|:---:|---:|---:|
| applyBoldToSelectionThenUndo | 5000 | false | 4.045 | 10407325 |
| applyBoldToSelectionThenUndo | 5000 | true | 4.301 | 15114335 |
| applyBoldToSelectionThenUndo | 20000 | false | 17.116 | 128555960 |
| applyBoldToSelectionThenUndo | 20000 | true | 16.180 | 185801235 |
| applyTextColorToSelectionThenUndoRedo | 5000 | false | 7.773 | 19508170 |
| applyTextColorToSelectionThenUndoRedo | 5000 | true | 7.308 | 26046150 |
| applyTextColorToSelectionThenUndoRedo | 20000 | false | 31.153 | 230016688 |
| applyTextColorToSelectionThenUndoRedo | 20000 | true | 31.022 | 350990938 |
| buildVisualLinesAfterSingleEdit | 5000 | false | 5.716 | 15068545 |
| buildVisualLinesAfterSingleEdit | 5000 | true | 5.208 | 19664661 |
| buildVisualLinesAfterSingleEdit | 20000 | false | 21.064 | 159854768 |
| buildVisualLinesAfterSingleEdit | 20000 | true | 21.660 | 247772608 |
| buildVisualLinesCacheHit | 5000 | false | 0.017 | 27911 |
| buildVisualLinesCacheHit | 5000 | true | 0.017 | 44920 |
| buildVisualLinesCacheHit | 20000 | false | 0.017 | 113414 |
| buildVisualLinesCacheHit | 20000 | true | 0.017 | 181503 |
| insertNearEndThenUndo | 5000 | false | 5.106 | 12973931 |
| insertNearEndThenUndo | 5000 | true | 5.100 | 18033497 |
| insertNearEndThenUndo | 20000 | false | 19.452 | 148179456 |
| insertNearEndThenUndo | 20000 | true | 19.524 | 224401489 |
| replaceMiddleCharThenUndo | 5000 | false | 4.905 | 12701499 |
| replaceMiddleCharThenUndo | 5000 | true | 5.014 | 17805763 |
| replaceMiddleCharThenUndo | 20000 | false | 20.959 | 157908889 |
| replaceMiddleCharThenUndo | 20000 | true | 19.746 | 225485937 |

## Notes

- Total benchmark wall-clock runtime: about `7m 22s`.
- JavaFX fork-shutdown timeout (`30s`) was not observed in this run.
