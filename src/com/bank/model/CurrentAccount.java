package com.bank.model;

import com.bank.exception.InsufficientFundsException;
import com.bank.exception.InvalidAmountException;

public class CurrentAccount extends Account {
    private static final double OVERDRAFT_LIMIT = 10000.0;

    public CurrentAccount(String accountNumber, String holderName, double openingBalance) {
        super(accountNumber, holderName, openingBalance);
    }

    @Override
    public void withdraw(double amount) throws InvalidAmountException, InsufficientFundsException {
        if (amount <= 0) {
            throw new InvalidAmountException("Invalid amount: " + amount);
        }
        lock.lock();
        try {
            if (balance - amount < -OVERDRAFT_LIMIT) {
                throw new InsufficientFundsException(
                        "Overdraft limit of " + OVERDRAFT_LIMIT + " exceeded");
            }
            balance -= amount;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public double calculateInterest() {
        return 0.0; // current accounts earn no interest
    }
}
