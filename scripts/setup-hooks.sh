#!/bin/sh
set -e

git config core.hooksPath .githooks
chmod +x .githooks/pre-commit .githooks/pre-push

echo "Hooks versionados ativados com sucesso (core.hooksPath=.githooks)."
