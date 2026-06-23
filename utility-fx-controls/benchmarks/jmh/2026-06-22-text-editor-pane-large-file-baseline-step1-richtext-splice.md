# Baseline: 2026-06-22 (Step 1: RichText Splice Replace for Text Edits)

- Timestamp (UTC): `2026-06-22T09:35:37Z`
- Command: `./gradlew :utility-fx-controls:jmh`
- Gradle: `9.5.1`
- JVM: `BellSoft Liberica 25.0.3+11-LTS`
- OS: `macOS 26.5.1 (arm64)`
- Raw results: [`2026-06-22-text-editor-pane-large-file-results-step1-richtext-splice.json`](./2026-06-22-text-editor-pane-large-file-results-step1-richtext-splice.json)

## Scope

- Added `RichText.replace(int start, int end, RichText replacement)` with segmented shared backing instead of full-document rebuild via `RichTextBuilder`.
- `TextEditorPane` text edits now use `RichText.replace(...)` for direct edit and history replay.
- Removed full-document `toString()` conversion from `onTextChanged` and incremental logical-block update path.
- Reference baseline for comparison: [`2026-06-22-text-editor-pane-large-file-baseline-block-payload-cache.md`](./2026-06-22-text-editor-pane-large-file-baseline-block-payload-cache.md)

## Text Edit Comparison (Step 1 Focus)

`score` is average latency in `ms/op`.  
`alloc` is `gc.alloc.rate.norm` in `B/op`.

| Benchmark | lines | wrap | old score | new score | score delta | old alloc | new alloc | alloc delta |
|---|---:|:---:|---:|---:|---:|---:|---:|---:|
| buildVisualLinesAfterSingleEdit | 5000 | false | 5.672 | 5.286 | -6.8% | 15049177 | 10005717 | -33.5% |
| buildVisualLinesAfterSingleEdit | 5000 | true | 5.224 | 4.712 | -9.8% | 19755372 | 14031034 | -29.0% |
| buildVisualLinesAfterSingleEdit | 20000 | false | 21.378 | 20.174 | -5.6% | 162580457 | 135915434 | -16.4% |
| buildVisualLinesAfterSingleEdit | 20000 | true | 20.978 | 19.259 | -8.2% | 240761773 | 206388928 | -14.3% |
| insertNearEndThenUndo | 5000 | false | 4.885 | 3.216 | -34.2% | 12604581 | 5462573 | -56.7% |
| insertNearEndThenUndo | 5000 | true | 5.097 | 4.256 | -16.5% | 17959398 | 11428297 | -36.4% |
| insertNearEndThenUndo | 20000 | false | 19.777 | 13.999 | -29.2% | 148278593 | 93216586 | -37.1% |
| insertNearEndThenUndo | 20000 | true | 19.475 | 13.667 | -29.8% | 224120024 | 146228446 | -34.8% |
| replaceMiddleCharThenUndo | 5000 | false | 4.900 | 4.349 | -11.3% | 12719796 | 7348594 | -42.2% |
| replaceMiddleCharThenUndo | 5000 | true | 5.104 | 5.281 | +3.5% | 18078686 | 14233248 | -21.3% |
| replaceMiddleCharThenUndo | 20000 | false | 20.326 | 18.666 | -8.2% | 152576782 | 124586333 | -18.3% |
| replaceMiddleCharThenUndo | 20000 | true | 21.456 | 18.303 | -14.7% | 244194715 | 193932173 | -20.6% |

## Non-Text History Comparison

`score` is average latency in `ms/op`.  
`alloc` is `gc.alloc.rate.norm` in `B/op`.

