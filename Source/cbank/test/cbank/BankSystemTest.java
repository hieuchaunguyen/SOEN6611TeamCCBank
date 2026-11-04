
package cbank;

import cbank.account.Account;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Integration tests for CBank's BankSystem.
 *
 * SOEN 6611 - Deliverable 2 - Problem 4(b)
 *
 * Tests authentication, transactions, vault balances,
 * PIN management, and administrator authorization.
 *
 * Uses the representative accounts initialized by BankSystem.
 * Tests execute sequentially because BankSystem is a singleton.
 *
 * Each test reports its expected and actual outcomes.
 * Results are automatically saved, replacing the previous report.
 */
public class BankSystemTest {

    private static int passed = 0;
    private static int failed = 0;

    private static final double EPSILON = 0.001;

    public static void main(String[] args) {

        configureReport();

        System.out.println("======================================");
        System.out.println("CBank - BankSystem Integration Testing");
        System.out.println("SOEN 6611 | D2 | Problem 4(b)");
        System.out.println("======================================");

        BankSystem bank = BankSystem.getInstance();

        // TC-B01: Valid customer authentication.
        run("TC-B01", "Valid customer login",
                "Alice Smith authenticated", () -> {

                    Account user = bank.login("1001", "1234");

                    check(user != null
                                    && user.getAccountNumber().equals("1001"),
                            "Incorrect authenticated account");

                    return user.getAccountName() + " authenticated";
                });

        // TC-B02: Invalid authentication must be rejected.
        run("TC-B02", "Invalid PIN",
                "AuthenticationException", () -> {

                    String exception = expectException(
                            CustomExceptions.AuthenticationException.class,
                            () -> bank.login("1001", "9999")
                    );

                    return exception;
                });

        // Restore the customer session after invalid login.
        // This is setup, not a separate test result.
        try {
            bank.logout();
            bank.login("1001", "1234");
        } catch (Exception e) {
            System.err.println("Test setup failed: " + e);
            System.exit(1);
        }

        // TC-B03: A valid withdrawal reduces both balances.
        run("TC-B03", "Withdraw CAD 200",
                "Account = CAD 1300.00; Vault = CAD 4800.00",
                () -> {

                    bank.withdrawCash(200);

                    checkBalance(
                            bank.getCurrentSessionUser().getBalanceCad(),
                            1300
                    );

                    checkBalance(bank.getMachineCashVaultCad(), 4800);

                    return balances(bank);
                });

        // TC-B04: Reject a withdrawal exceeding account funds.
        run("TC-B04", "Insufficient account funds",
                "InsufficientFundsException; balances unchanged",
                () -> {

                    String exception = expectException(
                            CustomExceptions.InsufficientFundsException.class,
                            () -> bank.withdrawCash(2000)
                    );

                    checkBalance(
                            bank.getCurrentSessionUser().getBalanceCad(),
                            1300
                    );
                    checkBalance(bank.getMachineCashVaultCad(), 4800);

                    return exception + "; " + balances(bank);
                });

        // TC-B05: CAD deposit credits account and CAD vault.
        run("TC-B05", "Deposit CAD 100",
                "Account = CAD 1400.00; Vault = CAD 4900.00",
                () -> {

                    bank.depositCash(100, "CAD");

                    checkBalance(
                            bank.getCurrentSessionUser().getBalanceCad(),
                            1400
                    );
                    checkBalance(bank.getMachineCashVaultCad(), 4900);

                    return balances(bank);
                });

        // TC-B06: Invalid PIN format must be rejected.
        run("TC-B06", "Change PIN to three digits",
                "InvalidPinException", () -> {

                    return expectException(
                            CustomExceptions.InvalidPinException.class,
                            () -> bank.changePin("123")
                    );
                });

        // TC-B07: A valid PIN change must take effect.
        run("TC-B07", "Change PIN to 4321",
                "New PIN accepted; old PIN rejected", () -> {

                    bank.changePin("4321");

                    Account user = bank.getCurrentSessionUser();

                    check(user.validatePin("4321"),
                            "New PIN was rejected");
                    check(!user.validatePin("1234"),
                            "Old PIN remains valid");

                    return "New PIN accepted; old PIN rejected";
                });

        // TC-B08: Customer must not access admin audit logs.
        run("TC-B08", "Customer requests audit logs",
                "SecurityException", () -> {

                    return expectException(
                            SecurityException.class,
                            bank::getAuditLogsFormatted
                    );
                });

        // TC-B09: Customer must not replenish the vault.
        run("TC-B09", "Customer replenishes vault",
                "SecurityException; vault unchanged", () -> {

                    String exception = expectException(
                            SecurityException.class,
                            () -> bank.adminReplenishVault(100)
                    );

                    checkBalance(bank.getMachineCashVaultCad(), 4900);

                    return exception + "; vault unchanged";
                });

        // TC-B10: Administrator can replenish the cash vault.
        run("TC-B10", "Admin replenishes CAD 500",
                "Vault = CAD 5400.00", () -> {

                    bank.logout();
                    bank.login("9999", "0000");
                    bank.adminReplenishVault(500);

                    checkBalance(bank.getMachineCashVaultCad(), 5400);

                    return String.format(
                            Locale.US,
                            "Vault = CAD %.2f",
                            bank.getMachineCashVaultCad()
                    );
                });

        bank.logout();

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
     * Redirects output to both the IntelliJ console and a
     * report file. Existing report content is overwritten.
     *
     * Requires the working directory to be the repository root.
     */
    private static void configureReport() {
        try {
            Path directory = Path.of("Tools", "TestResults");
            Files.createDirectories(directory);

            Path file = directory.resolve(
                    "bank_system_test_results.txt"
            );

            PrintStream console = System.out;
            PrintStream report = new PrintStream(
                    Files.newOutputStream(file),
                    true,
                    StandardCharsets.UTF_8
            );

            System.setOut(new PrintStream(
                    new OutputStream() {
                        @Override
                        public void write(int b) {
                            console.write(b);
                            report.write(b);
                        }

                        @Override
                        public void flush() {
                            console.flush();
                            report.flush();
                        }
                    },
                    true,
                    StandardCharsets.UTF_8
            ));

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to create test report", e
            );
        }
    }

