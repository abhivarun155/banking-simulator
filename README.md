# Banking / ATM Simulator (Java)

A console banking system written in Java. It shows:

- **Inheritance and overriding:** `Account` -> `SavingsAccount`, `CurrentAccount`
- **Interfaces:** `Transactable`, `Auditable`
- **Collections:** `ConcurrentHashMap`, `ArrayList`, `ConcurrentLinkedQueue`
- **Multithreading:** `ExecutorService` thread pool and `ReentrantLock`
- **Exception handling:** custom checked exceptions
- **Java NIO:** `Path`, `Files.write`, `Files.readAllLines`

## Project structure

```
src/com/bank/
  Main.java
  model/       Transactable, Auditable, Account, SavingsAccount, CurrentAccount
  service/     BankService, TransactionLogger
  worker/      TransactionTask
  exception/   InsufficientFundsException, InvalidAmountException, AccountNotFoundException
```

## How to run

Needs JDK 17 or newer.

Windows:
```
dir /s /b src\*.java > sources.txt
javac -d out @sources.txt
java -cp out com.bank.Main
```

Linux / Mac:
```
javac -d out $(find src -name "*.java")
java -cp out com.bank.Main
```

The transaction history is saved in `data/transactions.log`.

## Screenshots

See the `screenshots/` folder.

## Author

[chinnabathula abhi varun prasad], [160125733155]
