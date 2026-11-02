package com.bank.model;

import com.bank.exception.InsufficientFundsException;
import com.bank.exception.InvalidAmountException;

public class SavingsAccount extends Account {
    private static final double MIN_BALANCE = 500.0;
    private static final double INTEREST_RATE = 0.04;

    public SavingsAccount(String accountNumber, String holderName, double openingBalance) {
        super(accountNumber, holderName, openingBalance);
    }

    @Override
    public void withdraw(double amount) throws InvalidAmountException, InsufficientFundsException {
        if (amount <= 0) {
            throw new InvalidAmountException("Invalid amount: " + amount);
        }
        lock.lock();
        try {
            if (balance - amount < MIN_BALANCE) {
                throw new InsufficientFundsException(
                        "Savings account must keep a minimum balance of " + MIN_BALANCE);
            }
            balance -= amount;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public double calculateInterest() {
        return getBalance() * INTEREST_RATE;
    }
}