    /**
     * Checks a numeric balance using a small tolerance.
     */
    private static void checkBalance(
            double actual, double expected
    ) {
        check(
                Math.abs(actual - expected) < EPSILON,
                "Expected " + expected + ", received " + actual
        );
    }

    /**
     * Checks a test assertion.
     */
    private static void check(
            boolean condition, String message
    ) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    /**
     * Confirms that the expected exception is thrown.
     */
    private static String expectException(
            Class<? extends Throwable> expected,
            TestAction action
    ) throws Exception {
        try {
            action.execute();
        } catch (Throwable e) {
            if (expected.isInstance(e)) {
                return e.getClass().getSimpleName();
            }

            throw new AssertionError(
                    "Unexpected exception: " + e
            );
        }

        throw new AssertionError(
                "Expected " + expected.getSimpleName()
        );
    }

    /**
     * Formats the current customer and vault balances.
     */
    private static String balances(BankSystem bank) {
        return String.format(
                Locale.US,
                "Account = CAD %.2f; Vault = CAD %.2f",
                bank.getCurrentSessionUser().getBalanceCad(),
                bank.getMachineCashVaultCad()
        );
    }

    /**
     * Executes and reports an individual integration test.
     */
    private static void run(
            String id,
            String description,
            String expected,
            TestCase test
    ) {
        System.out.println("\n" + id + " - " + description);
        System.out.println("Expected: " + expected);

        try {
            String actual = test.execute();
            System.out.println("Actual  : " + actual);
            System.out.println("Result  : PASS");
            passed++;
        } catch (Throwable e) {
            System.out.println("Actual  : " + e);
            System.out.println("Result  : FAIL");
            failed++;
        }
    }

    @FunctionalInterface
    private interface TestAction {
        void execute() throws Exception;
    }

    @FunctionalInterface
    private interface TestCase {
        String execute() throws Exception;
    }
}
