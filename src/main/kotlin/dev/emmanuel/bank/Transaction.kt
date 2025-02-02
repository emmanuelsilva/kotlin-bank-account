package dev.emmanuel.bank.dev.emmanuel.bank

import java.time.Instant

sealed class Transaction(amount: Double, timestamp: Instant) {
    data class DepositTransaction(val amount: Double) : Transaction(amount, Instant.now())
    data class WithdrawTransaction(val amount: Double) : Transaction(amount, Instant.now())
}