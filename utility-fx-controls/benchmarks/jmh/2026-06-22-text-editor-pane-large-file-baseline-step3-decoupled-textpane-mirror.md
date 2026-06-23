# Baseline: 2026-06-22 (Step 3: Decouple `TextPane.text` Mirror)

- Timestamp (UTC): `2026-06-22T11:50:37Z`
- Command: `./gradlew :utility-fx-controls:jmh`
- Gradle: `9.5.1`
- JVM: `BellSoft Liberica 25.0.3+11-LTS`
- OS: `macOS 26.5.1 (arm64)`
- Raw results: [`2026-06-22-text-editor-pane-large-file-results-step3-decoupled-textpane-mirror.json`](./2026-06-22-text-editor-pane-large-file-results-step3-decoupled-textpane-mirror.json)

## Scope

- `TextEditorPane` internal text/edits now stay in `documentText` without synchronizing every internal edit into `TextPane.textProperty()`.
- `TextPane.getText()` now reads from `currentText()`, allowing `TextEditorPane` to expose internal document state directly.
- `TextPaneSkin` invalidation for editors is driven by `documentVersionProperty()` instead of `textProperty()`.
- Reference baseline for comparison: [`2026-06-22-text-editor-pane-large-file-baseline-step1-block-core-document.md`](./2026-06-22-text-editor-pane-large-file-baseline-step1-block-core-document.md)

## Text Edit Comparison (Step 3 Focus)

`score` is average latency in `ms/op`.  
`alloc` is `gc.alloc.rate.norm` in `B/op`.

| Benchmark | lines | wrap | old score | new score | score delta | old alloc | new alloc | alloc delta |
|---|---:|:---:|---:|---:|---:|---:|---:|---:|
| buildVisualLinesAfterSingleEdit | 5000 | false | 4.855 | 2.657 | -45.3% | 9286437 | 5625768 | -39.4% |
| buildVisualLinesAfterSingleEdit | 5000 | true | 4.697 | 2.422 | -48.4% | 13961925 | 7924316 | -43.2% |
| buildVisualLinesAfterSingleEdit | 20000 | false | 19.6 | 10.08 | -48.6% | 132302694 | 69388180 | -47.6% |
| buildVisualLinesAfterSingleEdit | 20000 | true | 18.896 | 10.177 | -46.1% | 203540058 | 110367098 | -45.8% |
| insertNearEndThenUndo | 5000 | false | 4.188 | 1.574 | -62.4% | 7072739 | 2725288 | -61.5% |
| insertNearEndThenUndo | 5000 | true | 4.146 | 1.59 | -61.7% | 11125226 | 4329362 | -61.1% |
| insertNearEndThenUndo | 20000 | false | 13.337 | 6.342 | -52.4% | 88927988 | 42729326 | -52% |
| insertNearEndThenUndo | 20000 | true | 13.504 | 5.979 | -55.7% | 143324245 | 64442219 | -55% |
| replaceMiddleCharThenUndo | 5000 | false | 4.284 | 2.215 | -48.3% | 7295899 | 3786477 | -48.1% |
| replaceMiddleCharThenUndo | 5000 | true | 4.292 | 2.076 | -51.6% | 11513528 | 5622701 | -51.2% |
| replaceMiddleCharThenUndo | 20000 | false | 18.79 | 8.624 | -54.1% | 125055836 | 57885099 | -53.7% |
| replaceMiddleCharThenUndo | 20000 | true | 18.875 | 8.648 | -54.2% | 201024092 | 93029571 | -53.7% |

## Non-Text History Comparison

`score` is average latency in `ms/op`.  
`alloc` is `gc.alloc.rate.norm` in `B/op`.

| Benchmark | lines | wrap | old score | new score | score delta | old alloc | new alloc | alloc delta |
|---|---:|:---:|---:|---:|---:|---:|---:|---:|
| applyBoldToSelectionThenUndo | 5000 | false | 6.98 | 6.337 | -9.2% | 12520807 | 11465434 | -8.4% |
| applyBoldToSelectionThenUndo | 5000 | true | 6.191 | 6.045 | -2.3% | 17362206 | 16982354 | -2.2% |
| applyBoldToSelectionThenUndo | 20000 | false | 25.545 | 25.552 | +0% | 173397752 | 173379632 | -0% |
| applyBoldToSelectionThenUndo | 20000 | true | 31.635 | 25.69 | -18.8% | 338510850 | 276455381 | -18.3% |
| applyTextColorToSelectionThenUndoRedo | 5000 | false | 11.649 | 10.703 | -8.1% | 20389287 | 18828359 | -7.7% |
| applyTextColorToSelectionThenUndoRedo | 5000 | true | 12.293 | 10.153 | -17.4% | 33649930 | 28038269 | -16.7% |
| applyTextColorToSelectionThenUndoRedo | 20000 | false | 53.961 | 44.222 | -18% | 360653041 | 296518279 | -17.8% |
| applyTextColorToSelectionThenUndoRedo | 20000 | true | 50.5 | 40.776 | -19.3% | 534606131 | 432503000 | -19.1% |

