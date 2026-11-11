# Banking / ATM Simulator (Java)

A menu-driven console banking system. The user can open and close accounts, deposit, withdraw and transfer money, view accounts and history, and run a multithreading demo.

## Concepts used
- Inheritance and overriding: Account -> SavingsAccount, CurrentAccount
- Interfaces: Transactable, Auditable
- Collections: ConcurrentHashMap, ArrayList, ConcurrentLinkedQueue
- Multithreading: ExecutorService thread pool and ReentrantLock
- Exception handling: custom checked exceptions
- Java NIO: Path, Files.write, Files.readAllLines (accounts and transaction log)

## Menu
1. Open a new account
2. Close (remove) an account
3. Deposit money
4. Withdraw money
5. Transfer money
6. View all accounts
7. View account details and interest
8. View transaction history
9. Run multithreading demo
0. Exit

## How to run (Windows)
Needs JDK 17 or newer. In the project folder:

```
javac -d out src\com\bank\*.java src\com\bank\model\*.java src\com\bank\service\*.java src\com\bank\worker\*.java src\com\bank\exception\*.java
java -cp out com.bank.Main
```

Accounts are saved in data/accounts.csv and transactions in data/transactions.log.

## Screenshots
See the screenshots folder.

## Author
[abhi varun], [160125733155]