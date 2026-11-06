
# SOEN 6611 — Software Measurement

**Team C**

- Hieu Chau Nguyen
- Nadia Tasnim Mim

## Project Overview

CBank is a Java-based banking application with a
Swing graphical user interface.

This repository contains the application source code,
testing artifacts, measurement tools, and reports
prepared for SOEN 6611 Deliverable 2.

The measurements documented below apply to the
current CBank V2.0 implementation.

## Repository Structure

```text
Source/
└── cbank/
    ├── src/
    │   ├── cbank/
    │   │   ├── account/
    │   │   ├── gui/
    │   │   ├── AuditLogger.java
    │   │   ├── BankSystem.java
    │   │   ├── CurrencyConverter.java
    │   │   ├── CustomExceptions.java
    │   │   └── Transaction.java
    │   └── init/
    └── test/
        └── cbank/

Tools/
├── SLOC/
│   ├── SLOCAnalyzer.java
│   ├── counting_scheme.md
│   ├── sloc_results.csv
│   └── sloc_results.md
├── Readability/
│   ├── rsm.jar
│   ├── readability.classifier
│   ├── readability_results.txt
│   ├── readability_results_clean.txt
│   ├── readability_analysis.md
│   └── README.md
├── TestResults/
└── README.md
```

## Problem 4(b) — CBank Automated Testing

### Testing Framework

CBank V2.0 uses JUnit 5 and Maven for automated unit testing.

Test source files are located under:

`Source/cbank/test/cbank/`

### Test Suites

| Test Suite | Tests Passed |
|---|---:|
| AccountTest.java | 19 |
| BankSystemTest.java | 16 |
| **Total** | **35** |

### How to Run

From the repository root containing `pom.xml`:

```powershell
mvn test
```

### Final Testing Results

| Metric | Result |
|---|---:|
| Tests executed | 35 |
| Tests passed | 35 |
| Failures | 0 |
| Errors | 0 |
| Skipped | 0 |
| Pass rate | 100% |
| Maven status | BUILD SUCCESS |

Pass Rate = (35 / 35) × 100 = 100%

All 35 automated tests passed in the recorded execution.
This does not imply 100% code coverage.

### Supporting Documentation

- [Testing Results](Tools/TestResults/README.md)

## Problem 5 — Physical and Logical SLOC

### Measurement Method

A custom Java analyzer, `SLOCAnalyzer.java`, measures
Physical and Logical Source Lines of Code.

The analyzer recursively discovers production Java
files under:

`Source/cbank/src/`

The measurement covers 16 Java files across the
`cbank`, `cbank.account`, `cbank.gui`, and `init`
packages.

Test sources and measurement utilities are excluded.

### Requirements

JDK 17 or newer.

### How to Run

From the repository root:

```powershell
javac -d Tools/SLOC Tools/SLOC/SLOCAnalyzer.java
java -cp Tools/SLOC SLOCAnalyzer
```

The analyzer regenerates:

- `Tools/SLOC/sloc_results.csv`
- `Tools/SLOC/sloc_results.md`

### Measurement Results

| Metric | Result |
|---|---:|
| Production Java files | 16 |
| Physical SLOC | 562 |
| Logical SLOC | 310 |

### Supporting Documentation

- [Counting Scheme](Tools/SLOC/counting_scheme.md)
- [SLOC Measurement Results](Tools/SLOC/sloc_results.md)

The counting scheme documents the project-specific
rules used to calculate Physical and Logical SLOC.

## Problem 6 — Source Code Readability

### Selected Model

The selected metric is the **Comprehensive Model for
Code Readability** by Scalabrino et al. (2018).

The original Java-based research implementation
(`rsm.jar`) is used to estimate source-code
readability.

The model produces scores between 0 and 1, where
higher scores indicate greater predicted readability.

### Measurement Scope

All 16 production Java files under
`Source/cbank/src/` are measured recursively.

Test files and measurement utilities are excluded.

### Requirements

- Java runtime
- `rsm.jar`
- `readability.classifier`
- PowerShell for the documented commands

### How to Run

From the repository root:

```powershell
cd Tools/Readability

$files = @(Get-ChildItem "../../Source/cbank/src" -Recurse -File -Filter "*.java" | Sort-Object FullName | ForEach-Object { $_.FullName })

java -jar rsm.jar $files 2>&1 | Tee-Object -FilePath "readability_results.txt"
```

### Measurement Results

| Metric | Result |
|---|---:|
| Production Java files | 16 |
| Unweighted average readability | 0.6973 |
| Highest score | CommercialAccount.java — 0.9864 |
| Lowest score | CBankGUI.java — 0.4371 |
| Files at or above 0.50 | 15 |
| Files below 0.50 | 1 |

The project average is calculated as the arithmetic
mean of the 16 file-level readability scores.

The 0.50 midpoint is a project-defined descriptive
reference, not a validated classification cutoff.

### Supporting Documentation

- [Readability Tool Instructions](Tools/Readability/README.md)
- [Readability Analysis and Charts](Tools/Readability/readability_analysis.md)
- [Raw Readability Results](Tools/Readability/readability_results.txt)
- [Clean Readability Results](Tools/Readability/readability_results_clean.txt)

The detailed analysis includes two Mermaid bar charts
and interpretation of the measured scores.

**Research tool:**
https://dibt-research.unimol.it/report/readability/



## Notes

- All reported measurements apply to the current
  CBank implementation used during analysis.
- Measurements must be regenerated if production
  source files change.
- Physical and Logical SLOC follow the documented
  project counting conventions.
- Readability scores are model predictions rather
  than direct measurements of correctness,
  usability, or maintainability.
- Compiled `.class` files and local IDE configuration
  files should not be committed.
