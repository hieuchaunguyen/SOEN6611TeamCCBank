package cbank;

import cbank.account.Account;
import cbank.account.AdminAccount;
import cbank.account.CommercialAccount;
import cbank.account.IndividualAccount;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SOEN 6611 - CBank V2.0 - D2 Problem 4(b)
 * JUnit 5 account unit tests: positive, negative, boundary, and cumulative-limit cases.
 * Each test creates a fresh account, so tests do not share mutable state.
 * Expected/actual observations are written to console and Tools/TestResults.
 * JUnit, not a printed message, determines PASS/FAIL.
 * Run from the repository root so the report is saved in the intended folder.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
class AccountTest {
    private static final double EPS = 0.001;
    private static BufferedWriter report;

    /** Start a new evidence report for this complete JUnit class execution. */
    @BeforeAll
    static void openReport() throws IOException {
        Path dir = Path.of("Tools", "TestResults");
        Files.createDirectories(dir);
        report = Files.newBufferedWriter(dir.resolve("account_junit_results.txt"),
                StandardCharsets.UTF_8);
        record("CBank V2.0 | Account JUnit 5 | D2 Problem 4(b)");
        record("Expected and actual values; PASS/FAIL is reported by JUnit 5.");
    }

    /** JUnit reports the final status only after a test has completed. */
    @RegisterExtension
    static final TestWatcher watcher = new TestWatcher() {
        @Override
        public void testSuccessful(ExtensionContext context) {
            record("RESULT: PASS | " + context.getDisplayName() + "\n");
        }
        @Override
        public void testFailed(ExtensionContext context, Throwable cause) {
            record("RESULT: FAIL | " + context.getDisplayName()
                    + " | " + cause + "\n");
        }
    };

    /** Close the report after JUnit completes the test class. */
    @AfterAll
    static void closeReport() throws IOException {
        if (report != null) report.close();
    }

    /** Record an observation in both the console and the report file. */
    private static synchronized void record(String message) {
        System.out.println(message);
        if (report != null) {
            try {
                report.write(message);
                report.newLine();
                report.flush();
            } catch (IOException e) {
                throw new IllegalStateException("Unable to write test report", e);
            }
        }
    }

    /** Compare monetary values using a tolerance for floating-point calculations. */
    private static void verifyBalance(String id, double expected, Account account) {
        double actual = account.getBalanceCad();
        record(String.format(Locale.US,
                "%s | Balance | Expected: CAD %.2f | Actual: CAD %.2f",
                id, expected, actual));
        assertEquals(expected, actual, EPS);
    }

    /** Verify the precise exception type and record what was observed. */
    private static <T extends Throwable> void verifyException(
            String id, Class<T> expected, ExecutableAction action) {
        try {
            T actual = assertThrows(expected, action::run);
            record(id + " | Exception | Expected: " + expected.getSimpleName()
                    + " | Actual: " + actual.getClass().getSimpleName());
        } catch (AssertionError error) {
            record(id + " | Exception | Expected: " + expected.getSimpleName()
                    + " | Actual: unexpected exception or none");
            throw error;
        }
    }

    @FunctionalInterface
    private interface ExecutableAction {
        void run() throws Throwable;
    }

    /** Create independent accounts with their actual V2.0 account-type rules. */
    private Account individual(double balance) {
        return new Account("1001", "Test Individual", "1234", balance,
                new IndividualAccount());
    }
    private Account commercial(double balance) {
        return new Account("2001", "Test Commercial", "5678", balance,
                new CommercialAccount());
    }
    private Account admin(double balance) {
        return new Account("9999", "Test Admin", "0000", balance,
                new AdminAccount());
    }

    /** TC-A01: A valid deposit increases the account balance. */
    @Test void test01ValidDeposit() throws Exception {
        Account a = individual(1500);
        a.deposit(100);
        verifyBalance("TC-A01", 1600, a);
    }

    /** TC-A02: A valid withdrawal decreases the account balance. */
    @Test void test02ValidWithdrawal() throws Exception {
        Account a = individual(1500);
        a.withdraw(200);
        verifyBalance("TC-A02", 1300, a);
    }

    /** TC-A03: Insufficient account funds must reject a withdrawal. */
    @Test void test03InsufficientFunds() {
        Account a = individual(1500);
        verifyException("TC-A03", CustomExceptions.InsufficientFundsException.class,
                () -> a.withdraw(2000));
        verifyBalance("TC-A03", 1500, a);
    }

    /** TC-A04: Negative deposits must be rejected without changing the balance. */
    @Test void test04NegativeDeposit() {
        Account a = individual(1500);
        verifyException("TC-A04", CustomExceptions.InvalidTransactionException.class,
                () -> a.deposit(-100));
        verifyBalance("TC-A04", 1500, a);
    }

    /** TC-A05: Zero is not a valid deposit amount. */
    @Test void test05ZeroDeposit() {
        Account a = individual(1500);
        verifyException("TC-A05", CustomExceptions.InvalidTransactionException.class,
                () -> a.deposit(0));
        verifyBalance("TC-A05", 1500, a);
    }

    /** TC-A06: Negative withdrawals must be rejected without changing the balance. */
    @Test void test06NegativeWithdrawal() {
        Account a = individual(1500);
        verifyException("TC-A06", CustomExceptions.InvalidTransactionException.class,
                () -> a.withdraw(-100));
        verifyBalance("TC-A06", 1500, a);
    }