| Benchmark | lines | wrap | old score | new score | score delta | old alloc | new alloc | alloc delta |
|---|---:|:---:|---:|---:|---:|---:|---:|---:|
| applyBoldToSelectionThenUndo | 5000 | false | 4.849 | 5.360 | +10.6% | 12674285 | 9819523 | -22.5% |
| applyBoldToSelectionThenUndo | 5000 | true | 4.853 | 5.046 | +4.0% | 17394311 | 14320635 | -17.7% |
| applyBoldToSelectionThenUndo | 20000 | false | 19.799 | 17.614 | -11.0% | 148978970 | 120218189 | -19.3% |
| applyBoldToSelectionThenUndo | 20000 | true | 19.685 | 17.967 | -8.7% | 224995894 | 194345245 | -13.6% |
| applyTextColorToSelectionThenUndoRedo | 5000 | false | 9.675 | 10.598 | +9.5% | 25119697 | 18572144 | -26.1% |
| applyTextColorToSelectionThenUndoRedo | 5000 | true | 9.695 | 10.496 | +8.3% | 34771792 | 28893202 | -16.9% |
| applyTextColorToSelectionThenUndoRedo | 20000 | false | 39.856 | 36.371 | -8.7% | 294903331 | 244179741 | -17.2% |
| applyTextColorToSelectionThenUndoRedo | 20000 | true | 39.271 | 35.876 | -8.6% | 448299834 | 381230200 | -15.0% |

## Primary Metrics

`score` is average latency in `ms/op`.  
`alloc` is `gc.alloc.rate.norm` in `B/op`.

| Benchmark | lines | wrap | score (ms/op) | alloc (B/op) |
|---|---:|:---:|---:|---:|
| applyBoldToSelectionThenUndo | 5000 | false | 5.360 | 9819523 |
| applyBoldToSelectionThenUndo | 5000 | true | 5.046 | 14320635 |
| applyBoldToSelectionThenUndo | 20000 | false | 17.614 | 120218189 |
| applyBoldToSelectionThenUndo | 20000 | true | 17.967 | 194345245 |
| applyTextColorToSelectionThenUndoRedo | 5000 | false | 10.598 | 18572144 |
| applyTextColorToSelectionThenUndoRedo | 5000 | true | 10.496 | 28893202 |
| applyTextColorToSelectionThenUndoRedo | 20000 | false | 36.371 | 244179741 |
| applyTextColorToSelectionThenUndoRedo | 20000 | true | 35.876 | 381230200 |
| buildVisualLinesAfterSingleEdit | 5000 | false | 5.286 | 10005717 |
| buildVisualLinesAfterSingleEdit | 5000 | true | 4.712 | 14031034 |
| buildVisualLinesAfterSingleEdit | 20000 | false | 20.174 | 135915434 |
| buildVisualLinesAfterSingleEdit | 20000 | true | 19.259 | 206388928 |
| buildVisualLinesCacheHit | 5000 | false | 0.021 | 34875 |
| buildVisualLinesCacheHit | 5000 | true | 0.018 | 47561 |
| buildVisualLinesCacheHit | 20000 | false | 0.018 | 122873 |
| buildVisualLinesCacheHit | 20000 | true | 0.018 | 194218 |
| insertNearEndThenUndo | 5000 | false | 3.216 | 5462573 |
| insertNearEndThenUndo | 5000 | true | 4.256 | 11428297 |
| insertNearEndThenUndo | 20000 | false | 13.999 | 93216586 |
| insertNearEndThenUndo | 20000 | true | 13.667 | 146228446 |
| replaceMiddleCharThenUndo | 5000 | false | 4.349 | 7348594 |
| replaceMiddleCharThenUndo | 5000 | true | 5.281 | 14233248 |
| replaceMiddleCharThenUndo | 20000 | false | 18.666 | 124586333 |
| replaceMiddleCharThenUndo | 20000 | true | 18.303 | 193932173 |

## Notes

- Total benchmark wall-clock runtime: about `7m 25s`.
- JavaFX fork-shutdown timeout (`30s`) was not observed in this run.
- Several small-file measurements showed larger variance than large-file cases; treat `5000` line latency deltas with lower confidence than allocation deltas.
