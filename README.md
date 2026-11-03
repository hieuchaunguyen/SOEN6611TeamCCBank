
# SOEN 6611 — Software Measurement

**Team C**

- Hieu Chau Nguyen
- Nadia Tasnim Mim

## Project Overview

CBank is a Java-based banking application with a Swing graphical user interface. This repository contains the application source code, automated tests, measurement tools, and reports prepared for SOEN 6611 Deliverable 2.

## Tools and Measurement Reports

The `Tools/` directory contains the project's measurement tools, methodologies, and generated reports.

### Problem 5 — Physical and Logical SLOC

`Tools/SLOC/` contains:

- `SLOCAnalyzer.java` — Java-based SLOC measurement tool.
- `counting_scheme.md` — definitions and counting rules.
- `sloc_results.csv` — per-file measurements in CSV format.
- `sloc_results.md` — formatted measurement report.

The analyzer measures all seven production Java files in `Source/cbank/src/cbank/`. Test files are excluded.

**Requirements:** JDK 17 or newer.

Run the following commands from the repository root:

```powershell
javac -d Tools\SLOC Tools\SLOC\SLOCAnalyzer.java
java -cp Tools\SLOC SLOCAnalyzer
```

**Measurement results:**

| Metric | Total |
|---|---:|
| Physical SLOC | 346 |
| Logical SLOC | 225 |
| Production Java files | 7 |

Running the analyzer regenerates `Tools/SLOC/sloc_results.csv` and `Tools/SLOC/sloc_results.md`.

The counting conventions are documented in `Tools/SLOC/counting_scheme.md`.

### Problem 6 — Source Code Readability

The selected readability metric is the **Comprehensive Model for Code Readability** by Scalabrino et al. (2018), published in the *Journal of Software: Evolution and Process*.

The model combines structural, visual, and textual source-code characteristics to estimate perceived readability.

`Tools/Readability/` contains:

- `rsm.jar` — researchers' readability measurement tool.
- `readability.classifier` — classification model required by the tool.
- `README.md` — original tool documentation.
- `readability_results.txt` — raw measurements.
- `readability_analysis.md` — formatted results, visualization, interpretation, and limitations.

**Requirements:** Java and the two tool files (`rsm.jar` and `readability.classifier`) in the same directory.

From the repository root, navigate to the readability directory:

```powershell
cd Tools\Readability
```

Then run:

```powershell
$files = @(Get-ChildItem "../../Source/cbank/src/cbank" -Filter "*.java" | Sort-Object Name | ForEach-Object { $_.FullName })
java -jar rsm.jar $files 2>&1 | Tee-Object -FilePath "readability_results.txt"
```

**Readability results:**

| Measurement | Result |
|---|---:|
| Production Java files | 7 |
| Unweighted average readability | 0.6198 |
| Highest score | Account.java — 0.7931 |
| Lowest score | CBankGUI.java — 0.4095 |
| Files scoring at least 0.50 | 6 |
| Files scoring below 0.50 | 1 |

The 0.50 midpoint is a project-defined descriptive comparison threshold, not a validated classification cutoff from the researchers.

For the full methodology, results, visualization, and interpretation, see `Tools/Readability/readability_analysis.md`.

**Research tool:** https://dibt-research.unimol.it/report/readability/

## Problem 4(b) — CBank Testing

The automated tests are located in:

`Source/cbank/test/cbank/`

They are standalone Java test programs and do not require an external testing framework.

### Automated Tests

- `AccountTest.java` — eight unit tests covering deposits, withdrawals, insufficient funds, invalid amounts, and PIN validation.
- `BankSystemTest.java` — ten integration tests covering login, transactions, PIN changes, vault operations, and customer/administrator authorization.

**Requirements:** JDK 17 or newer.

From the repository root, compile the production and test sources:

```powershell
javac -d out\cbank Source\cbank\src\cbank\*.java Source\cbank\test\cbank\*.java
```

Run the tests:

```powershell
java -cp out\cbank cbank.AccountTest
java -cp out\cbank cbank.BankSystemTest
```

**Automated test results:**

| Test Suite | Passed | Total | Result |
|---|---:|---:|---|
| AccountTest | 8 | 8 | PASS |
| BankSystemTest | 10 | 10 | PASS |
| **Total** | **18** | **18** | **PASS** |

Test outputs are saved to:

- `Tools/TestResults/account_unit_test_results.txt`
- `Tools/TestResults/bank_system_test_results.txt`

Each execution replaces the corresponding report with the latest test results.

### Manual GUI Testing

Representative graphical interface operations were tested manually, including:

- Customer login with valid credentials.
- Withdrawal with sufficient balance.
- Withdrawal with insufficient balance.
- Withdrawal with a negative amount.
- Deposit in CAD.
- Deposit in EUR.

**Observed issue:** Entering a negative withdrawal amount (`-100`) caused an uncaught `IllegalArgumentException` in the GUI withdrawal handler. This case failed during manual testing and should be corrected and retested before final release.

The automated test results above do not imply that all manual GUI scenarios passed.

## Repository Structure

```text
Source/
└── cbank/
    ├── src/
    │   └── cbank/
    │       └── [Production Java files]
    └── test/
        └── cbank/
            ├── AccountTest.java
            └── BankSystemTest.java

Tools/
├── SLOC/
│   ├── SLOCAnalyzer.java
│   ├── counting_scheme.md
│   ├── sloc_results.csv
│   └── sloc_results.md
├── Readability/
│   ├── rsm.jar
│   ├── readability.classifier
│   ├── README.md
│   ├── readability_results.txt
│   └── readability_analysis.md
└── TestResults/
    ├── account_unit_test_results.txt
    └── bank_system_test_results.txt
```

## Notes

- All reported measurements apply to the source-code version used during analysis.
- Measurements should be regenerated if production source files are modified.
- Physical and Logical SLOC follow the documented project counting conventions.
- Readability scores are model predictions, not direct measures of software correctness, usability, or maintainability.
- Generated `.class` files and local IDE configuration files should not be committed.
