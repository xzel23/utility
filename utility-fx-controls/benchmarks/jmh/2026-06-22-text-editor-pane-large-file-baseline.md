# Baseline: 2026-06-22

- Timestamp (UTC): `2026-06-22T05:11:34Z`
- Command: `./gradlew :utility-fx-controls:jmh`
- Gradle: `9.5.1`
- JVM: `BellSoft Liberica 25.0.3+11-LTS`
- OS: `macOS 26.5.1 (arm64)`
- Raw results: [`2026-06-22-text-editor-pane-large-file-results.json`](./2026-06-22-text-editor-pane-large-file-results.json)

## Primary Metrics

`score` is average latency in `ms/op`.  
`alloc` is `gc.alloc.rate.norm` in `B/op`.

| Benchmark | lines | wrap | score (ms/op) | alloc (B/op) |
|---|---:|:---:|---:|---:|
| buildVisualLinesAfterSingleEdit | 5000 | false | 5.494 | 14695260 |
| buildVisualLinesAfterSingleEdit | 5000 | true | 5.189 | 19655039 |
| buildVisualLinesAfterSingleEdit | 20000 | false | 21.976 | 165320586 |
| buildVisualLinesAfterSingleEdit | 20000 | true | 20.496 | 235833680 |
| buildVisualLinesCacheHit | 5000 | false | 0.017 | 28682 |
| buildVisualLinesCacheHit | 5000 | true | 0.017 | 45809 |
| buildVisualLinesCacheHit | 20000 | false | 0.019 | 124565 |
| buildVisualLinesCacheHit | 20000 | true | 0.017 | 185105 |
| insertNearEndThenUndo | 5000 | false | 5.113 | 12990468 |
| insertNearEndThenUndo | 5000 | true | 4.816 | 17248209 |
| insertNearEndThenUndo | 20000 | false | 20.361 | 153222713 |
| insertNearEndThenUndo | 20000 | true | 19.201 | 220470938 |
| replaceMiddleCharThenUndo | 5000 | false | 5.156 | 13064393 |
| replaceMiddleCharThenUndo | 5000 | true | 4.813 | 17195491 |
| replaceMiddleCharThenUndo | 20000 | false | 19.406 | 147106916 |
| replaceMiddleCharThenUndo | 20000 | true | 19.309 | 220560259 |

## Notes

- Each JMH fork hit JavaFX keepalive shutdown timeout (`30s`) before force-exit.
- This adds runtime overhead to the benchmark process wall-clock time, but not to per-iteration `score`.
