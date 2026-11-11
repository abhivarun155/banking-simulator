package com.bank;

import com.bank.exception.AccountNotFoundException;
import com.bank.exception.DuplicateAccountException;
import com.bank.exception.InsufficientFundsException;
import com.bank.exception.InvalidAmountException;
import com.bank.model.Account;
import com.bank.model.CurrentAccount;
import com.bank.model.SavingsAccount;
import com.bank.service.AccountStorage;
import com.bank.service.BankService;
import com.bank.service.TransactionLogger;
import com.bank.worker.TransactionTask;
import com.bank.worker.TransactionTask.Type;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    private static final Scanner in = new Scanner(System.in);
    private static final BankService bank = new BankService();
    private static TransactionLogger logger;
    private static AccountStorage storage;

    public static void main(String[] args) throws IOException {
        logger = new TransactionLogger();
        storage = new AccountStorage();

        for (Account a : storage.load()) {
            bank.addAccount(a);
        }
        System.out.println("Welcome to the Banking Simulator");
        System.out.println(bank.getAllAccounts().size() + " account(s) loaded from file.");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = readText("Enter your choice: ");
            try {
                switch (choice) {
                    case "1": openAccount(); break;
                    case "2": closeAccount(); break;
                    case "3": deposit(); break;
                    case "4": withdraw(); break;
                    case "5": transfer(); break;
                    case "6": viewAccounts(); break;
                    case "7": accountDetails(); break;
                    case "8": viewHistory(); break;
                    case "9": concurrentDemo(); break;
                    case "0": running = false; break;
                    default: System.out.println("Invalid choice, please try again.");
                }
            } catch (AccountNotFoundException | InvalidAmountException
                     | InsufficientFundsException | DuplicateAccountException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Error: please enter a valid number.");
            } catch (IOException e) {
                System.out.println("File error: " + e.getMessage());
            }
        }
        System.out.println("Thank you for using the Banking Simulator. Goodbye!");
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("========== BANK MENU ==========");
        System.out.println("1. Open a new account");
        System.out.println("2. Close (remove) an account");
        System.out.println("3. Deposit money");
        System.out.println("4. Withdraw money");
        System.out.println("5. Transfer money");
        System.out.println("6. View all accounts");
        System.out.println("7. View account details and interest");
        System.out.println("8. View transaction history");
        System.out.println("9. Run multithreading demo");
        System.out.println("0. Exit");
        System.out.println("===============================");
    }

    // ---------- menu actions ----------

    private static void openAccount()
            throws DuplicateAccountException, InvalidAmountException, IOException {
        String type = readText("Account type (1 = Savings, 2 = Current): ");
        if (!type.equals("1") && !type.equals("2")) {
            System.out.println("Invalid account type.");
            return;
        }
        String number = readAccountNumber("New account number: ");
        String name = readText("Holder name: ").replace(",", " ");
        if (number.isEmpty() || name.isEmpty()) {
            System.out.println("Account number and name cannot be empty.");
            return;
        }
        double opening = readDouble("Opening balance: ");

        Account acc;
        if (type.equals("1")) {
            if (opening < SavingsAccount.MIN_BALANCE) {
                throw new InvalidAmountException(
                        "A savings account needs at least " + SavingsAccount.MIN_BALANCE + " to open");
            }
            acc = new SavingsAccount(number, name, opening);
        } else {
            if (opening < 0) {
                throw new InvalidAmountException("Opening balance cannot be negative");
            }
            acc = new CurrentAccount(number, name, opening);
        }

        bank.openAccount(acc);
        record(acc, "OPEN", opening, "SUCCESS");
        storage.save(bank.getAllAccounts());
        System.out.println("Account " + number + " opened for " + name + ".");
    }

    private static void closeAccount() throws AccountNotFoundException, IOException {
        Account acc = bank.getAccount(readAccountNumber("Account number to close: "));
        System.out.println(acc.toAuditRecord());
        String sure = readText("Close this account? (y/n): ");
        if (!sure.equalsIgnoreCase("y")) {
            System.out.println("Cancelled. The account was not closed.");
            return;
        }
        double remaining = acc.getBalance();
        bank.removeAccount(acc.getAccountNumber());
        record(acc, "CLOSE", remaining, "SUCCESS");
        storage.save(bank.getAllAccounts());
        System.out.println("Account closed. Remaining balance of " + remaining + " paid out.");
    }

    private static void deposit()
            throws AccountNotFoundException, InvalidAmountException, IOException {
        Account acc = bank.getAccount(readAccountNumber("Account number: "));
        double amount = readDouble("Amount to deposit: ");
        try {
            acc.deposit(amount);
        } catch (InvalidAmountException e) {
            record(acc, "DEPOSIT", amount, "FAILED: " + e.getMessage());
            throw e;
        }
        record(acc, "DEPOSIT", amount, "SUCCESS");
        storage.save(bank.getAllAccounts());
        System.out.println("Deposited " + amount + ". New balance: " + acc.getBalance());
    }

    private static void withdraw()
            throws AccountNotFoundException, InvalidAmountException,
                   InsufficientFundsException, IOException {
        Account acc = bank.getAccount(readAccountNumber("Account number: "));
        double amount = readDouble("Amount to withdraw: ");
        try {
            acc.withdraw(amount);
        } catch (InvalidAmountException | InsufficientFundsException e) {
            record(acc, "WITHDRAW", amount, "FAILED: " + e.getMessage());
            throw e;
        }
        record(acc, "WITHDRAW", amount, "SUCCESS");
        storage.save(bank.getAllAccounts());
        System.out.println("Withdrew " + amount + ". New balance: " + acc.getBalance());
    }

    private static void transfer()
            throws AccountNotFoundException, InvalidAmountException,
                   InsufficientFundsException, IOException {
        String from = readAccountNumber("Transfer from account: ");
        String to = readAccountNumber("Transfer to account: ");
        if (from.equals(to)) {
            System.out.println("Error: you cannot transfer to the same account.");
            return;
        }
        double amount = readDouble("Amount to transfer: ");
        try {
            bank.transfer(from, to, amount);
        } catch (AccountNotFoundException | InvalidAmountException
                 | InsufficientFundsException e) {
            logger.log("main | " + from + " -> " + to + " | TRANSFER | "
                    + amount + " | FAILED: " + e.getMessage());
            throw e;
        }
        logger.log("main | " + from + " -> " + to + " | TRANSFER | " + amount + " | SUCCESS");
        storage.save(bank.getAllAccounts());
        System.out.println("Transferred " + amount + " from " + from + " to " + to + ".");
    }

    private static void viewAccounts() {
        List<Account> all = bank.getAllAccounts();
        if (all.isEmpty()) {
            System.out.println("There are no accounts yet. Use option 1 to open one.");
            return;
        }
        System.out.printf("%-10s %-16s %-15s %12s%n", "Number", "Type", "Holder", "Balance");
        for (Account a : all) {
            System.out.printf("%-10s %-16s %-15s %12.2f%n",
                    a.getAccountNumber(), a.getClass().getSimpleName(),
                    a.getHolderName(), a.getBalance());
        }
    }

    private static void accountDetails() throws AccountNotFoundException {
        Account acc = bank.getAccount(readAccountNumber("Account number: "));
        System.out.println("Account number : " + acc.getAccountNumber());
        System.out.println("Type           : " + acc.getClass().getSimpleName());
        System.out.println("Holder         : " + acc.getHolderName());
        System.out.println("Balance        : " + acc.getBalance());
        System.out.println("Yearly interest: " + acc.calculateInterest());
    }

    private static void viewHistory() throws IOException {
        List<String> lines = logger.readHistory();
        if (lines.isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }
        int start = Math.max(0, lines.size() - 20);
        System.out.println("Showing the last " + (lines.size() - start)
                + " of " + lines.size() + " transactions:");
        for (int i = start; i < lines.size(); i++) {
            System.out.println(lines.get(i));
        }
    }

    // 10 deposits and 10 withdrawals on one account, run by 4 threads at once
    private static void concurrentDemo() throws AccountNotFoundException, IOException {
        Account acc = bank.getAccount(readAccountNumber("Account number for the demo: "));
        double before = acc.getBalance();
        System.out.println("Balance before: " + before);
        System.out.println("Running 10 deposits of 100 and 10 withdrawals of 50 on 4 threads...");

        ExecutorService pool = Executors.newFixedThreadPool(4);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            futures.add(pool.submit(new TransactionTask(acc, Type.DEPOSIT, 100, logger)));
            futures.add(pool.submit(new TransactionTask(acc, Type.WITHDRAW, 50, logger)));
        }
        try {
            for (Future<?> f : futures) {
                f.get(); // wait for each task to finish
            }
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("The demo was interrupted: " + e.getMessage());
        } finally {
            pool.shutdown();
        }

        double after = acc.getBalance();
        System.out.println("Balance after : " + after);
        System.out.println("Net change    : " + (after - before)
                + " (+500 if every transaction succeeded)");
        System.out.println("Check option 8 to see which thread did each transaction.");
        storage.save(bank.getAllAccounts());
    }

    // ---------- helpers ----------

    private static void record(Account acc, String action, double amount, String result) {
        logger.log("main | " + acc.getAccountNumber() + " | " + action + " | "
                + amount + " | " + result);
    }

    private static String readText(String prompt) {
        System.out.print(prompt);
        return in.nextLine().trim();
    }

    private static String readAccountNumber(String prompt) {
        return readText(prompt).toUpperCase();
    }

    private static double readDouble(String prompt) {
        return Double.parseDouble(readText(prompt));
    }
}
