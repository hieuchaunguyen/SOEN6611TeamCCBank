# CBank SLOC Measurement Results

Measurement scope: production Java source files in `Source/cbank/src/cbank/`.

Counting rules are documented in `counting_scheme.md`.

| Source File | Physical SLOC | Logical SLOC |
|---|---:|---:|
| Account.java | 32 | 24 |
| AuditLogger.java | 13 | 3 |
| BankSystem.java | 76 | 50 |
| CBankGUI.java | 166 | 123 |
| CurrencyConverter.java | 22 | 10 |
| CustomExceptions.java | 15 | 4 |
| Transaction.java | 22 | 11 |
| **TOTAL** | **346** | **225** |

## Physical SLOC Calculation

32 + 13 + 76 + 166 + 22 + 15 + 22 = **346**

## Logical SLOC Calculation

24 + 3 + 50 + 123 + 10 + 4 + 11 = **225**

## Interpretation

The application contains **346 Physical SLOC** and **225 Logical SLOC** under the documented measurement conventions.

`CBankGUI.java` has the highest Physical SLOC (166).

SLOC measures source-code size. It does not directly establish readability or maintainability.
