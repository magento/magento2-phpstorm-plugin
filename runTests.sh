#!/bin/bash

./gradlew clean
./gradlew runIdeForUiTests &
RUN_IDE_PID=$!

sleep 10

./gradlew test --no-daemon

# run certain test
#./gradlew test --tests "com.magento.idea.magento2plugin.actions.content.MarkDirectoryAsMagentoRootTest.testMarkDirectoryAsMagentoRoot" --scan

kill $RUN_IDE_PID

wait $RUN_IDE_PID 2>/dev/null