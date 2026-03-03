#!/bin/bash

# Create results directory with today's date
DATE=$(date +%Y-%m-%d)
RESULT_DIR="../results/${DATE}"
mkdir -p "${RESULT_DIR}"

# Run k6 test
cd "$(dirname "$0")/../scenarios" || exit 1
k6 run scenario5-concurrency-accuracy.js

# Move generated files to date directory
mv ../results/scenario5_* "${RESULT_DIR}/" 2>/dev/null || true

echo ""
echo "Results saved to: ${RESULT_DIR}"
ls -lh "${RESULT_DIR}"
