# Baseline: 2026-06-22 (Logical Blocks with Per-Block Text Payload)

- Timestamp (UTC): `2026-06-22T09:13:57Z`
- Command: `./gradlew :utility-fx-controls:jmh`
- Gradle: `9.5.1`
- JVM: `BellSoft Liberica 25.0.3+11-LTS`
- OS: `macOS 26.5.1 (arm64)`
- Raw results: [`2026-06-22-text-editor-pane-large-file-results-block-payload-cache.json`](./2026-06-22-text-editor-pane-large-file-results-block-payload-cache.json)

## Scope

- `LogicalBlock` now stores per-block `RichText` payload (detached subsequence) instead of re-slicing from the full text during layout.
- Incremental block updates rebuild changed blocks with cached payload and preserve this payload for later layout passes.
- `onTextChanged` takes the pending-edit incremental path first and avoids full invalidation in the same-text/pending-edit case.
- Reference baseline for comparison: [`2026-06-22-text-editor-pane-large-file-baseline-format-history-range-entry.md`](./2026-06-22-text-editor-pane-large-file-baseline-format-history-range-entry.md)

## Non-Text History Comparison

`score` is average latency in `ms/op`.  
`alloc` is `gc.alloc.rate.norm` in `B/op`.

| Benchmark | lines | wrap | old score | new score | score delta | old alloc | new alloc | alloc delta |
|---|---:|:---:|---:|---:|---:|---:|---:|---:|
| applyBoldToSelectionThenUndo | 5000 | false | 4.855 | 4.849 | -0.1% | 12479085 | 12674285 | +1.6% |
| applyBoldToSelectionThenUndo | 5000 | true | 4.844 | 4.853 | +0.2% | 17297048 | 17394311 | +0.6% |
| applyBoldToSelectionThenUndo | 20000 | false | 19.361 | 19.799 | +2.3% | 146370986 | 148978970 | +1.8% |
| applyBoldToSelectionThenUndo | 20000 | true | 19.385 | 19.685 | +1.5% | 222635396 | 224995894 | +1.1% |
| applyTextColorToSelectionThenUndoRedo | 5000 | false | 9.714 | 9.675 | -0.4% | 24905226 | 25119697 | +0.9% |
| applyTextColorToSelectionThenUndoRedo | 5000 | true | 9.707 | 9.695 | -0.1% | 34492040 | 34771792 | +0.8% |
| applyTextColorToSelectionThenUndoRedo | 20000 | false | 41.008 | 39.856 | -2.8% | 304057633 | 294903331 | -3.0% |
| applyTextColorToSelectionThenUndoRedo | 20000 | true | 39.070 | 39.271 | +0.5% | 446523203 | 448299834 | +0.4% |

## Primary Metrics

`score` is average latency in `ms/op`.  
`alloc` is `gc.alloc.rate.norm` in `B/op`.

| Benchmark | lines | wrap | score (ms/op) | alloc (B/op) |
|---|---:|:---:|---:|---:|
| applyBoldToSelectionThenUndo | 5000 | false | 4.849 | 12674285 |
| applyBoldToSelectionThenUndo | 5000 | true | 4.853 | 17394311 |
| applyBoldToSelectionThenUndo | 20000 | false | 19.799 | 148978970 |
| applyBoldToSelectionThenUndo | 20000 | true | 19.685 | 224995894 |
| applyTextColorToSelectionThenUndoRedo | 5000 | false | 9.675 | 25119697 |
| applyTextColorToSelectionThenUndoRedo | 5000 | true | 9.695 | 34771792 |
| applyTextColorToSelectionThenUndoRedo | 20000 | false | 39.856 | 294903331 |
| applyTextColorToSelectionThenUndoRedo | 20000 | true | 39.271 | 448299834 |
| buildVisualLinesAfterSingleEdit | 5000 | false | 5.672 | 15049177 |
| buildVisualLinesAfterSingleEdit | 5000 | true | 5.224 | 19755372 |
| buildVisualLinesAfterSingleEdit | 20000 | false | 21.378 | 162580457 |
| buildVisualLinesAfterSingleEdit | 20000 | true | 20.978 | 240761773 |
| buildVisualLinesCacheHit | 5000 | false | 0.017 | 28073 |
| buildVisualLinesCacheHit | 5000 | true | 0.017 | 45207 |
| buildVisualLinesCacheHit | 20000 | false | 0.017 | 114351 |
| buildVisualLinesCacheHit | 20000 | true | 0.017 | 178082 |
| insertNearEndThenUndo | 5000 | false | 4.885 | 12604581 |
| insertNearEndThenUndo | 5000 | true | 5.097 | 17959398 |
| insertNearEndThenUndo | 20000 | false | 19.777 | 148278593 |
| insertNearEndThenUndo | 20000 | true | 19.475 | 224120024 |
| replaceMiddleCharThenUndo | 5000 | false | 4.900 | 12719796 |
| replaceMiddleCharThenUndo | 5000 | true | 5.104 | 18078686 |
| replaceMiddleCharThenUndo | 20000 | false | 20.326 | 152576782 |
| replaceMiddleCharThenUndo | 20000 | true | 21.456 | 244194715 |

## Notes

- Total benchmark wall-clock runtime: about `7m 21s`.
- JavaFX fork-shutdown timeout (`30s`) was not observed in this run.
