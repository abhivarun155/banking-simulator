package com.bank.model;

import com.bank.exception.InsufficientFundsException;
import com.bank.exception.InvalidAmountException;

public interface Transactable {
    void deposit(double amount) throws InvalidAmountException;

    void withdraw(double amount) throws InvalidAmountException, InsufficientFundsException;

    double getBalance();
}
