# Baseline: 2026-06-22 (Step 1: Block-Core Document Authority)

- Timestamp (UTC): `2026-06-22T11:19:36Z`
- Command: `./gradlew :utility-fx-controls:jmh`
- Gradle: `9.5.1`
- JVM: `BellSoft Liberica 25.0.3+11-LTS`
- OS: `macOS 26.5.1 (arm64)`
- Raw results: [`2026-06-22-text-editor-pane-large-file-results-step1-block-core-document.json`](./2026-06-22-text-editor-pane-large-file-results-step1-block-core-document.json)

## Scope

- `TextEditorPane` now treats logical blocks as the edit-time document core.
- Internal edit/format/history paths no longer derive updates from `getText()` as source-of-truth; they operate on document state and keep `TextPane.text` as synchronized mirror.
- Reference baseline for comparison: [`2026-06-22-text-editor-pane-large-file-baseline-step2-changed-range-history.md`](./2026-06-22-text-editor-pane-large-file-baseline-step2-changed-range-history.md)

## Text Edit Comparison (Step 1 Focus)

`score` is average latency in `ms/op`.  
`alloc` is `gc.alloc.rate.norm` in `B/op`.

| Benchmark | lines | wrap | old score | new score | score delta | old alloc | new alloc | alloc delta |
|---|---:|:---:|---:|---:|---:|---:|---:|---:|
| buildVisualLinesAfterSingleEdit | 5000 | false | 5.002 | 4.855 | -2.9% | 9516375 | 9286437 | -2.4% |
| buildVisualLinesAfterSingleEdit | 5000 | true | 4.712 | 4.697 | -0.3% | 14025691 | 13961925 | -0.5% |
| buildVisualLinesAfterSingleEdit | 20000 | false | 20.103 | 19.600 | -2.5% | 134854170 | 132302694 | -1.9% |
| buildVisualLinesAfterSingleEdit | 20000 | true | 19.974 | 18.896 | -5.4% | 212797047 | 203540058 | -4.4% |
| insertNearEndThenUndo | 5000 | false | 4.297 | 4.188 | -2.5% | 7262263 | 7072739 | -2.6% |
| insertNearEndThenUndo | 5000 | true | 3.208 | 4.146 | +29.2% | 8645225 | 11125226 | +28.7% |
| insertNearEndThenUndo | 20000 | false | 14.034 | 13.337 | -5.0% | 94505312 | 88927988 | -5.9% |
| insertNearEndThenUndo | 20000 | true | 14.094 | 13.504 | -4.2% | 149647763 | 143324245 | -4.2% |
| replaceMiddleCharThenUndo | 5000 | false | 4.282 | 4.284 | +0.1% | 7243686 | 7295899 | +0.7% |
| replaceMiddleCharThenUndo | 5000 | true | 4.301 | 4.292 | -0.2% | 11546555 | 11513528 | -0.3% |
| replaceMiddleCharThenUndo | 20000 | false | 18.756 | 18.790 | +0.2% | 124553862 | 125055836 | +0.4% |
| replaceMiddleCharThenUndo | 20000 | true | 18.807 | 18.875 | +0.4% | 201235110 | 201024092 | -0.1% |

## Non-Text History Comparison

`score` is average latency in `ms/op`.  
`alloc` is `gc.alloc.rate.norm` in `B/op`.

| Benchmark | lines | wrap | old score | new score | score delta | old alloc | new alloc | alloc delta |
|---|---:|:---:|---:|---:|---:|---:|---:|---:|
| applyBoldToSelectionThenUndo | 5000 | false | 6.186 | 6.980 | +12.8% | 11177970 | 12520807 | +12.0% |
| applyBoldToSelectionThenUndo | 5000 | true | 6.122 | 6.191 | +1.1% | 17146914 | 17362206 | +1.3% |
| applyBoldToSelectionThenUndo | 20000 | false | 23.253 | 25.545 | +9.9% | 157068291 | 173397752 | +10.4% |
| applyBoldToSelectionThenUndo | 20000 | true | 23.512 | 31.635 | +34.6% | 250537041 | 338510850 | +35.1% |
| applyTextColorToSelectionThenUndoRedo | 5000 | false | 11.412 | 11.649 | +2.1% | 19986521 | 20389287 | +2.0% |
| applyTextColorToSelectionThenUndoRedo | 5000 | true | 11.626 | 12.293 | +5.7% | 31872953 | 33649930 | +5.6% |
| applyTextColorToSelectionThenUndoRedo | 20000 | false | 41.580 | 53.961 | +29.8% | 275283460 | 360653041 | +31.0% |
| applyTextColorToSelectionThenUndoRedo | 20000 | true | 41.932 | 50.500 | +20.4% | 439733996 | 534606131 | +21.6% |

## Primary Metrics

`score` is average latency in `ms/op`.  
`alloc` is `gc.alloc.rate.norm` in `B/op`.

| Benchmark | lines | wrap | score (ms/op) | alloc (B/op) |
|---|---:|:---:|---:|---:|
| applyBoldToSelectionThenUndo | 5000 | false | 6.980 | 12520807 |
| applyBoldToSelectionThenUndo | 5000 | true | 6.191 | 17362206 |
| applyBoldToSelectionThenUndo | 20000 | false | 25.545 | 173397752 |
| applyBoldToSelectionThenUndo | 20000 | true | 31.635 | 338510850 |
| applyTextColorToSelectionThenUndoRedo | 5000 | false | 11.649 | 20389287 |
| applyTextColorToSelectionThenUndoRedo | 5000 | true | 12.293 | 33649930 |
| applyTextColorToSelectionThenUndoRedo | 20000 | false | 53.961 | 360653041 |
| applyTextColorToSelectionThenUndoRedo | 20000 | true | 50.500 | 534606131 |
| buildVisualLinesAfterSingleEdit | 5000 | false | 4.855 | 9286437 |
| buildVisualLinesAfterSingleEdit | 5000 | true | 4.697 | 13961925 |
| buildVisualLinesAfterSingleEdit | 20000 | false | 19.600 | 132302694 |
| buildVisualLinesAfterSingleEdit | 20000 | true | 18.896 | 203540058 |
| buildVisualLinesCacheHit | 5000 | false | 0.017 | 28738 |
| buildVisualLinesCacheHit | 5000 | true | 0.017 | 45554 |
| buildVisualLinesCacheHit | 20000 | false | 0.017 | 116490 |
| buildVisualLinesCacheHit | 20000 | true | 0.017 | 178747 |
| insertNearEndThenUndo | 5000 | false | 4.188 | 7072739 |
| insertNearEndThenUndo | 5000 | true | 4.146 | 11125226 |
| insertNearEndThenUndo | 20000 | false | 13.337 | 88927988 |
| insertNearEndThenUndo | 20000 | true | 13.504 | 143324245 |
| replaceMiddleCharThenUndo | 5000 | false | 4.284 | 7295899 |
| replaceMiddleCharThenUndo | 5000 | true | 4.292 | 11513528 |
| replaceMiddleCharThenUndo | 20000 | false | 18.790 | 125055836 |
| replaceMiddleCharThenUndo | 20000 | true | 18.875 | 201024092 |

## Notes

- Total benchmark wall-clock runtime: about `7m 22s`.
- JavaFX fork-shutdown timeout (`30s`) was not observed in this run.
- Step-1 text-edit improvements are present for most large-file edit/layout cases, but non-text history paths regress in this version and one small-file wrapped insertion case regresses (`5000`, `wrap=true`).