    /** TC-A07: Zero is not a valid withdrawal amount. */
    @Test void test07ZeroWithdrawal() {
        Account a = individual(1500);
        verifyException("TC-A07", CustomExceptions.InvalidTransactionException.class,
                () -> a.withdraw(0));
        verifyBalance("TC-A07", 1500, a);
    }

    /** TC-A08: A correct PIN succeeds; an incorrect PIN fails validation. */
    @Test void test08PinValidation() {
        Account a = individual(1500);
        boolean correct = a.validatePin("1234");
        boolean incorrect = a.validatePin("9999");
        record("TC-A08 | PIN | Expected: true/false | Actual: "
                + correct + "/" + incorrect);
        assertTrue(correct);
        assertFalse(incorrect);
    }

    /** TC-A09: Individual deposit exactly at CAD 2,000 limit is allowed. */
    @Test void test09IndividualDepositAtLimit() throws Exception {
        Account a = individual(1500);
        a.deposit(2000);
        verifyBalance("TC-A09", 3500, a);
    }

    /** TC-A10: Individual deposit one cent above the limit is rejected. */
    @Test void test10IndividualDepositAboveLimit() {
        Account a = individual(1500);
        verifyException("TC-A10", CustomExceptions.InvalidTransactionException.class,
                () -> a.deposit(2000.01));
        verifyBalance("TC-A10", 1500, a);
    }

    /** TC-A11: Individual withdrawal exactly at CAD 1,000 limit is allowed. */
    @Test void test11IndividualWithdrawalAtLimit() throws Exception {
        Account a = individual(2000);
        a.withdraw(1000);
        verifyBalance("TC-A11", 1000, a);
    }

    /** TC-A12: Individual withdrawal above limit is rejected despite sufficient funds. */
    @Test void test12IndividualWithdrawalAboveLimit() {
        Account a = individual(2000);
        verifyException("TC-A12", CustomExceptions.InvalidTransactionException.class,
                () -> a.withdraw(1000.01));
        verifyBalance("TC-A12", 2000, a);
    }

    /** TC-A13: Separate deposits cumulatively consume the deposit allowance. */
    @Test void test13CumulativeDepositLimit() throws Exception {
        Account a = individual(1500);
        a.deposit(1200);
        a.deposit(800);
        verifyException("TC-A13", CustomExceptions.InvalidTransactionException.class,
                () -> a.deposit(1));
        verifyBalance("TC-A13", 3500, a);
    }

    /** TC-A14: Separate withdrawals cumulatively consume the withdrawal allowance. */
    @Test void test14CumulativeWithdrawalLimit() throws Exception {
        Account a = individual(2000);
        a.withdraw(600);
        a.withdraw(400);
        verifyException("TC-A14", CustomExceptions.InvalidTransactionException.class,
                () -> a.withdraw(1));
        verifyBalance("TC-A14", 1000, a);
    }

    /** TC-A15: Commercial accounts allow their larger configured transaction limits. */
    @Test void test15CommercialHigherLimits() throws Exception {
        Account a = commercial(1500000);
        a.deposit(2000000);
        a.withdraw(1000000);
        verifyBalance("TC-A15", 2500000, a);
        record("TC-A15 | Admin role | Expected: false | Actual: " + a.isAdmin());
        assertFalse(a.isAdmin());
    }

    /** TC-A16: Commercial deposits above CAD 2,000,000 are rejected. */
    @Test void test16CommercialDepositAboveLimit() {
        Account a = commercial(1500000);
        verifyException("TC-A16", CustomExceptions.InvalidTransactionException.class,
                () -> a.deposit(2000000.01));
        verifyBalance("TC-A16", 1500000, a);
    }

    /** TC-A17: Commercial withdrawals above CAD 1,000,000 are rejected. */
    @Test void test17CommercialWithdrawalAboveLimit() {
        Account a = commercial(1500000);
        verifyException("TC-A17", CustomExceptions.InvalidTransactionException.class,
                () -> a.withdraw(1000000.01));
        verifyBalance("TC-A17", 1500000, a);
    }

    /** TC-A18: Admin accounts use -1 to represent unlimited transaction allowances. */
    @Test void test18AdminUnlimitedTransactions() throws Exception {
        Account a = admin(5000000);
        a.deposit(3000000);
        a.withdraw(2000000);
        verifyBalance("TC-A18", 6000000, a);
        record("TC-A18 | Admin role | Expected: true | Actual: " + a.isAdmin());
        assertTrue(a.isAdmin());
    }

    /** TC-A19: Public getters expose the three account types' configured limits. */
    @Test void test19AccountLimitConfiguration() {
        Account i = individual(1500);
        Account c = commercial(1500);
        Account a = admin(1500);
        record("TC-A19 | Individual limits | Expected: 2000/1000 | Actual: "
                + i.getDepositLimit() + "/" + i.getWithdrawLimit());
        assertEquals(2000, i.getDepositLimit(), EPS);
        assertEquals(1000, i.getWithdrawLimit(), EPS);
        record("TC-A19 | Commercial limits | Expected: 2000000/1000000 | Actual: "
                + c.getDepositLimit() + "/" + c.getWithdrawLimit());
        assertEquals(2000000, c.getDepositLimit(), EPS);
        assertEquals(1000000, c.getWithdrawLimit(), EPS);
        record("TC-A19 | Admin limits | Expected: -1/-1 | Actual: "
                + a.getDepositLimit() + "/" + a.getWithdrawLimit());
        assertEquals(-1, a.getDepositLimit(), EPS);
        assertEquals(-1, a.getWithdrawLimit(), EPS);
    }
}
