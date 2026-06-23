# Baseline: 2026-06-23 (Step 4: True Block-Core Document)

- Timestamp (UTC): `2026-06-23T02:29:10Z`
- Command: `./gradlew :utility-fx-controls:jmh`
- Gradle: `9.5.1`
- JVM: `BellSoft Liberica 25.0.3+11-LTS`
- OS: `macOS 26.5.1 (arm64)`
- Raw results: [`2026-06-23-text-editor-pane-large-file-results-step4-true-block-core-document.json`](./2026-06-23-text-editor-pane-large-file-results-step4-true-block-core-document.json)

## Scope

- `replaceDocumentRange(...)` now mutates only logical blocks and offsets.
- Full-document `RichText` is no longer rebuilt for every text edit; it is materialized on demand.
- Selection slicing now reads directly from logical blocks (`readDocumentRange(...)`) to avoid forced full-document materialization.
- Reference baseline for comparison: [`2026-06-22-text-editor-pane-large-file-baseline-step3-decoupled-textpane-mirror.md`](./2026-06-22-text-editor-pane-large-file-baseline-step3-decoupled-textpane-mirror.md)

## Text Edit Comparison (Step 4 Focus)

`score` is average latency in `ms/op`.  
`alloc` is `gc.alloc.rate.norm` in `B/op`.

| Benchmark | lines | wrap | old score | new score | score delta | old alloc | new alloc | alloc delta |
|---|---:|:---:|---:|---:|---:|---:|---:|---:|
| buildVisualLinesAfterSingleEdit | 5000 | false | 2.657 | 0.712 | -73.2% | 5625768 | 2368960 | -57.9% |
| buildVisualLinesAfterSingleEdit | 5000 | true | 2.422 | 0.377 | -84.4% | 7924316 | 2475125 | -68.8% |
| buildVisualLinesAfterSingleEdit | 20000 | false | 10.08 | 1.766 | -82.5% | 69388180 | 14132053 | -79.6% |
| buildVisualLinesAfterSingleEdit | 20000 | true | 10.177 | 1.377 | -86.5% | 110367098 | 17298200 | -84.3% |
| insertNearEndThenUndo | 5000 | false | 1.574 | 0.084 | -94.7% | 2725288 | 234696 | -91.4% |
| insertNearEndThenUndo | 5000 | true | 1.59 | 0.084 | -94.7% | 4329362 | 317322 | -92.7% |
| insertNearEndThenUndo | 20000 | false | 6.342 | 0.278 | -95.6% | 42729326 | 2198789 | -94.9% |
| insertNearEndThenUndo | 20000 | true | 5.979 | 0.281 | -95.3% | 64442219 | 3336480 | -94.8% |
| replaceMiddleCharThenUndo | 5000 | false | 2.215 | 0.106 | -95.2% | 3786477 | 275161 | -92.7% |
| replaceMiddleCharThenUndo | 5000 | true | 2.076 | 0.091 | -95.6% | 5622701 | 339912 | -94% |
| replaceMiddleCharThenUndo | 20000 | false | 8.624 | 0.245 | -97.2% | 57885099 | 1977773 | -96.6% |
| replaceMiddleCharThenUndo | 20000 | true | 8.648 | 0.26 | -97% | 93029571 | 3123338 | -96.6% |

## Non-Text History Comparison

`score` is average latency in `ms/op`.  
`alloc` is `gc.alloc.rate.norm` in `B/op`.

| Benchmark | lines | wrap | old score | new score | score delta | old alloc | new alloc | alloc delta |
|---|---:|:---:|---:|---:|---:|---:|---:|---:|
| applyBoldToSelectionThenUndo | 5000 | false | 6.337 | 4.257 | -32.8% | 11465434 | 13307235 | +16.1% |
| applyBoldToSelectionThenUndo | 5000 | true | 6.045 | 4.187 | -30.7% | 16982354 | 17487909 | +3% |
| applyBoldToSelectionThenUndo | 20000 | false | 25.552 | 16.805 | -34.2% | 173379632 | 135475653 | -21.9% |
| applyBoldToSelectionThenUndo | 20000 | true | 25.69 | 18.313 | -28.7% | 276455381 | 216606548 | -21.6% |
| applyTextColorToSelectionThenUndoRedo | 5000 | false | 10.703 | 6.573 | -38.6% | 18828359 | 22659049 | +20.3% |
| applyTextColorToSelectionThenUndoRedo | 5000 | true | 10.153 | 6.727 | -33.7% | 28038269 | 29805307 | +6.3% |
| applyTextColorToSelectionThenUndoRedo | 20000 | false | 44.222 | 24.918 | -43.7% | 296518279 | 209197803 | -29.4% |
| applyTextColorToSelectionThenUndoRedo | 20000 | true | 40.776 | 25.445 | -37.6% | 432503000 | 315360099 | -27.1% |

