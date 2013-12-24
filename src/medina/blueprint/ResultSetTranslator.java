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

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import medina.blueprint.AbstractEntity.ColumnScope;
import medina.blueprint.AbstractEntity.ObjectScope;
import medina.blueprint.AbstractEntity.Variable;
import medina.blueprint.exception.BlueprintException;
import medina.blueprint.type.EnumType;
import medina.blueprint.type.SetType;

class ResultSetTranslator {

  private Map<String, Integer> columnMapping;
  private ResultSet resultSet;

  ResultSetTranslator() {}

  void prepare(final ResultSet resultSet, final String sql) throws SQLException {
    this.resultSet = resultSet;
    columnMapping = SessionManager.getColumnMapping(resultSet, sql);
  }

  boolean hasColumn(final String column) {
    return columnMapping.containsKey(column.toLowerCase());
  }

  private Object translatePrimaryType(final Class<?> fieldType, final int index)
      throws SQLException {
    
    if (fieldType.isPrimitive()) {
      
      if (fieldType == long.class) {
        return resultSet.getLong(index);
      }

      if (fieldType == int.class) {
        return resultSet.getInt(index);
      }

      if (fieldType == double.class) {
        return resultSet.getDouble(index);
      }

      if (fieldType == float.class) {
        return resultSet.getFloat(index);
      }

      if (fieldType == short.class) {
        return resultSet.getShort(index);
      }

      if (fieldType == byte.class) {
        return resultSet.getByte(index);
      }

      if (fieldType == boolean.class) {
        return resultSet.getBoolean(index);
      }

      throw new BlueprintException("Primitive type " + fieldType + " not supported.");
    }

    if (fieldType == String.class) {
      return resultSet.getString(index);
    }

    if (Number.class.isAssignableFrom(fieldType)) {
      
      if (fieldType == Long.class) {
        return resultSet.getLong(index);
      }

      if (fieldType == Integer.class) {
        return resultSet.getInt(index);
      }

      if (fieldType == Double.class) {
        return resultSet.getDouble(index);
      }

      if (fieldType == Float.class) {
        return resultSet.getFloat(index);
      }

      if (fieldType == Short.class) {
        return resultSet.getShort(index);
      }

      if (fieldType == Byte.class) {
        return resultSet.getByte(index);
      }

      if (fieldType == BigDecimal.class) {
        return resultSet.getBigDecimal(index);
      }

      throw new BlueprintException("Number type " + fieldType + " not supported.");
    }

    return null;
  }

  private Object translateOtherType(final Class<?> type, final int index) throws SQLException {
    
    final String packageName = type.getPackage().getName();

    if (packageName.equals("java.util")) {
      
      if (type == java.util.Date.class) {
        return resultSet.getDate(index);
      }
    }

    if (packageName.equals("java.sql")) {
      
      if (type == java.sql.Date.class) {
        return resultSet.getDate(index);
      }

      if (type == java.sql.Blob.class) {
        return resultSet.getBlob(index);
      }

      if (type == java.sql.Clob.class) {
        return resultSet.getClob(index);
      }

      if (type == java.sql.NClob.class) {
        return resultSet.getNClob(index);
      }

      if (type == java.sql.Ref.class) {
        return resultSet.getRef(index);
      }

      if (type == java.sql.RowId.class) {
        return resultSet.getRowId(index);
      }

      if (type == java.sql.SQLXML.class) {
        return resultSet.getSQLXML(index);
      }

      if (type == java.sql.Time.class) {
        return resultSet.getTime(index);
      }

      if (type == java.sql.Timestamp.class) {
        return resultSet.getTimestamp(index);
      }
    }

    if (type == java.net.URL.class) {
      return resultSet.getURL(index);
    }

    return null;
  }

  EntityObjectAssociation tranlateToAssociation(final ObjectScope entityObject, final Object object)
      throws SQLException {

    final Class<?> type = entityObject.identity.getType();
    final int index = columnMapping.get(entityObject.label.toLowerCase());

    final Object result = translatePrimaryType(type, index);

    if (result != null) {
      return new EntityObjectAssociation(object, result, entityObject);
    }

    throw new RuntimeException("invalid identity type '" + entityObject.identity.getType()
        + "' at " + entityObject.getFieldName() + " field on '" + entityObject.field.getType()
        + "'");
  }

  void translate(final Variable variable, final Object object) throws SQLException,
      IllegalArgumentException, IllegalAccessException {
    final String label = variable.label.toLowerCase();

    if (!columnMapping.containsKey(label)) {
      return;
    }

    final int index = columnMapping.get(label);

    final Class<?> type = variable.field.getType();

    Object result = translatePrimaryType(type, index);

    if (result != null) {
      variable.field.set(object, result);
      return;
    }

    result = translateOtherType(type, index);

    if (result != null) {
      variable.field.set(object, result);
      return;
    }

    if (type.isEnum() && EnumType.class.isAssignableFrom(type)) {
      variable.field.set(object, translateEnumType(resultSet.getString(index), type));
      return;
    }

    if (type == SetType.class) {
      variable.field.set(object,
          translateSetType(resultSet.getString(index), ((ColumnScope) variable).genericType));
      return;
    }

  }

  @SuppressWarnings("unchecked")
  final <T> T translateGeneratedKeys(final ResultSet generatedKeys, final Class<T> type)
      throws SQLException {
    final Object result = translatePrimaryType(type, 1);

    if (result != null) {
      return (T) result;
    }

    throw new BlueprintException(type + " type not supported for auto generated keys.");
  }

  @SuppressWarnings("unchecked")
  private SetType<EnumType> translateSetType(final String values, final Type genericType) {
    final SetType<EnumType> setType = new SetType<>((Class<EnumType>) genericType);
    setType.addByComma(values);

    return setType;
  }


  private EnumType translateEnumType(final String value, final Class<?> type) {
    for (final Object constant : type.getEnumConstants()) {
      if (value.equals(((EnumType) constant).getValue())) {
        return (EnumType) constant;
      }
    }

    return null;
  }
}
