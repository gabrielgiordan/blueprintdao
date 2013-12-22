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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;

import medina.blueprint.AbstractEntity.EntityListener;
import medina.blueprint.AbstractEntity.ObjectScope;
import medina.blueprint.dao.RestrictionsSettings;

class EntityRestrictions implements RestrictionsSettings {

  private Map<Class<?>, Collection<String>> restrictions;
  private Collection<Class<?>> resctrictedClasses;

  EntityRestrictions() {

    restrictions = new HashMap<>();
    resctrictedClasses = new HashSet<>();
  }

  @Override
  public boolean isClassRestricted(final Class<?> clazz) {
    return resctrictedClasses.contains(clazz);
  }

  @Override
  public boolean containsRestrictions(final Class<?> clazz) {
    return restrictions.containsKey(clazz);
  }

  protected boolean containsRestrictions(final Entity entity) {
    return containsRestrictions(entity.getEntityClass());
  }

  @Override
  public void restrictClass(final Class<?> clazz) {
    resctrictedClasses.add(clazz);
  }

  @Override
  public void restrictColumns(final Class<?> clazz, final String... columns) {

    final Collection<String> restrictedColumns = new LinkedHashSet<>();

    for (final String column : columns) {
      restrictedColumns.add(column);
    }

    restrictions.put(clazz, restrictedColumns);
  }

  @Override
  public void restrictFields(final Class<?> clazz, final String... fields) {

    final Entity entity = SessionManager.getEntity(clazz);

    final Collection<String> restrictedColumns = new LinkedHashSet<>();

    entity.nextSeveralVariables(new EntityListener() {

      @Override
      public void performAction(final EntityEvent event) {

        if (event.hasField()) {
          for (final String field : fields) {

            if (event.getField().equals(field)) {
              restrictedColumns.add(event.getLabel());
            }
          }
        }
      }
    });

    restrictions.put(clazz, restrictedColumns);
  }

  @Override
  public void removeClazzRestriction(final Class<?> clazz) {
    resctrictedClasses.remove(clazz);
  }

  @Override
  public void resetRestrictionsOf(final Class<?> clazz) {
    restrictions.remove(clazz);
  }

  @Override
  public void resetRestrictions() {
    restrictions = new HashMap<>();
  }

  @Override
  public void resetClassRestrictions() {
    resctrictedClasses = new HashSet<>();
  }

  @Override
  public void resetAll() {
    resetRestrictions();
    resetClassRestrictions();
  }

  String restrictColumnByObject(final Class<?> resquester, final Class<?> clazz) {
    final Entity entityRequester = SessionManager.getEntity(resquester);

    for (final ObjectScope entityObject : entityRequester.objects) {
      if (entityObject.field.getType().equals(clazz)) {
        restrictColumns(resquester, entityObject.label);
        return entityObject.label;
      }
    }

    return null;
  }

  Collection<String> getPermissions(final Entity entity) {
    return getPermissions(entity.getEntityClass());
  }

  Collection<String> getPermissions(final Class<?> clazz) {

    if (containsRestrictions(clazz)) {

      final Entity entity = SessionManager.getEntity(clazz);

      final Collection<String> restrictions = this.restrictions.get(clazz);
      final Collection<String> permissions = new LinkedHashSet<>();

      entity.nextSeveralVariables(new EntityListener() {

        @Override
        public void performAction(final EntityEvent event) {

          if (!restrictions.contains(event.getLabel())) {
            permissions.add(event.getLabel());
          }
        }
      });

      return permissions;
    }

    return null;
  }
}
