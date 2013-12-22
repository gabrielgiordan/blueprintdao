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
package medina.blueprint.dao;

import java.util.List;

public interface DaoLayer<E> {

  void useAutoIncrement(boolean increment);

  void useAutoIncrement(String sequence);

  List<E> list();

  <N extends Number> E search(N identity);

  E search(String identity);

  void save(E instance);

  void update(E instance);

  void delete(E instance);

  <N extends Number> void delete(N identity);

  void delete(String identity);

}
