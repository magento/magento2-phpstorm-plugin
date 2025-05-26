#!/bin/bash

# Get the list of modified files
MODIFIED_FILES=$(git diff --name-only HEAD | grep -E '\.java$' | sed 's/^/"/;s/$/"/' | tr '\n' ',' | sed 's/,$//')
MODIFIED_FILES="[$MODIFIED_FILES]"

# Export the environment variable
export MODIFIED_FILES

# Run the checkstyle and PMD tasks
./gradlew checkstyleCI pmdCI