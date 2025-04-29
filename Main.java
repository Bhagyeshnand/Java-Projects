import java.io.*;
import java.util.*;

abstract class BankAccount {
    protected int accountNumber;
    protected String accountHolderName;
    protected double balance;

    public BankAccount(int accNum, String accHolderName, double bal) {
        this.accountNumber = accNum;
        this.accountHolderName = accHolderName;
        this.balance = bal;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            System.out.println("Deposit successful. New balance: " + balance);
        } else {
            System.out.println("Invalid deposit amount.");
        }
    }

    public abstract void withdraw(double amount);

    public void displayAccountDetails() {
        System.out.printf("Account Number: %d%n", accountNumber);
        System.out.printf("Account Holder: %s%n", accountHolderName);
        System.out.printf("Balance: $%.2f%n", balance);
    }

    public abstract String getAccountType();

    public abstract void save(PrintWriter outFile);
}

class SavingsAccount extends BankAccount {
    private double interestRate;

    public SavingsAccount(int accNum, String accHolderName, double bal, double intRate) {
        super(accNum, accHolderName, bal);
        this.interestRate = intRate;
    }

    @Override
    public void withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            System.out.println("Withdrawal successful. New balance: " + balance);
        } else {
            System.out.println("Invalid withdrawal amount or insufficient balance.");
        }
    }

    @Override
    public void displayAccountDetails() {
        super.displayAccountDetails();
        System.out.println("Account Type: Savings");
        System.out.println("Interest Rate: " + interestRate + "%");
    }

    @Override
    public String getAccountType() {
        return "Savings";
    }

    @Override
    public void save(PrintWriter outFile) {
        outFile.println("Savings " + accountNumber + " " + accountHolderName + " " + balance + " " + interestRate);
    }
}

class CurrentAccount extends BankAccount {
    private double overdraftLimit;

    public CurrentAccount(int accNum, String accHolderName, double bal, double odLimit) {
        super(accNum, accHolderName, bal);
        this.overdraftLimit = odLimit;
    }

    @Override
    public void withdraw(double amount) {
        if (amount > 0 && (balance + overdraftLimit) >= amount) {
            balance -= amount;
            System.out.println("Withdrawal successful. New balance: " + balance);
        } else {
            System.out.println("Invalid withdrawal amount or overdraft limit exceeded.");
        }
    }

    @Override
    public void displayAccountDetails() {
        super.displayAccountDetails();
        System.out.println("Account Type: Current");
        System.out.printf("Overdraft Limit: $%.2f%n", overdraftLimit);
    }

    @Override
    public String getAccountType() {
        return "Current";
    }

    @Override
    public void save(PrintWriter outFile) {
        outFile.println("Current " + accountNumber + " " + accountHolderName + " " + balance + " " + overdraftLimit);
    }
}

class BankingSystem {
    private List<BankAccount> accounts = new ArrayList<>();
    private int nextAccountNumber;

    public BankingSystem() {
        loadAccounts();
    }

    private void loadAccounts() {
        File file = new File("accounts.txt");
        if (!file.exists()) {
            nextAccountNumber = 1;
            return;
        }

        try (Scanner inFile = new Scanner(file)) {
            while (inFile.hasNext()) {
                String type = inFile.next();
                int accNum = inFile.nextInt();
                String accHolderName = inFile.next();
                double bal = inFile.nextDouble();
                double extra = inFile.nextDouble();

                BankAccount account;
                if (type.equals("Savings")) {
                    account = new SavingsAccount(accNum, accHolderName, bal, extra);
                } else {
                    account = new CurrentAccount(accNum, accHolderName, bal, extra);
                }
                accounts.add(account);
            }
        } catch (IOException e) {
            System.out.println("Error loading accounts: " + e.getMessage());
        }

        nextAccountNumber = accounts.isEmpty() ? 1 : accounts.get(accounts.size() - 1).getAccountNumber() + 1;
    }

