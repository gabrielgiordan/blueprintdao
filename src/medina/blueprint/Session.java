package medina.blueprint;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.HashMap;
import java.util.Map;

import medina.blueprint.exception.BlueprintException;
import medina.blueprint.exception.TransactionException;
import medina.blueprint.session.Transaction;

public final class Session extends SessionManager {

  private final Transaction transaction;

  // Constructors____________________________________________________________________ //

  public Session(final Connection connection) {
    super(connection);
    this.transaction = new SessionTransaction();
    begin();
  }

  // Public Methods__________________________________________________________________ //

  public Transaction transaction() {
    return transaction;
  }

  public void begin() throws BlueprintException {
    try {
      connection.setReadOnly(true);
    } catch (final SQLException e) {
      throw new BlueprintException(e);
    }
  }

  public void end() throws BlueprintException {
    try {
      for (final PreparedStatement statement : statementMap.values()) {
        statement.close();
      }
    } catch (final SQLException e) {
      throw new BlueprintException(e);
    } finally {
      try {
        connection.close();
      } catch (final SQLException e) {
        throw new BlueprintException(e);
      } finally {
        statementMap.clear();
      }
    }
  }

  public Connection connection() {
    return connection;
  }

  private final class SessionTransaction implements Transaction {

    private boolean rolledBack;
    private boolean committed;
    private int isolation;

    private final Map<Integer, Savepoint> savepoints;

    // Constructors____________________________________________________________________ //

    private SessionTransaction() {
      rolledBack = false;
      committed = false;
      isolation = ISOLATION_SERIALIZABLE;

      savepoints = new HashMap<>();
    }

    // Transaction - Public Methods____________________________________________________ //

    @Override
    public void setIsolationLevel(final int level) {
      this.isolation = level;
    }

    @Override
    public void begin() throws TransactionException {
      try {
        System.err.println("	Beginning transaction.");

        connection.setTransactionIsolation(isolation);
        connection.setReadOnly(false);
        connection.setAutoCommit(false);

        rolledBack = false;
        committed = false;

      } catch (final SQLException e) {
        throw new TransactionException(e);
      }
    }

    @Override
    public void addSavePoint(final int index) throws TransactionException {
      try {
        savepoints.put(index, connection.setSavepoint(String.valueOf(index)));
      } catch (final SQLException e) {
        throw new TransactionException(e);
      }
    }

    @Override
    public void releaseSavePoint(final int index) throws TransactionException {
      try {
        connection.releaseSavepoint(savepoints.get(index));

        savepoints.remove(index);
      } catch (final SQLException e) {
        throw new TransactionException(e);
      }
    }

    @Override
    public void rollback() throws BlueprintException {
      try {
        connection.rollback();
        rolledBack = true;

        System.err.println("	Transaction was rolled back.");
      } catch (final SQLException e) {
        throw new BlueprintException(e);
      }
    }

    @Override
    public void rollback(final int savepointIndex) throws BlueprintException {
      try {
        connection.rollback(savepoints.get(savepointIndex));

        rolledBack = true;

        System.err.println("	Transaction was rolled back at" + " savepoint of index "
            + savepointIndex + ".");
      } catch (final SQLException e) {
        throw new BlueprintException(e);
      }
    }

    @Override
    public void end() throws TransactionException {
      try {
        if (wasRolledBack()) {
          reset();
          return;
        }

        connection.commit();
        committed = true;

        reset();

        System.err.println("	Transaction was successfully committed.");
      } catch (final SQLException e) {
        throw new TransactionException(e);
      }
    }

    @Override
    public boolean wasRolledBack() {
      return rolledBack;
    }

    @Override
    public boolean wasCommitted() {
      return committed;
    }

    // Private Methods_________________________________________________________________ //

    private void reset() throws SQLException {
      savepoints.clear();
      connection.setAutoCommit(true);
      connection.setReadOnly(true);
    }
  }
}
