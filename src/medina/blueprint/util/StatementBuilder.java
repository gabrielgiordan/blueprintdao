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
package medina.blueprint.util;

import java.util.Collection;

public interface StatementBuilder<E> {

  boolean isAutoIncrement();

  boolean hasSequence();

  void useAutoIncrement(boolean increment);

  void useAutoIncrement(boolean increment, String sequence);

  StatementBuilder<E> select();

  StatementBuilder<E> select(String... columns);

  StatementBuilder<E> where();

  Object where(E instance);

  StatementBuilder<E> where(String... columns);

  StatementBuilder<E> insert();

  Collection<Object> insert(E instance);

  StatementBuilder<E> insert(String... columns);

  String[] getInsertColumns();

  StatementBuilder<E> update();

  Collection<Object> update(E instance);

  StatementBuilder<E> update(String... columns);

  StatementBuilder<E> delete();

  String prepare();

}
