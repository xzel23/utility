# Baseline: 2026-06-22 (Step 2: Formatting History Changed-Range Detection)

- Timestamp (UTC): `2026-06-22T09:59:14Z`
- Command: `./gradlew :utility-fx-controls:jmh`
- Gradle: `9.5.1`
- JVM: `BellSoft Liberica 25.0.3+11-LTS`
- OS: `macOS 26.5.1 (arm64)`
- Raw results: [`2026-06-22-text-editor-pane-large-file-results-step2-changed-range-history.json`](./2026-06-22-text-editor-pane-large-file-results-step2-changed-range-history.json)

## Scope

- Added `RichText.ChangeRange` and `RichText.findChangedRange(RichText other)` to compute a minimal changed span without full-string conversion.
- Updated `TextEditorPane.applyFormattingChange(...)` to build history entries from the actual changed range instead of selection bounds.
- Reference baseline for comparison: [`2026-06-22-text-editor-pane-large-file-baseline-step1-richtext-splice.md`](./2026-06-22-text-editor-pane-large-file-baseline-step1-richtext-splice.md)

## Non-Text History Comparison (Step 2 Focus)

`score` is average latency in `ms/op`.  
`alloc` is `gc.alloc.rate.norm` in `B/op`.

| Benchmark | lines | wrap | old score | new score | score delta | old alloc | new alloc | alloc delta |
|---|---:|:---:|---:|---:|---:|---:|---:|---:|
| applyBoldToSelectionThenUndo | 5000 | false | 5.360 | 6.186 | +15.4% | 9819523 | 11177970 | +13.8% |
| applyBoldToSelectionThenUndo | 5000 | true | 5.046 | 6.122 | +21.3% | 14320635 | 17146914 | +19.7% |
| applyBoldToSelectionThenUndo | 20000 | false | 17.614 | 23.253 | +32.0% | 120218189 | 157068291 | +30.7% |
| applyBoldToSelectionThenUndo | 20000 | true | 17.967 | 23.512 | +30.9% | 194345245 | 250537041 | +28.9% |
| applyTextColorToSelectionThenUndoRedo | 5000 | false | 10.598 | 11.412 | +7.7% | 18572144 | 19986521 | +7.6% |
| applyTextColorToSelectionThenUndoRedo | 5000 | true | 10.496 | 11.626 | +10.8% | 28893202 | 31872953 | +10.3% |
| applyTextColorToSelectionThenUndoRedo | 20000 | false | 36.371 | 41.580 | +14.3% | 244179741 | 275283460 | +12.7% |
| applyTextColorToSelectionThenUndoRedo | 20000 | true | 35.876 | 41.932 | +16.9% | 381230200 | 439733996 | +15.3% |

## Primary Metrics (Non-Text History)

`score` is average latency in `ms/op`.  
`alloc` is `gc.alloc.rate.norm` in `B/op`.

| Benchmark | lines | wrap | score (ms/op) | alloc (B/op) |
|---|---:|:---:|---:|---:|
| applyBoldToSelectionThenUndo | 5000 | false | 6.186 | 11177970 |
| applyBoldToSelectionThenUndo | 5000 | true | 6.122 | 17146914 |
| applyBoldToSelectionThenUndo | 20000 | false | 23.253 | 157068291 |
| applyBoldToSelectionThenUndo | 20000 | true | 23.512 | 250537041 |
| applyTextColorToSelectionThenUndoRedo | 5000 | false | 11.412 | 19986521 |
| applyTextColorToSelectionThenUndoRedo | 5000 | true | 11.626 | 31872953 |
| applyTextColorToSelectionThenUndoRedo | 20000 | false | 41.580 | 275283460 |
| applyTextColorToSelectionThenUndoRedo | 20000 | true | 41.932 | 439733996 |

## Notes

- Total benchmark wall-clock runtime: about `7m 26s`.
- JavaFX fork-shutdown timeout (`30s`) was not observed in this run.
- Under current benchmark workloads, changed-range detection introduces extra scan cost and regresses both latency and allocation for non-text history operations.
