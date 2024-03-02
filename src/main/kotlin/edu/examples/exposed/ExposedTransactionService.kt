package edu.examples.exposed

import io.quarkus.arc.properties.IfBuildProperty
import mu.KLogging
import org.eclipse.microprofile.config.ConfigProvider
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Slf4jSqlDebugLogger
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionManager
import javax.sql.DataSource
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

/**
 * This class operates Exposed transactions.
 * That is one place where DataSource should be injected.
 */
@IfBuildProperty(name = "app.persistence.implementation", stringValue = "Exposed")
@ApplicationScoped
class ExposedTransactionService(
  @Inject var ds: DataSource,
) {
  private val db = Database.connect(ds)

  fun getTx(): Transaction {
    val tx = db.transactionManager.newTransaction(db.transactionManager.defaultIsolationLevel)
    addLogger(tx)
    return tx
  }

  fun executeInTx(stub: Transaction.() -> Unit) =
    transaction(db) {
      addLogger(this)
      this.stub()
    }

  fun <T> queryInTx(stub: Transaction.() -> T): T =
    transaction(db) {
      addLogger(this)
      return@transaction this.stub()
    }

  fun addLogger(transaction: Transaction) {
    logger.debug { "transaction id: ${transaction.id}" }
    val sqlLogger =
      ConfigProvider.getConfig()
        .getOptionalValue("exposed.sql-logger", String::class.java)
        .orElse("none")
    when (sqlLogger) {
      "std" -> transaction.addLogger(StdOutSqlLogger)
      "slf4j" -> transaction.addLogger(Slf4jSqlDebugLogger)
    }
  }

  companion object : KLogging()
}
