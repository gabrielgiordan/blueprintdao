/*
 * Copyright (C) 2013 Gabriel Giordano
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package medina.blueprint.session;

import java.sql.Connection;

import medina.blueprint.exception.BlueprintException;
import medina.blueprint.exception.TransactionException;

public interface Transaction {

  public static int ISOLATION_READ_COMMITTED = Connection.TRANSACTION_READ_COMMITTED;
  public static int ISOLATION_READ_UNCOMMITTED = Connection.TRANSACTION_READ_UNCOMMITTED;
  public static int ISOLATION_REPEATABLE_READ = Connection.TRANSACTION_REPEATABLE_READ;
  public static int ISOLATION_SERIALIZABLE = Connection.TRANSACTION_SERIALIZABLE;

  void setIsolationLevel(int level);

  void begin() throws TransactionException;

  void addSavePoint(int index) throws TransactionException;

  void releaseSavePoint(int index) throws TransactionException;

  void rollback() throws BlueprintException;

  void rollback(int savepointIndex) throws BlueprintException;

  void end() throws TransactionException;

  boolean wasRolledBack();

  boolean wasCommitted();

}
