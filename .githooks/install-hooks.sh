#!/bin/bash

# Install git hooks for spring-core project

echo "Configuring git hooks..."

# Make hooks executable
chmod +x .githooks/pre-commit
chmod +x .githooks/commit-msg

# Configure git to use .githooks directory
git config core.hooksPath .githooks

echo "Git hooks configured successfully"
echo ""
echo "Installed hooks:"
echo "  - pre-commit: Validates code quality before commit"
echo "  - commit-msg: Validates commit message format"
echo ""
echo "Git is now using .githooks/ directory for all hooks"
echo ""
echo "To run manually:"
echo "  git commit (hooks run automatically)"
echo ""
echo "To skip hooks (use sparingly):"
echo "  git commit --no-verify"
echo ""
echo "To uninstall:"
echo "  git config --unset core.hooksPath"