## Visual-Line Cache-Hit Comparison

`score` is average latency in `ms/op`.  
`alloc` is `gc.alloc.rate.norm` in `B/op`.

| Benchmark | lines | wrap | old score | new score | score delta | old alloc | new alloc | alloc delta |
|---|---:|:---:|---:|---:|---:|---:|---:|---:|
| buildVisualLinesCacheHit | 5000 | false | 0.017 | 0.019 | +10.2% | 28738 | 31598 | +10% |
| buildVisualLinesCacheHit | 5000 | true | 0.017 | 0.018 | +3.5% | 45554 | 47239 | +3.7% |
| buildVisualLinesCacheHit | 20000 | false | 0.017 | 0.017 | +1% | 116490 | 117645 | +1% |
| buildVisualLinesCacheHit | 20000 | true | 0.017 | 0.017 | +1.4% | 178747 | 181358 | +1.5% |

## Primary Metrics

`score` is average latency in `ms/op`.  
`alloc` is `gc.alloc.rate.norm` in `B/op`.

| Benchmark | lines | wrap | score (ms/op) | alloc (B/op) |
|---|---:|:---:|---:|---:|
| applyBoldToSelectionThenUndo | 5000 | false | 6.337 | 11465434 |
| applyBoldToSelectionThenUndo | 5000 | true | 6.045 | 16982354 |
| applyBoldToSelectionThenUndo | 20000 | false | 25.552 | 173379632 |
| applyBoldToSelectionThenUndo | 20000 | true | 25.69 | 276455381 |
| applyTextColorToSelectionThenUndoRedo | 5000 | false | 10.703 | 18828359 |
| applyTextColorToSelectionThenUndoRedo | 5000 | true | 10.153 | 28038269 |
| applyTextColorToSelectionThenUndoRedo | 20000 | false | 44.222 | 296518279 |
| applyTextColorToSelectionThenUndoRedo | 20000 | true | 40.776 | 432503000 |
| buildVisualLinesAfterSingleEdit | 5000 | false | 2.657 | 5625768 |
| buildVisualLinesAfterSingleEdit | 5000 | true | 2.422 | 7924316 |
| buildVisualLinesAfterSingleEdit | 20000 | false | 10.08 | 69388180 |
| buildVisualLinesAfterSingleEdit | 20000 | true | 10.177 | 110367098 |
| buildVisualLinesCacheHit | 5000 | false | 0.019 | 31598 |
| buildVisualLinesCacheHit | 5000 | true | 0.018 | 47239 |
| buildVisualLinesCacheHit | 20000 | false | 0.017 | 117645 |
| buildVisualLinesCacheHit | 20000 | true | 0.017 | 181358 |
| insertNearEndThenUndo | 5000 | false | 1.574 | 2725288 |
| insertNearEndThenUndo | 5000 | true | 1.59 | 4329362 |
| insertNearEndThenUndo | 20000 | false | 6.342 | 42729326 |
| insertNearEndThenUndo | 20000 | true | 5.979 | 64442219 |
| replaceMiddleCharThenUndo | 5000 | false | 2.215 | 3786477 |
| replaceMiddleCharThenUndo | 5000 | true | 2.076 | 5622701 |
| replaceMiddleCharThenUndo | 20000 | false | 8.624 | 57885099 |
| replaceMiddleCharThenUndo | 20000 | true | 8.648 | 93029571 |

## Notes

- Total benchmark wall-clock runtime: about `7m 22s`.
- JavaFX fork-shutdown timeout (`30s`) was not observed in this run.
- Internal edit paths show large latency/allocation drops after removing per-edit mirror updates.
- Potential API contract impact remains: for `TextEditorPane`, `textProperty()` no longer reflects live internal edits; use `getText()` as the document read path.
