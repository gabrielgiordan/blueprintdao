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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import medina.blueprint.AbstractEntity.EntityListener;
import medina.blueprint.exception.BlueprintException;
import medina.blueprint.util.StatementBuilder;

public abstract class StatementTool<E> implements StatementBuilder<E> {

  private final Entity defaultEntity;
  private final StringBuilder builder;

  private boolean delete;
  private boolean select;
  private final Collection<String> selectColumns;
  private final List<String> insertColumns;
  private final Collection<String> updateColumns;
  private final Collection<String> whereColumns;

  private String[] insertColumnsArray;

  private boolean increment;
  private String sequence;

  StatementTool(final Entity entity) {
    defaultEntity = entity;

    builder = new StringBuilder();

    selectColumns = new LinkedHashSet<>();
    insertColumns = new ArrayList<>();
    updateColumns = new LinkedHashSet<>();
    whereColumns = new LinkedHashSet<>();
  }

  StatementTool(final Class<?> clazz) {
    this(SessionManager.getEntity(clazz));
  }

  public StatementTool() {
    defaultEntity = SessionManager.getEntity(EngineUtil.resolveGenericType(getClass()));

    builder = new StringBuilder();

    selectColumns = new LinkedHashSet<>();
    insertColumns = new ArrayList<>();
    updateColumns = new LinkedHashSet<>();
    whereColumns = new LinkedHashSet<>();
  }

  private void reset() {
    select = false;
    delete = false;

    selectColumns.clear();
    insertColumns.clear();
    updateColumns.clear();
    whereColumns.clear();

    builder.setLength(0);
    builder.trimToSize();
  }

  @Override
  public String[] getInsertColumns() {
    if (!increment) {
      throw new BlueprintException("The columns' array are available "
          + "only for auto increment insert generated statements.");
    }

    final String[] insertColumns = insertColumnsArray;
    insertColumnsArray = null;

    return insertColumns;
  }

  @Override
  public boolean isAutoIncrement() {
    return increment;
  }

  @Override
  public boolean hasSequence() {
    return sequence != null;
  }

  @Override
  public void useAutoIncrement(final boolean increment) {
    this.increment = increment;
  }

  @Override
  public void useAutoIncrement(final boolean increment, final String sequence) {
    this.increment = increment;
    this.sequence = sequence;
  }

  @Override
  public StatementBuilder<E> select() {
    select = true;
    return this;
  }

  @Override
  public StatementBuilder<E> select(final String... columns) {
    this.selectColumns.addAll(Arrays.asList(columns));
    return this;
  }

  @Override
  public StatementBuilder<E> where() {
    whereColumns.add(defaultEntity.getIdentity().getLabel());
    return this;
  }

  @Override
  public Object where(final E instance) {
    try {
      where();
      return defaultEntity.getIdentityValue(instance);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      throw new BlueprintException(e);
    }
  }

  @Override
  public StatementBuilder<E> where(final String... columns) {
    whereColumns.addAll(Arrays.asList(columns));
    return this;
  }

  @Override
  public StatementBuilder<E> insert() {
    insertColumns.addAll(defaultEntity.getAllLabels());
    return this;
  }

  @Override
  public Collection<Object> insert(final E instance) {
    final Collection<Object> values = new ArrayList<>();

    try {
      defaultEntity.nextSeveralVariables(new EntityListener() {

        @Override
        public void performAction(final EntityEvent event) {
          if (increment && event.isIdentity()) {
            return;
          } else if (event.getValue() == null) {
            return;
          } else if (Number.class.isAssignableFrom(event.getValue().getClass())) {
            if (((Number) event.getValue()).intValue() == 0) {
              return;
            }
          }

          insertColumns.add(event.getLabel());
          values.add(event.getValue());
        }

      }, instance);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      throw new BlueprintException(e);
    }

    return values;
  }

  @Override
  public StatementBuilder<E> insert(final String... columns) {
    insertColumns.addAll(Arrays.asList(columns));
    return this;
  }

  @Override
  public StatementBuilder<E> update() {
    updateColumns.addAll(defaultEntity.getAllLabels());
    return this;
  }

