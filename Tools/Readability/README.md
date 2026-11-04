
# CBank Source Code Readability Measurement

**SOEN 6611 — Software Measurement**  
**Deliverable 2 — Problem 6**

## Overview

This directory contains the readability measurement
tool and supporting artifacts for CBank V2.0.

The selected metric is based on the Comprehensive
Model for Code Readability proposed by Scalabrino
et al. (2018).

The original Java-based implementation, `rsm.jar`,
is used to calculate predicted readability scores
for the production Java source files.

Higher scores indicate greater predicted readability
on a scale from 0 to 1.

## Directory Contents

```text
Tools/Readability/
├── readability.classifier
├── readability_analysis.md
├── readability_results.txt
├── readability_results_clean.txt
├── README.md
└── rsm.jar
```

| File | Description |
|---|---|
| `rsm.jar` | Original research readability measurement tool |
| `readability.classifier` | Classifier required by the tool |
| `readability_results.txt` | Raw results with full source paths |
| `readability_results_clean.txt` | Results with shortened filenames |
| `readability_analysis.md` | Detailed analysis, tables, and Mermaid charts |
| `README.md` | Measurement instructions and documentation |

## Requirements

- Java Runtime Environment compatible with `rsm.jar`
- PowerShell for the documented execution commands

No additional charting libraries are required.

The readability visualizations are written using
Mermaid directly in the Markdown analysis report.

## Measurement Scope

The measurement includes all **16 production Java
source files** under:

`Source/cbank/src/`

Included packages:

- `cbank`
- `cbank.account`
- `cbank.gui`
- `init`

The following are excluded:

- Unit tests
- Measurement tools
- Compiled Java files
- External dependencies

Recursive file discovery is used because CBank V2.0
contains multiple source-code subdirectories.

## How to Run

### Step 1 — Open the Readability Directory

From the repository root:

```powershell
cd Tools/Readability
```

### Step 2 — Collect Production Java Files

```powershell
$files = @(Get-ChildItem "../../Source/cbank/src" -Recurse -File -Filter "*.java" | Sort-Object FullName | ForEach-Object { $_.FullName })
```

### Step 3 — Verify the File Count

```powershell
$files.Count
```

Expected output:

```text
16
```

### Step 4 — Execute the Java Readability Tool

```powershell
java -jar rsm.jar $files 2>&1 | Tee-Object -FilePath "readability_results.txt"
```

The tool prints a predicted readability score for
each production Java source file.

The results are saved to:

`readability_results.txt`

The original output includes full file paths because
the tool reports the paths supplied as input.

## Optional — Create a Readable Results File

To create a copy containing only filenames and
scores, run:

```powershell
Get-Content "readability_results.txt" |
    ForEach-Object {
        if ($_ -match '^(.+?\.java)(\s+)([0-9]+(?:\.[0-9]+)?)\s*$') {
            [System.IO.Path]::GetFileName($Matches[1]) + "`t" + $Matches[3]
        } else {
            $_
        }
    } | Set-Content "readability_results_clean.txt"
```

This does not change the measured scores.

The raw results remain available separately for
traceability.

## CBank V2.0 Results

| Metric | Value |
|---|---:|
| Production Java files measured | 16 |
| Unweighted average readability | 0.6973 |
| Highest readability score | 0.9864 |
| Lowest readability score | 0.4371 |
| Files at or above 0.50 | 15 |
| Files below 0.50 | 1 |

**Highest-scoring file:**
CommercialAccount.java (0.9864)

**Lowest-scoring file:**
CBankGUI.java (0.4371)

The project average is calculated by taking the
arithmetic mean of all 16 file-level scores.

The 0.50 midpoint is used only as a descriptive
reference and is not a validated readability cutoff.

## Results and Visualization

See the complete report:

[CBank V2.0 Readability Analysis](readability_analysis.md)

The analysis includes:

- Measurement objective and methodology
- Complete 16-file results table
- Average readability calculation
- Two Mermaid bar charts
- Highest- and lowest-scoring files
- Results interpretation
- Limitations and conclusions

Mermaid charts render directly in GitHub Markdown
without requiring image files or Python.

## Reproducibility

The measurement can be repeated using the same
Java tool and production source files.

The repository preserves:

- The original measurement tool
- The classifier
- Raw measurement output
- Cleaned measurement output
- Measurement procedure
- Quantitative analysis

## Reference

Scalabrino, S., et al. (2018).
*A Comprehensive Model for Code Readability.*
Journal of Software: Evolution and Process.

Research implementation:
https://dibt-research.unimol.it/report/readability/
