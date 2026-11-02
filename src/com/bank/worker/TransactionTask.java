package com.bank.worker;

import com.bank.exception.InsufficientFundsException;
import com.bank.exception.InvalidAmountException;
import com.bank.model.Account;
import com.bank.service.TransactionLogger;

public class TransactionTask implements Runnable {
    public enum Type { DEPOSIT, WITHDRAW }

    private final Account account;
    private final Type type;
    private final double amount;
    private final TransactionLogger logger;

    public TransactionTask(Account account, Type type, double amount, TransactionLogger logger) {
        this.account = account;
        this.type = type;
        this.amount = amount;
        this.logger = logger;
    }

    @Override
    public void run() {
        String thread = Thread.currentThread().getName();
        try {
            if (type == Type.DEPOSIT) {
                account.deposit(amount);
            } else {
                account.withdraw(amount);
            }
            logger.log(thread + " | " + account.getAccountNumber() + " | "
                    + type + " | " + amount + " | SUCCESS");
        } catch (InvalidAmountException | InsufficientFundsException e) {
            logger.log(thread + " | " + account.getAccountNumber() + " | "
                    + type + " | " + amount + " | FAILED: " + e.getMessage());
        }
    }
}
