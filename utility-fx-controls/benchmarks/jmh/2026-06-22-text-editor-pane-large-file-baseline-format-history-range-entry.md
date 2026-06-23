# Baseline: 2026-06-22 (Formatting History as Range Entry)

- Timestamp (UTC): `2026-06-22T08:53:43Z`
- Command: `./gradlew :utility-fx-controls:jmh`
- Gradle: `9.5.1`
- JVM: `BellSoft Liberica 25.0.3+11-LTS`
- OS: `macOS 26.5.1 (arm64)`
- Raw results: [`2026-06-22-text-editor-pane-large-file-results-format-history-range-entry.json`](./2026-06-22-text-editor-pane-large-file-results-format-history-range-entry.json)

## Scope

- Formatting undo/redo history switched from full `SnapshotHistoryEntry` to range-based `TextReplaceHistoryEntry`.
- Reference baseline for comparison: [`2026-06-22-text-editor-pane-large-file-baseline-non-text-history.md`](./2026-06-22-text-editor-pane-large-file-baseline-non-text-history.md)

## Non-Text History Comparison

`score` is average latency in `ms/op`.  
`alloc` is `gc.alloc.rate.norm` in `B/op`.

| Benchmark | lines | wrap | old score | new score | score delta | old alloc | new alloc | alloc delta |
|---|---:|:---:|---:|---:|---:|---:|---:|---:|
| applyBoldToSelectionThenUndo | 20000 | false | 17.116 | 19.361 | +13.1% | 128555960 | 146370986 | +13.9% |
| applyBoldToSelectionThenUndo | 20000 | true | 16.180 | 19.385 | +19.8% | 185801235 | 222635396 | +19.8% |
| applyBoldToSelectionThenUndo | 5000 | false | 4.045 | 4.855 | +20.0% | 10407325 | 12479085 | +19.9% |
| applyBoldToSelectionThenUndo | 5000 | true | 4.301 | 4.844 | +12.6% | 15114335 | 17297048 | +14.4% |
| applyTextColorToSelectionThenUndoRedo | 20000 | false | 31.153 | 41.008 | +31.6% | 230016688 | 304057633 | +32.2% |
| applyTextColorToSelectionThenUndoRedo | 20000 | true | 31.022 | 39.070 | +25.9% | 350990938 | 446523203 | +27.2% |
| applyTextColorToSelectionThenUndoRedo | 5000 | false | 7.773 | 9.714 | +25.0% | 19508170 | 24905226 | +27.7% |
| applyTextColorToSelectionThenUndoRedo | 5000 | true | 7.308 | 9.707 | +32.8% | 26046150 | 34492040 | +32.4% |

## Primary Metrics

`score` is average latency in `ms/op`.  
`alloc` is `gc.alloc.rate.norm` in `B/op`.

| Benchmark | lines | wrap | score (ms/op) | alloc (B/op) |
|---|---:|:---:|---:|---:|
| applyBoldToSelectionThenUndo | 5000 | false | 4.855 | 12479085 |
| applyBoldToSelectionThenUndo | 5000 | true | 4.844 | 17297048 |
| applyBoldToSelectionThenUndo | 20000 | false | 19.361 | 146370986 |
| applyBoldToSelectionThenUndo | 20000 | true | 19.385 | 222635396 |
| applyTextColorToSelectionThenUndoRedo | 5000 | false | 9.714 | 24905226 |
| applyTextColorToSelectionThenUndoRedo | 5000 | true | 9.707 | 34492040 |
| applyTextColorToSelectionThenUndoRedo | 20000 | false | 41.008 | 304057633 |
| applyTextColorToSelectionThenUndoRedo | 20000 | true | 39.070 | 446523203 |
| buildVisualLinesAfterSingleEdit | 5000 | false | 5.559 | 14819329 |
| buildVisualLinesAfterSingleEdit | 5000 | true | 5.313 | 19963499 |
| buildVisualLinesAfterSingleEdit | 20000 | false | 21.428 | 162092171 |
| buildVisualLinesAfterSingleEdit | 20000 | true | 20.711 | 238781768 |
| buildVisualLinesCacheHit | 5000 | false | 0.017 | 29115 |
| buildVisualLinesCacheHit | 5000 | true | 0.018 | 46934 |
| buildVisualLinesCacheHit | 20000 | false | 0.017 | 113967 |
| buildVisualLinesCacheHit | 20000 | true | 0.017 | 180561 |
| insertNearEndThenUndo | 5000 | false | 4.863 | 12570633 |
| insertNearEndThenUndo | 5000 | true | 5.103 | 18010904 |
| insertNearEndThenUndo | 20000 | false | 20.014 | 151288187 |
| insertNearEndThenUndo | 20000 | true | 19.478 | 223859686 |
| replaceMiddleCharThenUndo | 5000 | false | 4.948 | 12715024 |
| replaceMiddleCharThenUndo | 5000 | true | 4.972 | 17631823 |
| replaceMiddleCharThenUndo | 20000 | false | 19.706 | 148628207 |
| replaceMiddleCharThenUndo | 20000 | true | 20.666 | 236310592 |

## Notes

- Total benchmark wall-clock runtime: about `7m 21s`.
- JavaFX fork-shutdown timeout (`30s`) was not observed in this run.