## Visual-Line Cache-Hit Comparison

`score` is average latency in `ms/op`.  
`alloc` is `gc.alloc.rate.norm` in `B/op`.

| Benchmark | lines | wrap | old score | new score | score delta | old alloc | new alloc | alloc delta |
|---|---:|:---:|---:|---:|---:|---:|---:|---:|
| buildVisualLinesCacheHit | 5000 | false | 0.019 | 0.017 | -7.2% | 31598 | 29358 | -7.1% |
| buildVisualLinesCacheHit | 5000 | true | 0.018 | 0.017 | -1% | 47239 | 46708 | -1.1% |
| buildVisualLinesCacheHit | 20000 | false | 0.017 | 0.017 | -0.7% | 117645 | 116621 | -0.9% |
| buildVisualLinesCacheHit | 20000 | true | 0.017 | 0.017 | +2.1% | 181358 | 186605 | +2.9% |

## Primary Metrics

`score` is average latency in `ms/op`.  
`alloc` is `gc.alloc.rate.norm` in `B/op`.

| Benchmark | lines | wrap | score (ms/op) | alloc (B/op) |
|---|---:|:---:|---:|---:|
| applyBoldToSelectionThenUndo | 5000 | false | 4.257 | 13307235 |
| applyBoldToSelectionThenUndo | 5000 | true | 4.187 | 17487909 |
| applyBoldToSelectionThenUndo | 20000 | false | 16.805 | 135475653 |
| applyBoldToSelectionThenUndo | 20000 | true | 18.313 | 216606548 |
| applyTextColorToSelectionThenUndoRedo | 5000 | false | 6.573 | 22659049 |
| applyTextColorToSelectionThenUndoRedo | 5000 | true | 6.727 | 29805307 |
| applyTextColorToSelectionThenUndoRedo | 20000 | false | 24.918 | 209197803 |
| applyTextColorToSelectionThenUndoRedo | 20000 | true | 25.445 | 315360099 |
| buildVisualLinesAfterSingleEdit | 5000 | false | 0.712 | 2368960 |
| buildVisualLinesAfterSingleEdit | 5000 | true | 0.377 | 2475125 |
| buildVisualLinesAfterSingleEdit | 20000 | false | 1.766 | 14132053 |
| buildVisualLinesAfterSingleEdit | 20000 | true | 1.377 | 17298200 |
| buildVisualLinesCacheHit | 5000 | false | 0.017 | 29358 |
| buildVisualLinesCacheHit | 5000 | true | 0.017 | 46708 |
| buildVisualLinesCacheHit | 20000 | false | 0.017 | 116621 |
| buildVisualLinesCacheHit | 20000 | true | 0.017 | 186605 |
| insertNearEndThenUndo | 5000 | false | 0.084 | 234696 |
| insertNearEndThenUndo | 5000 | true | 0.084 | 317322 |
| insertNearEndThenUndo | 20000 | false | 0.278 | 2198789 |
| insertNearEndThenUndo | 20000 | true | 0.281 | 3336480 |
| replaceMiddleCharThenUndo | 5000 | false | 0.106 | 275161 |
| replaceMiddleCharThenUndo | 5000 | true | 0.091 | 339912 |
| replaceMiddleCharThenUndo | 20000 | false | 0.245 | 1977773 |
| replaceMiddleCharThenUndo | 20000 | true | 0.26 | 3123338 |

## Notes

- Total benchmark wall-clock runtime: about `7m 25s`.
- JavaFX fork-shutdown timeout (`30s`) was not observed in this run.
- This run confirms the step-4 objective: text-edit paths now avoid rebuilding a full document `RichText` per edit, with strong latency/allocation reductions in text-edit benchmarks.
