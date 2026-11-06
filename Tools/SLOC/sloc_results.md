# CBank SLOC Measurement Results

Measurement scope: all 16 production Java source files
recursively discovered under Source/cbank/src/,
including the cbank and init packages.

Counting rules are documented in `counting_scheme.md`.

| Source File | Physical SLOC | Logical SLOC |
|---|---:|---:|
| Account.java | 38 | 22 |
| AccountType.java | 47 | 26 |
| AdminAccount.java | 7 | 1 |
| CommercialAccount.java | 6 | 1 |
| IndividualAccount.java | 7 | 1 |
| AuditLogger.java | 13 | 3 |
| BankSystem.java | 79 | 48 |
| CurrencyConverter.java | 28 | 8 |
| CustomExceptions.java | 18 | 5 |
| AbstractCBankWindow.java | 10 | 2 |
| CBankGUI.java | 144 | 87 |
| DashBoardWindow.java | 72 | 58 |
| LoginWindow.java | 40 | 24 |
| Transaction.java | 22 | 11 |
| AccountDatabase.java | 16 | 5 |
| CurrencyDatabase.java | 15 | 8 |
| **TOTAL** | **562** | **310** |

## Physical SLOC Calculation

38 + 47 + 7 + 6 + 7 + 13 + 79 + 28 + 18 + 10 + 144 + 72 + 40 + 22 + 16 + 15 = **562**

## Logical SLOC Calculation

22 + 26 + 1 + 1 + 1 + 3 + 48 + 8 + 5 + 2 + 87 + 58 + 24 + 11 + 5 + 8 = **310**

## Interpretation

The application contains **562 Physical SLOC** and **310 Logical SLOC** under the documented 
measurement conventions.

`CBankGUI.java` has the highest Physical SLOC (144).

SLOC measures source-code size. It does not directly establish readability or maintainability.
