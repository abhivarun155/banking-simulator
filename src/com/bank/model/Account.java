package com.bank.model;

import com.bank.exception.InvalidAmountException;

import java.util.concurrent.locks.ReentrantLock;

public abstract class Account implements Transactable, Auditable {
    protected final String accountNumber;
    protected final String holderName;
    protected double balance;
    protected final ReentrantLock lock = new ReentrantLock();

    public Account(String accountNumber, String holderName, double openingBalance) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.balance = openingBalance;
    }

    @Override
    public void deposit(double amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit must be positive: " + amount);
        }
        lock.lock();
        try {
            balance += amount;
        } finally {
            lock.unlock();
        }
    }

    // each account type decides its own interest
    public abstract double calculateInterest();

    @Override
    public double getBalance() {
        lock.lock();
        try {
            return balance;
        } finally {
            lock.unlock();
        }
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getHolderName() {
        return holderName;
    }

    @Override
    public String toAuditRecord() {
        return getClass().getSimpleName() + "|" + accountNumber + "|" + holderName + "|" + getBalance();
    }
}
