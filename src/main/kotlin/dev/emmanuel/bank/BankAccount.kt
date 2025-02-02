package dev.emmanuel.bank.dev.emmanuel.bank

import java.util.concurrent.locks.ReentrantReadWriteLock

enum class BankAccountStatus {
    OPENED,
    BLOCKED
}

class BankAccount(val number: String) {

    private var balance: Double = 0.0
    private var status: BankAccountStatus = BankAccountStatus.OPENED
    private val transactions = mutableListOf<Transaction>()
    private val lock = ReentrantReadWriteLock()

    fun status() = status

    fun block() {
        status = BankAccountStatus.BLOCKED
    }

    fun balance() = withReadLock {
        balance
    }

    fun transactions() = transactions.toList()

    fun deposit(amount: Double) = withWriteLock {
        checkAccountStatus()
        require(amount > 0) { "Deposit amount must be positive" }

        balance += amount
        transactions.add(Transaction.DepositTransaction(amount))
    }

    fun withdraw(amount: Double) = withWriteLock {
        checkAccountStatus()
        require(amount > 0) { "Withdrawal amount must be positive" }
        require(amount <= balance) { "Insufficient balance" }

        balance -= amount
        transactions.add(Transaction.WithdrawTransaction(amount))
    }

    private fun checkAccountStatus() {
        require(status == BankAccountStatus.OPENED) { "Account is blocked" }
    }

    private fun withWriteLock(writeOperation: () -> Unit) {
        try {
            lock.writeLock().lock()
            writeOperation()
        } finally {
            lock.writeLock().unlock()
        }
    }

    private fun withReadLock(readOperation: () -> Double): Double {
        try {
            lock.readLock().lock()
            return readOperation()
        } finally {
            lock.readLock().unlock()
        }
    }
}