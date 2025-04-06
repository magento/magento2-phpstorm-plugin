#!/bin/bash

rm -rf intellij-test-project
./gradlew clean
./gradlew runIdeForUiTests &
RUN_IDE_PID=$!

sleep 10

./gradlew test -PexcludeTests="**/reference/**,**/linemarker/**,**/inspections/**,**/completion/**,**/actions/**" --no-daemon

kill $RUN_IDE_PID

wait $RUN_IDE_PID 2>/dev/null