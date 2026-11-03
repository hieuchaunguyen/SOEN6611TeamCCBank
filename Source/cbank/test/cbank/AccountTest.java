
package cbank;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Automated unit tests for the Account class.
 *
 * Course: SOEN 6611 - Software Measurement
 * Deliverable: D2, Problem 4(b)
 *
 * Objective:
 * Verify account operations using representative valid,
 * invalid, and boundary test data.
 *
 * Testing approach:
 * - Each test creates a fresh account with an initial balance
 *   of CAD 1500.00 to ensure test independence.
 * - Expected outcomes are defined before execution.
 * - Actual outcomes are checked automatically.
 * - PASS/FAIL results are displayed in the console.
 *
 * No external testing framework or database is required.
 */
public class AccountTest {

    private static int passed = 0;
    private static int failed = 0;

    /**
     * Executes the complete Account unit test suite.
     */
    public static void main(String[] args) {


        // Save test output to Tools/TestResults on every execution.
        try {
            Path reportDirectory = Path.of(
                    "Tools", "TestResults"
            );

            Files.createDirectories(reportDirectory);

            Path reportFile = reportDirectory.resolve(
                    "account_unit_test_results.txt"
            );

            PrintStream reportStream = new PrintStream(
                    Files.newOutputStream(reportFile),
                    true,
                    StandardCharsets.UTF_8
            );

            PrintStream consoleStream = System.out;

            // Display results in IntelliJ and save them to a file.
            System.setOut(new PrintStream(
                    new java.io.OutputStream() {
                        @Override
                        public void write(int b) throws IOException {
                            consoleStream.write(b);
                            reportStream.write(b);
                        }

                        @Override
                        public void flush() throws IOException {
                            consoleStream.flush();
                            reportStream.flush();
                        }
                    },
                    true,
                    StandardCharsets.UTF_8
            ));

        } catch (IOException e) {
            System.err.println(
                    "Unable to save test report: " + e.getMessage()
            );
            System.exit(1);
        }

        System.out.println("======================================");
        System.out.println("CBank - Account Unit Testing");
        System.out.println("SOEN 6611 | D2 | Problem 4(b)");
        System.out.println("======================================");

        // TC-A01: Verify that a valid deposit increases balance.
        run("TC-A01", "Valid deposit of CAD 100",
                "Balance = CAD 1600.00", () -> {
                    Account account = createTestAccount();
                    account.deposit(100);

                    assertBalance(account, 1600);
                    return formatBalance(account);
                });

        // TC-A02: Verify that a valid withdrawal reduces balance.
        run("TC-A02", "Valid withdrawal of CAD 200",
                "Balance = CAD 1300.00", () -> {
                    Account account = createTestAccount();
                    account.withdraw(200);

                    assertBalance(account, 1300);
                    return formatBalance(account);
                });

        // TC-A03: Reject withdrawals exceeding available funds.
        run("TC-A03", "Insufficient account funds",
                "InsufficientFundsException; balance = CAD 1500.00",
                () -> {
                    Account account = createTestAccount();

                    String exception = expectException(
                            CustomExceptions.InsufficientFundsException.class,
                            () -> account.withdraw(2000)
                    );

                    assertBalance(account, 1500);
                    return exception + "; " + formatBalance(account);
                });

        // TC-A04: Reject negative withdrawal amounts.
        run("TC-A04", "Negative withdrawal of CAD -100",
                "IllegalArgumentException; balance = CAD 1500.00",
                () -> {
                    Account account = createTestAccount();

                    String exception = expectException(
                            IllegalArgumentException.class,
                            () -> account.withdraw(-100)
                    );

                    assertBalance(account, 1500);
                    return exception + "; " + formatBalance(account);
                });

        // TC-A05: Reject zero-value withdrawals.
        run("TC-A05", "Zero withdrawal",
                "IllegalArgumentException; balance = CAD 1500.00",
                () -> {
                    Account account = createTestAccount();

                    String exception = expectException(
                            IllegalArgumentException.class,
                            () -> account.withdraw(0)
                    );

                    assertBalance(account, 1500);
                    return exception + "; " + formatBalance(account);
                });

        // TC-A06: Reject negative deposit amounts.
        run("TC-A06", "Negative deposit of CAD -100",
                "IllegalArgumentException; balance = CAD 1500.00",
                () -> {
                    Account account = createTestAccount();

                    String exception = expectException(
                            IllegalArgumentException.class,
                            () -> account.deposit(-100)
                    );

                    assertBalance(account, 1500);
                    return exception + "; " + formatBalance(account);
                });

        // TC-A07: Reject zero-value deposits.
        run("TC-A07", "Zero deposit",
                "IllegalArgumentException; balance = CAD 1500.00",
                () -> {
                    Account account = createTestAccount();

                    String exception = expectException(
                            IllegalArgumentException.class,
                            () -> account.deposit(0)
                    );

                    assertBalance(account, 1500);
                    return exception + "; " + formatBalance(account);
                });

        // TC-A08: Verify correct and incorrect PIN handling.
        run("TC-A08", "PIN validation",
                "Correct PIN = true; incorrect PIN = false",
                () -> {
                    Account account = createTestAccount();

                    boolean correct = account.validatePin("1234");
                    boolean incorrect = account.validatePin("9999");

                    assertCondition(correct,
                            "Valid PIN was rejected");
                    assertCondition(!incorrect,
                            "Invalid PIN was accepted");

                    return "Correct PIN = " + correct
                            + "; incorrect PIN = " + incorrect;
                });

        // Display overall test results.
        System.out.println("\n======================================");
        System.out.println("TEST EXECUTION SUMMARY");
        System.out.println("Total tests : " + (passed + failed));
        System.out.println("Passed      : " + passed);
        System.out.println("Failed      : " + failed);
        System.out.println("======================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    /**
     * Creates an independent account for each test case.
     *
     * @return a new account with CAD 1500.00 balance
     */
    private static Account createTestAccount() {
        return new Account(
                "1001", "Alice Smith", "1234", 1500.00, false
        );
    }

    /**
     * Checks whether the actual balance matches the expected
     * balance, allowing a small floating-point tolerance.
     */
    private static void assertBalance(
            Account account, double expected
    ) {
        double actual = account.getBalanceCad();

        assertCondition(
                Math.abs(actual - expected) < 0.001,
                "Expected balance " + expected
                        + ", but received " + actual
        );
    }

    /**
     * Verifies a condition and reports an assertion failure
     * if the condition is false.
     */
    private static void assertCondition(
            boolean condition, String message
    ) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    /**
     * Executes an action and verifies that the expected
     * exception type is thrown.
     *
     * @return the actual exception type name
     */
    private static String expectException(
            Class<? extends Throwable> expected,
            TestAction action
    ) throws Exception {

        try {
            action.execute();
        } catch (Throwable exception) {
            if (expected.isInstance(exception)) {
                return exception.getClass().getSimpleName();
            }

            throw new AssertionError(
                    "Unexpected exception: " + exception
            );
        }

        throw new AssertionError(
                "Expected exception not thrown: "
                        + expected.getSimpleName()
        );
    }

    /**
     * Formats the account balance for reporting.
     */
    private static String formatBalance(Account account) {
        return String.format(
                java.util.Locale.US,
                "Balance = CAD %.2f",
                account.getBalanceCad()
        );
    }

    /**
     * Executes one test and records its result.
     *
     * The test is marked PASS only when all assertions
     * complete successfully.
     */
    private static void run(
            String testId,
            String description,
            String expected,
            TestCase test
    ) {
        System.out.println("\n" + testId + " - " + description);
        System.out.println("Expected: " + expected);

        try {
            String actual = test.execute();

            System.out.println("Actual  : " + actual);
            System.out.println("Result  : PASS");
            passed++;

        } catch (Throwable exception) {
            System.out.println(
                    "Actual  : " + exception
            );
            System.out.println("Result  : FAIL");
            failed++;
        }
    }

    /**
     * Functional interface for a test action.
     */
    @FunctionalInterface
    private interface TestAction {
        void execute() throws Exception;
    }

    /**
     * Functional interface for an executable test case.
     */
    @FunctionalInterface
    private interface TestCase {
        String execute() throws Exception;
    }
}
