package com.bank.service;

import com.bank.exception.AccountNotFoundException;
import com.bank.exception.DuplicateAccountException;
import com.bank.exception.InsufficientFundsException;
import com.bank.exception.InvalidAmountException;
import com.bank.model.Account;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BankService {
    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    // used when accounts are loaded from the file
    public void addAccount(Account account) {
        accounts.put(account.getAccountNumber(), account);
    }

    // used when the user opens a new account: refuses duplicate numbers
    public void openAccount(Account account) throws DuplicateAccountException {
        Account existing = accounts.putIfAbsent(account.getAccountNumber(), account);
        if (existing != null) {
            throw new DuplicateAccountException(
                    "Account number already exists: " + account.getAccountNumber());
        }
    }

    public Account removeAccount(String number) throws AccountNotFoundException {
        Account removed = accounts.remove(number);
        if (removed == null) {
            throw new AccountNotFoundException("No account found: " + number);
        }
        return removed;
    }

    public Account getAccount(String number) throws AccountNotFoundException {
        Account acc = accounts.get(number);
        if (acc == null) {
            throw new AccountNotFoundException("No account found: " + number);
        }
        return acc;
    }

    public List<Account> getAllAccounts() {
        List<Account> list = new ArrayList<>(accounts.values());
        list.sort(Comparator.comparing(Account::getAccountNumber));
        return list;
    }

    // Always lock the account with the smaller number first, so two opposite
    // transfers can never wait for each other (no deadlock).
    public void transfer(String from, String to, double amount)
            throws AccountNotFoundException, InvalidAmountException, InsufficientFundsException {
        Account a = getAccount(from);
        Account b = getAccount(to);
        Account first = a.getAccountNumber().compareTo(b.getAccountNumber()) < 0 ? a : b;
        Account second = (first == a) ? b : a;

        first.getLock().lock();
        try {
            second.getLock().lock();
            try {
                a.withdraw(amount);
                b.deposit(amount);
            } finally {
                second.getLock().unlock();
            }
        } finally {
            first.getLock().unlock();
        }
    }
}
