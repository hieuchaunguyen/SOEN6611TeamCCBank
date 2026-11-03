# CBank Source Lines of Code (SLOC) Measurement

**Course:** SOEN 6611 — Software Measurement  
**Deliverable:** 2  
**Problem:** 5 — Physical and Logical SLOC Measurement  
**Application:** CBank (Java Banking Application)

## 1. Objective

The objective of this measurement is to determine the Physical Source Lines of Code (Physical SLOC) and Logical Source Lines of Code (Logical SLOC) of the CBank application.

The measurements provide quantitative information about the size of the application's production source code.

## 2. Measurement Scope

The measurement includes all seven production Java source files located in:

`Source/cbank/src/cbank/`

The following files are included:

1. `Account.java`
2. `AuditLogger.java`
3. `BankSystem.java`
4. `CBankGUI.java`
5. `CurrencyConverter.java`
6. `CustomExceptions.java`
7. `Transaction.java`

The measurement excludes test files, compiled `.class` files, measurement tools, IDE configuration files, and other nonproduction artifacts.

## 3. Physical SLOC Counting Scheme

Physical SLOC represents the number of nonblank physical source lines containing Java code.

**Counting rules:**

- Each physical line containing source code counts as one Physical SLOC.
- Blank lines are excluded.
- Single-line comment-only lines are excluded.
- Block-comment-only lines are excluded.
- Lines containing both source code and comments are counted.
- A statement spanning multiple physical lines may contribute multiple Physical SLOC.
- Lines containing braces or other Java syntax are counted if they contain source code.

The analyzer processes comments and string literals separately to avoid treating comment markers inside strings as actual comments.

## 4. Logical SLOC Counting Scheme

Logical SLOC represents the number of selected Java source declarations and statements according to an explicitly defined counting convention.

The analyzer uses the Java Compiler API to construct an Abstract Syntax Tree (AST) and identify the relevant syntax constructs.

### Included constructs

- Field declarations.
- Local variable declarations.
- Expression statements, including assignments and method calls.
- Return statements.
- Throw statements.
- Break and continue statements.
- Assert statements.
- Conditional statements (`if`).
- Traditional and enhanced `for` loops.
- `while` and `do-while` loops.
- `switch` statements and switch expressions.
- `try` statements.
- Synchronized statements.
- Labeled statements.

Each included control statement counts as one logical unit. Statements inside its body are counted separately.

### Excluded constructs

- Package and import declarations.
- Class, interface, enum, and method declarations.
- Constructor declarations.
- Method parameters.
- Lambda parameters.
- Catch-clause parameters.
- Empty statements.
- Standalone blocks.
- Traditional `for`-loop initializer, condition, and update components as separate units.
- Enhanced `for`-loop iteration variables as separate declarations.
- Comments and whitespace.

### Counting convention

The analyzer counts each traditional or enhanced `for` loop once, plus the statements within its body.

A multiline statement counts as one logical unit when represented by one included AST statement node.

This is a project-specific, AST-based operational definition. Different SLOC tools may produce different Logical SLOC results because counting conventions vary.

## 5. Measurement Tool

A custom Java measurement tool, `SLOCAnalyzer.java`, was developed for this project.

The analyzer:

- Uses Java's standard file-handling APIs to read source files.
- Uses the Java Compiler API to parse Java syntax.
- Calculates Physical and Logical SLOC independently.
- Measures only the production Java source directory.
- Produces per-file measurements and overall totals.
- Automatically overwrites previous result files.

The tool requires JDK 17 or newer and does not require external dependencies.

## 6. Compilation and Execution

The analyzer is stored at:

`Tools/SLOC/SLOCAnalyzer.java`

Open the IntelliJ terminal from the repository root, which contains the `Source` and `Tools` directories.

### Step 1 — Compile

```powershell
javac -d Tools/SLOC Tools/SLOC/SLOCAnalyzer.java
```

### Step 2 — Execute

```powershell
java -cp Tools/SLOC SLOCAnalyzer
```

### Step 3 — Locate the reports

The analyzer automatically generates:

- `Tools/SLOC/sloc_results.csv`
- `Tools/SLOC/sloc_results.md`

Both files are overwritten whenever the analyzer runs.

The CSV contains the measurements in a machine-readable format. The Markdown report contains the per-file measurements, total calculations, and interpretation.

## 7. Measurement Results

The latest calculated measurements are maintained in:

**`sloc_results.md`**

This report is automatically generated from the analyzer's output and contains:

- Physical SLOC for each Java source file.
- Logical SLOC for each Java source file.
- Total Physical SLOC.
- Total Logical SLOC.
- Explicit summation calculations.
- A brief interpretation.

Keeping the results in an automatically generated report avoids discrepancies between the source code, CSV output, and written calculations.

## 8. Interpretation

Physical SLOC and Logical SLOC measure different aspects of source-code size.

Physical SLOC is sensitive to code formatting and line breaks, while Logical SLOC depends on the selected syntax constructs and the documented counting scheme.

A source file with a higher SLOC value contains more measured code, but a higher SLOC value does not necessarily imply lower readability or poorer maintainability.

Readability is evaluated separately in Problem 6.

## 9. Limitations

- The Logical SLOC count follows a documented AST-based convention rather than a universally standardized Java SLOC definition.
- The analyzer is intended for the CBank production source files and has not been independently validated as a general-purpose Java metrics tool.
- The counting convention may differ from other tools, particularly for complex expressions, declarations, and control-flow constructs.
- Physical SLOC may change when source formatting changes, even if program behavior remains identical.
- Results must be regenerated after changes to production source code.

## 10. Reproducibility

To reproduce the measurements, use JDK 17 or newer, open a terminal at the repository root, compile `SLOCAnalyzer.java`, and execute it using the commands in Section 6.

The analyzer will process the current production Java files and regenerate both result reports.

The generated `.class` files are build artifacts and should not be committed to version control.