---
category: gotcha
subject: stale-test-results
---

2026-11-08 night: a broken resource (widget.xml lost its "/>" after a sed deleting `android:textStyle="italic" />` lines) made Gradle fail before tests, but parsing app/build/test-results XML still showed the previous green run. Always delete app/build/test-results/testDebugUnitTest before a run and check Gradle's exit status (scratchpad fullsuite.sh did both, plus :app:assembleRelease). Never delete XML attribute lines with sed when they may carry the closing tag.
