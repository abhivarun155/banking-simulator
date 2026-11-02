package com.bank;

import com.bank.exception.AccountNotFoundException;
import com.bank.exception.InsufficientFundsException;
import com.bank.exception.InvalidAmountException;
import com.bank.model.Account;
import com.bank.model.CurrentAccount;
import com.bank.model.SavingsAccount;
import com.bank.service.BankService;
import com.bank.service.TransactionLogger;
import com.bank.worker.TransactionTask;
import com.bank.worker.TransactionTask.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) throws Exception {
        TransactionLogger logger = new TransactionLogger();
        BankService bank = new BankService();

        Account s1 = new SavingsAccount("SA101", "Asha", 5000);
        Account c1 = new CurrentAccount("CA201", "Ravi", 2000);
        bank.addAccount(s1);
        bank.addAccount(c1);

        System.out.println("=== Opening balances ===");
        bank.getAllAccounts().forEach(a -> System.out.println(a.toAuditRecord()));

        // 1. Exceptions, one by one
        System.out.println("\n=== Exception demo ===");
        try {
            s1.deposit(-100);
        } catch (InvalidAmountException e) {
            System.out.println("Error: " + e.getMessage());
        }
        try {
            s1.withdraw(99999);
        } catch (InsufficientFundsException | InvalidAmountException e) {
            System.out.println("Error: " + e.getMessage());
        }
        try {
            bank.getAccount("XX999");
        } catch (AccountNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }

        // 2. Many threads on the same accounts
        System.out.println("\n=== Running 30 concurrent transactions ===");
        ExecutorService pool = Executors.newFixedThreadPool(4);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            futures.add(pool.submit(new TransactionTask(s1, Type.DEPOSIT, 100, logger)));
            futures.add(pool.submit(new TransactionTask(s1, Type.WITHDRAW, 50, logger)));
            futures.add(pool.submit(new TransactionTask(c1, Type.WITHDRAW, 300, logger)));
        }
        for (Future<?> f : futures) {
            f.get(); // wait for each task to finish
        }
        pool.shutdown();

        // 3. Transfer
        System.out.println("\n=== Transfer 1000 from SA101 to CA201 ===");
        bank.transfer("SA101", "CA201", 1000);

        System.out.println("\n=== Final balances ===");
        bank.getAllAccounts().forEach(a -> System.out.println(a.toAuditRecord()));
        System.out.println("Interest on savings: " + s1.calculateInterest());
        System.out.println("Interest on current: " + c1.calculateInterest());

        System.out.println("\n=== History read back from data/transactions.log ===");
        logger.readHistory().forEach(System.out::println);
    }
}
