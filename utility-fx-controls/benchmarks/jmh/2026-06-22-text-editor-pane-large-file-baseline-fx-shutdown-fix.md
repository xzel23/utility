# Baseline: 2026-06-22 (FX Shutdown Fix)

- Timestamp (UTC): `2026-06-22T05:26:54Z`
- Command: `./gradlew :utility-fx-controls:jmh`
- Gradle: `9.5.1`
- JVM: `BellSoft Liberica 25.0.3+11-LTS`
- OS: `macOS 26.5.1 (arm64)`
- Raw results: [`2026-06-22-text-editor-pane-large-file-results-fx-shutdown-fix.json`](./2026-06-22-text-editor-pane-large-file-results-fx-shutdown-fix.json)

## Primary Metrics

`score` is average latency in `ms/op`.  
`alloc` is `gc.alloc.rate.norm` in `B/op`.

| Benchmark | lines | wrap | score (ms/op) | alloc (B/op) |
|---|---:|:---:|---:|---:|
| buildVisualLinesAfterSingleEdit | 5000 | false | 5.639 | 14940851 |
| buildVisualLinesAfterSingleEdit | 5000 | true | 5.440 | 20256925 |
| buildVisualLinesAfterSingleEdit | 20000 | false | 21.260 | 161070855 |
| buildVisualLinesAfterSingleEdit | 20000 | true | 21.260 | 244001512 |
| buildVisualLinesCacheHit | 5000 | false | 0.019 | 31137 |
| buildVisualLinesCacheHit | 5000 | true | 0.017 | 46817 |
| buildVisualLinesCacheHit | 20000 | false | 0.018 | 121206 |
| buildVisualLinesCacheHit | 20000 | true | 0.017 | 180066 |
| insertNearEndThenUndo | 5000 | false | 5.132 | 13008088 |
| insertNearEndThenUndo | 5000 | true | 4.853 | 17358334 |
| insertNearEndThenUndo | 20000 | false | 19.443 | 147106304 |
| insertNearEndThenUndo | 20000 | true | 20.364 | 232009939 |
| replaceMiddleCharThenUndo | 5000 | false | 5.123 | 13000308 |
| replaceMiddleCharThenUndo | 5000 | true | 4.861 | 17403539 |
| replaceMiddleCharThenUndo | 20000 | false | 19.522 | 148017690 |
| replaceMiddleCharThenUndo | 20000 | true | 19.723 | 224344331 |

## Notes

- JavaFX keepalive fork-shutdown timeout (`30s`) was not observed in this run.
- Wall-clock benchmark runtime dropped from about `13 min` (previous run) to about `5 min`.
