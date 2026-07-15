# EPD Core - Chess Engine Testing Framework

A Java-based framework for testing chess engines using EPD (Extended Position Description) format test suites.

## Overview

EPD Core is a testing framework designed to evaluate chess engine performance using standardized EPD test suites. It
provides search configuration, result tracking, and comprehensive reporting capabilities for analyzing engine move
selection and position evaluation accuracy.

## Features

- **Flexible Search Configuration**: Configurable alpha-beta search implementation with multiple enhancement options
- **EPD Format Support**: Parse and process standard EPD test suites with best move (`bm`) and evaluation (`ce`)
  operations
- **Comprehensive Result Tracking**: Track both move accuracy and evaluation accuracy
- **Detailed Reporting**: Generate formatted reports with success rates and failed test case details
- **Performance Metrics**: Measure and report search time and statistics

## Architecture

### Core Components

#### SearchSupplier

Factory for creating configured search instances with different feature sets:

- **Default**: Full-featured search with transposition tables
- **No Transposition Table**: Search without TT for testing purposes
- **Stale Age TT**: Search with transposition table entry aging

Features include:

- Game evaluator caching
- Quiescence search
- Move ordering (Killer moves, Recapture, MVV-LVA)
- Aspiration windows
- Search statistics

#### EpdSearchResult

Encapsulates test results for individual EPD positions:

- Original EPD entry
- Best move found by engine
- Best evaluation score
- Move success validation
- Evaluation success validation
- Full search result details

#### EpdSearchModel

Statistical model for aggregating test suite results:

- Total number of searches
- Move success rate (percentage)
- Evaluation success rate (percentage)
- Total search duration
- List of failed test cases with details

#### EpdSearchPrinter

Formats and outputs test results:

- Configurable output stream
- Custom report titles
- Summary statistics
- Detailed failure information

## Usage Example

```java
// Create search instance
SearchSupplier searchSupplier = new SearchSupplier();
Search search = searchSupplier.get();

// Run EPD test suite
List<EpdSearchResult> results = runEpdTests(search, epdEntries);

// Generate report
new EpdSearchPrinter()
    .setOut(System.out)
    .setReportTitle("Tactical Test Suite")
    .withEdpEntries(results)
    .print();
