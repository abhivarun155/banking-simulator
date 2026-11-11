package com.bank.service;

import com.bank.model.Account;
import com.bank.model.CurrentAccount;
import com.bank.model.SavingsAccount;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

// Saves and loads the accounts so they are still there the next time the program runs.
public class AccountStorage {
    private final Path file = Paths.get("data", "accounts.csv");

    public AccountStorage() throws IOException {
        Files.createDirectories(file.getParent());
    }

    public void save(List<Account> accounts) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Account a : accounts) {
            String type = (a instanceof SavingsAccount) ? "SAVINGS" : "CURRENT";
            lines.add(type + "," + a.getAccountNumber() + "," + a.getHolderName() + "," + a.getBalance());
        }
        Files.write(file, lines,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public List<Account> load() throws IOException {
        List<Account> list = new ArrayList<>();
        if (!Files.exists(file)) {
            return list;
        }
        for (String line : Files.readAllLines(file)) {
            String[] p = line.split(",");
            if (p.length != 4) {
                continue; // skip broken lines
            }
            try {
                double balance = Double.parseDouble(p[3]);
                if (p[0].equals("SAVINGS")) {
                    list.add(new SavingsAccount(p[1], p[2], balance));
                } else {
                    list.add(new CurrentAccount(p[1], p[2], balance));
                }
            } catch (NumberFormatException e) {
                System.err.println("Skipping bad line in accounts file: " + line);
            }
        }
        return list;
    }
}
