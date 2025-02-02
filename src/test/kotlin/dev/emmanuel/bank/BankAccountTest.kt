package dev.emmanuel.bank

import dev.emmanuel.bank.dev.emmanuel.bank.BankAccount
import dev.emmanuel.bank.dev.emmanuel.bank.BankAccountStatus
import dev.emmanuel.bank.dev.emmanuel.bank.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.util.concurrent.atomic.AtomicReference

class BankAccountTest {

    @Test
    fun `should initiate a bank account with status opened and zero balance and empty transactions`() {
        val openedBankAccount = BankAccount(number = "12345")
        assertThat(openedBankAccount.status()).isEqualTo(BankAccountStatus.OPENED)
        assertThat(openedBankAccount.balance()).isEqualTo(0.0)
        assertThat(openedBankAccount.transactions()).isEmpty()
    }

    @Test
    fun `should not accept negative deposit amount`() {
        val bankAccount = BankAccount(number = "12345")

        assertThatIllegalArgumentException()
            .isThrownBy { bankAccount.deposit(-12.00) }
            .withMessage("Deposit amount must be positive")

        assertThat(bankAccount.transactions()).isEmpty()
    }

    @Test
    fun `should increment balance and generate transaction history when receiving a deposit`() {
        val bankAccount = BankAccount(number = "12345")
        bankAccount.deposit(10.00)
        val transactions = bankAccount.transactions()

        assertThat(bankAccount.balance()).isEqualTo(10.00)

        assertThat(transactions).hasSize(1)
        assertThat(transactions).containsExactly(Transaction.DepositTransaction(10.00))
    }

    @Test
    fun `should not accept deposit when account is blocked`() {
        val bankAccount = BankAccount(number = "12345")
        bankAccount.block()

        assertThatIllegalArgumentException()
            .isThrownBy { bankAccount.deposit(15.00) }
            .withMessage("Account is blocked")

        assertThat(bankAccount.transactions()).isEmpty()
    }

    @Test
    fun `should not accept negative withdrawal amount`() {
        val bankAccount = BankAccount(number = "12345")

        assertThatIllegalArgumentException()
            .isThrownBy { bankAccount.withdraw(-30.00) }
            .withMessage("Withdrawal amount must be positive")

        assertThat(bankAccount.transactions()).isEmpty()
    }

    @Test
    fun `should not accept withdrawal when balance is insufficient`() {
        val bankAccount = BankAccount(number = "12345")

        assertThatIllegalArgumentException()
            .isThrownBy { bankAccount.withdraw(10.00) }
            .withMessage("Insufficient balance")

        assertThat(bankAccount.transactions()).isEmpty()
    }

    @Test
    fun `should not accept withdrawal when account is blocked`() {
        val bankAccount = BankAccount(number = "12345")
        bankAccount.deposit(30.00)
        bankAccount.block()

        assertThatIllegalArgumentException()
            .isThrownBy { bankAccount.withdraw(15.00) }
            .withMessage("Account is blocked")

        assertThat(bankAccount.transactions()).containsExactly(
            Transaction.DepositTransaction(30.00),
        )
    }

    @Test
    fun `should keep balance consistent in a concurrent scenario`(): Unit = runBlocking {
        val depositAmount = 10.00
        val withdrawalAmount = 5.00
        val expectedBalance = AtomicReference(0.0)
        val bankAccount = BankAccount(number = "12345")

        coroutineScope {
            repeat(10000) {
                launch(Dispatchers.Default) {
                    expectedBalance.updateAndGet { it + (depositAmount - withdrawalAmount) }
                    bankAccount.deposit(depositAmount)
                    bankAccount.withdraw(withdrawalAmount)
                }
            }
        }

        assertThat(bankAccount.balance()).isEqualTo(expectedBalance.get())
    }

    @ParameterizedTest
    @CsvSource(
        "10.00, 10.00, 0.00",
        "10.00, 3.00, 7.00"
    )
    fun `should decrease balance when receiving a withdrawal`(
        depositAmount: Double,
        withdrawalAmount: Double,
        expectedBalance: Double
    ) {
        val bankAccount = BankAccount(number = "12345")

        bankAccount.deposit(depositAmount)
        bankAccount.withdraw(withdrawalAmount)

        assertThat(bankAccount.balance()).isEqualTo(expectedBalance)

        val transactions = bankAccount.transactions()

        assertThat(transactions).hasSize(2)
        assertThat(transactions).containsExactly(
            Transaction.DepositTransaction(depositAmount),
            Transaction.WithdrawTransaction(withdrawalAmount)
        )
    }
}