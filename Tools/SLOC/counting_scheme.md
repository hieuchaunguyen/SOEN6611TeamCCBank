
# CBank V2.0 — Physical and Logical SLOC Counting Scheme

**Course:** SOEN 6611 — Software Measurement  
**Deliverable:** 2  
**Problem:** 5 — Source Lines of Code

## 1. Measurement Objective

Measure the size of the CBank V2.0 Java implementation using two metrics:

- Physical Source Lines of Code (Physical SLOC)
- Logical Source Lines of Code (Logical SLOC)

The measurements are obtained using the custom Java analyzer `SLOCAnalyzer.java`.

## 2. Measurement Scope

The source directory is:

`Source/cbank/src/`

The analyzer searches this directory recursively using `Files.walk()`.

The measurement includes all 16 production Java files in the following packages:

- `cbank`
- `cbank.account`
- `cbank.gui`
- `init`

The following are excluded:

- Test source files
- Compiled `.class` files
- Blank lines
- Comment-only lines (for Physical SLOC)
- Measurement tools and generated reports

## 3. Physical SLOC Counting Rules

Physical SLOC counts the number of physical source lines containing Java code.

Rules:

1. Each nonblank line containing source code contributes one Physical SLOC.
2. Blank lines are excluded.
3. Lines containing only `//` comments are excluded.
4. Lines containing only block comments (`/* ... */`) are excluded.
5. Lines containing code followed by an inline comment are counted.
6. A line containing only a brace (`{` or `}`) is counted if it is not otherwise blank or comment-only.
7. A statement spanning multiple physical lines contributes one count for each code-bearing physical line.

Physical SLOC is therefore a line-based measure rather than a statement count.

## 4. Logical SLOC Counting Rules

Logical SLOC counts selected Java declarations and executable statements using the Java compiler Abstract Syntax Tree (AST).

The analyzer counts:

- Variable declarations
- Expression statements
- Return statements
- Throw statements
- Break and continue statements
- Assert statements
- If statements
- While and do-while statements
- For and enhanced-for statements
- Switch statements and switch expressions
- Try statements
- Synchronized statements
- Labeled statements

Counting conventions:

1. Each selected AST statement or declaration contributes one Logical SLOC.
2. Class, interface, and method declarations are excluded.
3. Method and catch parameters are excluded.
4. For-loop headers are counted as one loop statement, avoiding separate counts for their initialization and update expressions.
5. Nested executable statements are counted separately when they belong to the selected AST categories.
6. Blank lines, comments, and formatting do not directly contribute to Logical SLOC.

These conventions define a project-specific Logical SLOC measure. Other tools may report different totals because Logical SLOC counting conventions are not universal.

## 5. Measurement Procedure

**Requirements:** JDK 17 or newer.

From the repository root, compile the analyzer:

```powershell
javac -d Tools/SLOC Tools/SLOC/SLOCAnalyzer.java
```

Run the analyzer:

```powershell
java -cp Tools/SLOC SLOCAnalyzer
```

The analyzer discovers production Java files recursively and generates:

- `Tools/SLOC/sloc_results.csv`
- `Tools/SLOC/sloc_results.md`

## 6. CBank V2.0 Measurement Results

| Metric | Result |
|---|---:|
| Production Java files | 16 |
| Physical SLOC | 562 |
| Logical SLOC | 310 |

The detailed per-file measurements are provided in `sloc_results.csv` and `sloc_results.md`.

## 7. Interpretation and Limitations

Physical SLOC measures the amount of code-bearing source text, whereas Logical SLOC measures selected declarations and statements.

The Physical SLOC total is greater than the Logical SLOC total because Java statements may span multiple lines, and physical code lines may include braces and other syntax not counted as separate logical statements.

SLOC is a software size metric. It does not directly measure software quality, maintainability, complexity, or developer productivity.

Both metrics depend on the documented counting conventions and the source-code version being analyzed.
