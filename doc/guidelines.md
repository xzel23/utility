# Documentation Guidelines

This document outlines the guidelines for documentation in the utility library project.

## Documentation Structure

All documentation for the utility library should be stored in the `doc` folder at the root of the project. This includes:

1. Module documentation
2. User guides
3. Developer documentation
4. API documentation references

## Documentation Format

All documentation should be written in Markdown format (.md files) for consistency and ease of reading both in text editors and when rendered on platforms like GitHub.

## Module Documentation

Each module in the utility library should have its documentation stored in the `doc` folder. The documentation should include:

1. Overview of the module's purpose
2. Key features and functionality
3. Maven coordinates for including the module in projects
4. Dependencies on other modules
5. Usage examples (where applicable)

The main overview of all modules is available in the `modules-overview.md` file.

## Updating Documentation

When making changes to the codebase:

1. Update the relevant documentation files in the `doc` folder
2. Ensure the module overview is kept up-to-date with any new modules or significant changes
3. Add examples for new functionality

## API Documentation

Detailed API documentation is generated from Javadoc comments in the source code. When writing code:

1. Include comprehensive Javadoc comments for all public classes and methods
2. Ensure examples in the documentation are up-to-date
3. Reference the generated Javadoc in the module documentation where appropriate