  @Override
  public Collection<Object> update(final E instance) {
    final Collection<Object> values = new ArrayList<>();

    try {
      defaultEntity.nextSeveralVariables(new EntityListener() {

        @Override
        public void performAction(final EntityEvent event) {
          if (!event.isIdentity()) {
            updateColumns.add(event.getLabel());
            values.add(event.getValue());
          }
        }

      }, instance);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      throw new BlueprintException(e);
    }

    return values;
  }

  @Override
  public StatementBuilder<E> update(final String... columns) {
    insertColumns.addAll(Arrays.asList(columns));
    return this;
  }

  @Override
  public StatementBuilder<E> delete() {
    delete = true;
    return this;
  }

  @Override
  public String prepare() {
    if (checkSelect()) {
      ;
    } else if (checkInsert()) {
      ;
    } else if (checkUpdate()) {
      ;
    } else if (checkDelete()) {
      ;
    }

    checkWhere();

    final String statement = builder.toString();

    reset();

    return statement;
  }

  private boolean checkSelect() {
    if (select) {
      builder.append("SELECT * FROM " + defaultEntity.getTable());
    } else if (!selectColumns.isEmpty()) {
      int index = 0;
      for (final String column : selectColumns) {
        if (index == 0) {
          builder.append("SELECT " + column);
        } else {
          builder.append(", " + column);
        }

        ++index;
      }

      builder.append(" FROM " + defaultEntity.getTable());
    } else {
      return false;
    }

    return true;
  }

  private boolean checkInsert() {
    if (!insertColumns.isEmpty()) {
      builder.append("INSERT INTO " + defaultEntity.getTable());

      int sequenceIndex = 0;

      if (increment && sequence == null) {
        if (insertColumns.contains(defaultEntity.getIdentity().getLabel())) {
          insertColumns.remove(defaultEntity.getIdentity().getLabel());
          insertColumnsArray = insertColumns.toArray(new String[insertColumns.size()]);
        } else {
          insertColumns.add(defaultEntity.getIdentity().getLabel());
          insertColumnsArray = insertColumns.toArray(new String[insertColumns.size()]);
          insertColumns.remove(defaultEntity.getIdentity().getLabel());
        }
      } else if (increment && sequence != null) {
        if (!insertColumns.contains(defaultEntity.getIdentity().getLabel())) {
          insertColumns.add(0, defaultEntity.getIdentity().getLabel());
        }

        sequenceIndex = insertColumns.indexOf(defaultEntity.getIdentity().getLabel());
        insertColumnsArray = insertColumns.toArray(new String[insertColumns.size()]);
      }

      int index = 0;
      for (final String column : insertColumns) {
        if (index == 0) {
          builder.append(" (" + column);
        } else {
          builder.append(", " + column);
        }

        ++index;
      }

      builder.append(") VALUES");

      for (index = 0; index < insertColumns.size(); ++index) {
        if (index == 0) {
          if (increment && sequence != null && index == sequenceIndex) {
            builder.append(" (" + sequence + ".NEXTVAL");
          } else {
            builder.append(" (?");
          }
        } else {
          if (increment && sequence != null && index == sequenceIndex) {
            builder.append(", " + sequence + ".NEXTVAL");
          } else {
            builder.append(", ?");
          }
        }
      }

      builder.append(")");
    } else {
      return false;
    }

    return true;
  }

  private boolean checkUpdate() {
    if (!updateColumns.isEmpty()) {
      builder.append("UPDATE " + defaultEntity.getTable() + " SET ");

      int index = 0;
      for (final String column : updateColumns) {
        if (index == 0) {
          builder.append(column + " = ?");
        } else {
          builder.append(", " + column + " = ?");
        }

        ++index;
      }
    } else {
      return false;
    }

    return true;
  }

  private boolean checkDelete() {
    if (delete) {
      builder.append("DELETE FROM " + defaultEntity.getTable());
    } else {
      return false;
    }

    return true;
  }

  private boolean checkWhere() {
    if (!whereColumns.isEmpty()) {
      int index = 0;
      for (final String column : whereColumns) {
        if (index == 0) {
          builder.append(" WHERE " + column + " = ?");
        } else {
          builder.append(" AND " + column + " = ?");
        }

        ++index;
      }
    } else {
      return false;
    }

    return true;
  }

}
