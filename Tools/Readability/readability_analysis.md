# CBank Source Code Readability Analysis

**Course:** SOEN 6611 - Software Measurement
**Deliverable:** 2
**Problem:** 6 - Source Code Readability

## 1. Selected Metric

The selected metric is the Comprehensive Model for Code Readability by Scalabrino et al. (2018), published in the Journal of Software: Evolution and Process.

The model uses structural, visual, and textual characteristics of source code to predict perceived readability.

The researchers' Java implementation, rsm.jar, calculates the readability of a Java class as the mean readability of its constituent methods.

## 2. Measurement Scope

The seven production Java source files in Source/cbank/src/cbank/ were measured. Test files and measurement tools were excluded.

## 3. Readability Results

| Java Source File | Readability Score |
|---|---:|
| Account.java | 0.7931 |
| AuditLogger.java | 0.5632 |
| BankSystem.java | 0.6059 |
| CBankGUI.java | 0.4095 |
| CurrencyConverter.java | 0.6361 |
| CustomExceptions.java | 0.6836 |
| Transaction.java | 0.6470 |
| **Unweighted file average** | **0.6198** |

## 4. Readability Visualization

Each bar represents the predicted readability score on a 0-1 scale.

```text
Account.java             ########################       0.7931
CustomExceptions.java    #####################          0.6836
Transaction.java         ###################            0.6470
CurrencyConverter.java   ###################            0.6361
BankSystem.java          ##################             0.6059
AuditLogger.java         #################              0.5632
CBankGUI.java            ############                   0.4095
```

## 5. Quantitative Interpretation

The unweighted mean readability score across the seven files is **0.6198**.

For descriptive comparison, this project uses 0.50 as an illustrative midpoint threshold. This is not claimed to be an empirically validated cutoff from the researchers.

- Files with scores >= 0.50: **6**
- Files with scores below 0.50: **1**

**Highest score:** Account.java, 0.7931.

**Lowest score:** CBankGUI.java, 0.4095.

**Score range:** 0.3836.

CBankGUI.java is the primary candidate for manual readability review because it has the lowest predicted readability score.

The reported project average is an unweighted average of file-level scores, not a method-weighted overall score.

## 6. Reproducibility

Run these commands from Tools/Readability:

```powershell
$files = @(Get-ChildItem "../../Source/cbank/src/cbank" -Filter "*.java" | Sort-Object Name | ForEach-Object { $_.FullName })
java -jar rsm.jar $files 2>&1 | Tee-Object -FilePath "readability_results.txt"
```

The original tool output is stored in readability_results.txt.

## 7. Limitations

- Readability predictions are not direct measures of correctness or maintainability.
- The 0.50 midpoint is a project-defined descriptive threshold.
- The average gives equal weight to each file, regardless of method count.

## 8. Reference

Scalabrino, S., et al. (2018). A Comprehensive Model for Code Readability. Journal of Software: Evolution and Process.

Research tool: https://dibt-research.unimol.it/report/readability/
