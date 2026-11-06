
# D2 Measurement and Testing Outputs

This directory contains measurement tools, reports, and
testing evidence for SOEN 6611 Deliverable 2.

## Directory Overview

| Directory | Purpose |
|---|---|
| [SLOC](SLOC/) | Physical and Logical SLOC measurement (Problem 5) |
| [Readability](Readability/) | Source-code readability measurement (Problem 6) |
| [TestResults](TestResults/) | Automated and manual testing evidence (Problem 4(b)) |

## Problem 5 — Source Lines of Code

The Java-based SLOC analyzer measures all 16 production
Java files recursively under `Source/cbank/src/`.

Current measurements:

- Physical SLOC: **562**
- Logical SLOC: **310**
- Production Java files: **16**

The measurement methodology and results are documented
in [SLOC/counting_scheme.md](SLOC/counting_scheme.md)
and [SLOC/sloc_results.md](SLOC/sloc_results.md).

## Problem 6 — Source Code Readability

Readability is measured using the Comprehensive Model
for Code Readability by Scalabrino et al. (2018).

All 16 production Java files are included.

Current measurements:

- Unweighted average readability: **0.6973**
- Highest score: **CommercialAccount.java — 0.9864**
- Lowest score: **CBankGUI.java — 0.4371**

See [Readability/readability_analysis.md](Readability/readability_analysis.md)
for the complete results, Mermaid charts, and interpretation.


## Problem 4(b) — Automated Testing

CBank V2.0 was tested using JUnit 5 and Maven.

| Test Suite | Passed |
|---|---:|
| AccountTest.java | 19 |
| BankSystemTest.java | 16 |
| **Total** | **35** |

Final execution results:

- Tests executed: 35
- Failures: 0
- Errors: 0
- Skipped: 0
- Pass rate: 100%
- Maven build: BUILD SUCCESS

The test execution reports are stored in
`Tools/TestResults/`.

See [Testing Documentation](TestResults/README.md)
for the execution procedure, results, and limitations.
