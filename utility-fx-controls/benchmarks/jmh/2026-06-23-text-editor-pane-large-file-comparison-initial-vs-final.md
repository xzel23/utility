# Final Comparison: Initial Baseline vs Final Refactoring

- Initial baseline: [`2026-06-22-text-editor-pane-large-file-baseline.md`](./2026-06-22-text-editor-pane-large-file-baseline.md)
- Final baseline: [`2026-06-23-text-editor-pane-large-file-baseline-step4-true-block-core-document.md`](./2026-06-23-text-editor-pane-large-file-baseline-step4-true-block-core-document.md)
- Raw data sources:
  - [`2026-06-22-text-editor-pane-large-file-results.json`](./2026-06-22-text-editor-pane-large-file-results.json)
  - [`2026-06-23-text-editor-pane-large-file-results-step4-true-block-core-document.json`](./2026-06-23-text-editor-pane-large-file-results-step4-true-block-core-document.json)

## Scope and Notes

- This comparison uses the 16 benchmark cases present in both runs.
- Non-text history benchmarks were added later and are therefore not part of this initial-vs-final table.
- `score` is latency (`ms/op`), lower is better.
- `alloc` is allocation (`B/op`), lower is better.

## Summary

- `buildVisualLinesAfterSingleEdit`: latency improved by about `87.0%` to `93.3%`; allocation improved by about `83.9%` to `92.7%`.
- `insertNearEndThenUndo`: latency improved by about `98.3%` to `98.6%`; allocation improved by about `98.2%` to `98.6%`.
- `replaceMiddleCharThenUndo`: latency improved by about `97.9%` to `98.7%`; allocation improved by about `97.9%` to `98.7%`.
- `buildVisualLinesCacheHit`: effectively flat (small noise-level shifts around zero).

## Detailed Comparison

| Benchmark | lines | wrap | old score | new score | score delta | old alloc | new alloc | alloc delta |
|---|---:|:---:|---:|---:|---:|---:|---:|---:|
| buildVisualLinesAfterSingleEdit | 5000 | false | 5.494 | 0.712 | -87.0% | 14695260 | 2368960 | -83.9% |
| buildVisualLinesAfterSingleEdit | 5000 | true | 5.189 | 0.377 | -92.7% | 19655039 | 2475125 | -87.4% |
| buildVisualLinesAfterSingleEdit | 20000 | false | 21.976 | 1.766 | -92.0% | 165320586 | 14132053 | -91.5% |
| buildVisualLinesAfterSingleEdit | 20000 | true | 20.496 | 1.377 | -93.3% | 235833680 | 17298200 | -92.7% |
| buildVisualLinesCacheHit | 5000 | false | 0.017 | 0.017 | +2.8% | 28682 | 29358 | +2.4% |
| buildVisualLinesCacheHit | 5000 | true | 0.017 | 0.017 | +1.7% | 45809 | 46708 | +2.0% |
| buildVisualLinesCacheHit | 20000 | false | 0.019 | 0.017 | -6.4% | 124565 | 116621 | -6.4% |
| buildVisualLinesCacheHit | 20000 | true | 0.017 | 0.017 | -0.0% | 185105 | 186605 | +0.8% |
| insertNearEndThenUndo | 5000 | false | 5.113 | 0.084 | -98.4% | 12990468 | 234696 | -98.2% |
| insertNearEndThenUndo | 5000 | true | 4.816 | 0.084 | -98.3% | 17248209 | 317322 | -98.2% |
| insertNearEndThenUndo | 20000 | false | 20.361 | 0.278 | -98.6% | 153222713 | 2198789 | -98.6% |
| insertNearEndThenUndo | 20000 | true | 19.201 | 0.281 | -98.5% | 220470938 | 3336480 | -98.5% |
| replaceMiddleCharThenUndo | 5000 | false | 5.156 | 0.106 | -97.9% | 13064393 | 275161 | -97.9% |
| replaceMiddleCharThenUndo | 5000 | true | 4.813 | 0.091 | -98.1% | 17195491 | 339912 | -98.0% |
| replaceMiddleCharThenUndo | 20000 | false | 19.406 | 0.245 | -98.7% | 147106916 | 1977773 | -98.7% |
| replaceMiddleCharThenUndo | 20000 | true | 19.309 | 0.260 | -98.7% | 220560259 | 3123338 | -98.6% |
