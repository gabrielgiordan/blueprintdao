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

public interface RestrictionsSettings {

  boolean isClassRestricted(Class<?> clazz);

  boolean containsRestrictions(Class<?> clazz);

  void restrictClass(Class<?> clazz);

  void restrictColumns(Class<?> clazz, String... columns);

  void restrictFields(Class<?> clazz, final String... fields);

  void removeClazzRestriction(Class<?> clazz);

  void resetRestrictionsOf(Class<?> clazz);

  void resetRestrictions();

  void resetClassRestrictions();

  void resetAll();

}