    private void saveAccounts() {
        try (PrintWriter outFile = new PrintWriter("accounts.txt")) {
            for (BankAccount account : accounts) {
                account.save(outFile);
            }
        } catch (IOException e) {
            System.out.println("Error saving accounts: " + e.getMessage());
        }
    }

    public void createSavingsAccount(String accountHolderName, double initialDeposit, double interestRate) {
        BankAccount newAccount = new SavingsAccount(nextAccountNumber++, accountHolderName, initialDeposit, interestRate);
        accounts.add(newAccount);
        System.out.println("Savings account created successfully. Account Number: " + newAccount.getAccountNumber());
        saveAccounts();
    }

    public void createCurrentAccount(String accountHolderName, double initialDeposit, double overdraftLimit) {
        BankAccount newAccount = new CurrentAccount(nextAccountNumber++, accountHolderName, initialDeposit, overdraftLimit);
        accounts.add(newAccount);
        System.out.println("Current account created successfully. Account Number: " + newAccount.getAccountNumber());
        saveAccounts();
    }

    public void deposit(int accountNumber, double amount) {
        for (BankAccount account : accounts) {
            if (account.getAccountNumber() == accountNumber) {
                account.deposit(amount);
                saveAccounts();
                return;
            }
        }
        System.out.println("Account not found.");
    }

    public void withdraw(int accountNumber, double amount) {
        for (BankAccount account : accounts) {
            if (account.getAccountNumber() == accountNumber) {
                account.withdraw(amount);
                saveAccounts();
                return;
            }
        }
        System.out.println("Account not found.");
    }

    public void displayAccountDetails(int accountNumber) {
        for (BankAccount account : accounts) {
            if (account.getAccountNumber() == accountNumber) {
                account.displayAccountDetails();
                return;
            }
        }
        System.out.println("Account not found.");
    }
}

public class Main {
    public static void main(String[] args) {
        BankingSystem bank = new BankingSystem();
        Scanner scanner = new Scanner(System.in);
        int choice;
        String name;
        int accountNumber;
        double amount;
        double interestRate;
        double overdraftLimit;

        do {
            System.out.println("\nBanking System Menu");
            System.out.println("1. Create Savings Account");
            System.out.println("2. Create Current Account");
            System.out.println("3. Deposit");
            System.out.println("4. Withdraw");
            System.out.println("5. Display Account Details");
            System.out.println("6. Exit");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();

            scanner.nextLine(); // Clear buffer

            switch (choice) {
                case 1:
                    System.out.print("Enter account holder name: ");
                    name = scanner.nextLine();
                    System.out.print("Enter initial deposit amount: ");
                    amount = scanner.nextDouble();
                    System.out.print("Enter interest rate: ");
                    interestRate = scanner.nextDouble();
                    bank.createSavingsAccount(name, amount, interestRate);
                    break;

                case 2:
                    System.out.print("Enter account holder name: ");
                    name = scanner.nextLine();
                    System.out.print("Enter initial deposit amount: ");
                    amount = scanner.nextDouble();
                    System.out.print("Enter overdraft limit: ");
                    overdraftLimit = scanner.nextDouble();
                    bank.createCurrentAccount(name, amount, overdraftLimit);
                    break;

                case 3:
                    System.out.print("Enter account number: ");
                    accountNumber = scanner.nextInt();
                    System.out.print("Enter amount to deposit: ");
                    amount = scanner.nextDouble();
                    bank.deposit(accountNumber, amount);
                    break;

                case 4:
                    System.out.print("Enter account number: ");
                    accountNumber = scanner.nextInt();
                    System.out.print("Enter amount to withdraw: ");
                    amount = scanner.nextDouble();
                    bank.withdraw(accountNumber, amount);
                    break;

                case 5:
                    System.out.print("Enter account number: ");
                    accountNumber = scanner.nextInt();
                    bank.displayAccountDetails(accountNumber);
                    break;

                case 6:
                    System.out.println("Exiting...");
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }

        } while (choice != 6);

        scanner.close();
    }
}
