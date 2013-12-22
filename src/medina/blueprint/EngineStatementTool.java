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
package medina.blueprint;

import java.util.Collection;

/** @author Gabriel Giordano */
class EngineStatementTool {

  private StringBuilder builder;
  private final Entity entity;

  EngineStatementTool(final Class<?> entityClass) {
    this(SessionManager.getEntity(entityClass));
  }

  EngineStatementTool(final Entity entity) {
    this.entity = entity;
    builder = new StringBuilder();
  }

  EngineStatementTool select() {
    builder.append("SELECT * FROM " + entity.table);
    return this;
  }

  EngineStatementTool select(final String column) {
    builder.append("SELECT " + column + " FROM " + entity.table);
    return this;
  }

  EngineStatementTool select(final Collection<String> columns) {
    int index = 0;
    for (final String column : columns) {

      if (index == 0) {
        builder.append("SELECT " + column);
      } else {
        builder.append(", " + column);
      }

      ++index;
    }

    builder.append(" FROM " + entity.table);
    return this;
  }

  EngineStatementTool where(final String column) {
    builder.append(" WHERE " + column + " = ?");
    return this;
  }

  EngineStatementTool where(final Collection<String> columns) {
    int index = 0;
    for (final String column : columns) {

      if (index == 0) {
        builder.append(" WHERE " + column + " = ?");
      } else {
        builder.append(" AND " + column + " = ?");
      }

      ++index;
    }

    return this;
  }

  EngineStatementTool insert(final Collection<String> columns) {
    builder.append("INSERT INTO " + entity.table);

    int index = 0;
    for (final String column : columns) {

      if (index == 0) {
        builder.append(" (" + column);
      } else {
        builder.append(", " + column);
      }

      ++index;
    }

    builder.append(") VALUES");

    for (index = 0; index < columns.size(); ++index) {

      if (index == 0) {
        builder.append(" (?");
      } else {
        builder.append(", ?");
      }
    }

    builder.append(")");

    return this;
  }

  EngineStatementTool update(final Collection<String> columns) {

    builder.append("UPDATE " + entity.table + " SET ");

    int index = 0;
    for (final String column : columns) {

      if (index == 0) {
        builder.append(column + " = ?");
      } else {
        builder.append(", " + column + " = ?");
      }

      ++index;
    }

    return this;
  }

  EngineStatementTool delete() {
    builder.append("DELETE FROM " + entity.table);
    return this;
  }

  String end() {
    final String result = builder.toString();
    builder = new StringBuilder();

    return result;
  }
}
