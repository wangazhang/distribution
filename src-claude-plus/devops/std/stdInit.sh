#!/bin/bash
cd ../../
git config core.hooksPath .githooks
chmod +x .githooks/commit-msg
chmod +x .githooks/pre-push