/* Copyright (C) 2013 Gabriel Giordano
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */
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

class ResultSetTranslator
{

	private Map<String, Integer> columnMapping;
	private ResultSet resultSet;

	ResultSetTranslator() {}
	
	void prepare(ResultSet resultSet, String sql) throws SQLException
	{
		this.resultSet = resultSet;
		columnMapping = SessionManager.getColumnMapping(resultSet, sql);
	}
	
	boolean hasColumn(String column)
	{
		return columnMapping.containsKey(column.toLowerCase());
	}
	
	private Object translatePrimaryType(Class<?> fieldType, int index) throws SQLException 
	{
		if(fieldType.isPrimitive())
		{
			if (fieldType.equals(Long.TYPE))
			{
				return resultSet.getLong(index);
			}

			if (fieldType.equals(Integer.TYPE))
			{
				return resultSet.getInt(index);
			}

			if (fieldType.equals(Double.TYPE))
			{
				return resultSet.getDouble(index);
			}

			if (fieldType.equals(Float.TYPE))
			{
				return resultSet.getFloat(index);
			}

			if (fieldType.equals(Short.TYPE))
			{
				return resultSet.getShort(index);
			}

			if (fieldType.equals(Byte.TYPE))
			{
				return resultSet.getByte(index);
			}
			
			if(fieldType.equals(Boolean.TYPE))
			{
				return resultSet.getBoolean(index);
			}
			
			throw new BlueprintException
			(
				"Primitive type " + fieldType + " not supported."
			);
		}
		
		if (fieldType.equals(String.class))
		{
			return resultSet.getString(index);
		}
		
		if(Number.class.isAssignableFrom(fieldType))
		{
			if (fieldType.equals(Long.class))
			{
				return resultSet.getLong(index);
			}

			if (fieldType.equals(Integer.class))
			{
				return resultSet.getInt(index);
			}

			if (fieldType.equals(Double.class))
			{
				return resultSet.getDouble(index);
			}

			if (fieldType.equals(Float.class))
			{
				return resultSet.getFloat(index);
			}

			if (fieldType.equals(Short.class))
			{
				return resultSet.getShort(index);
			}

			if (fieldType.equals(Byte.class))
			{
				return resultSet.getByte(index);
			}
			
			if (fieldType.equals(BigDecimal.class))
			{
				return resultSet.getBigDecimal(index);
			}
			
			throw new BlueprintException
			(
				"Number type " + fieldType + " not supported."
			);
		}
		
		return null;
	}
	
	private Object translateOtherType(Class<?> type, int index) throws SQLException
	{
		String packageName = type.getPackage().getName();
		
		if(packageName.equals("java.util"))
		{
			if (type.equals(java.util.Date.class))
			{
				return resultSet.getDate(index);
			}
		}
		
		if(packageName.equals("java.sql"))
		{
			if (type.equals(java.sql.Date.class))
			{
				return resultSet.getDate(index);
			}
			
			if (type.equals(java.sql.Blob.class))
			{
				return resultSet.getBlob(index);
			}

			if (type.equals(java.sql.Clob.class))
			{
				return resultSet.getClob(index);
			}

			if (type.equals(java.sql.NClob.class))
			{
				return resultSet.getNClob(index);
			}

			if (type.equals(java.sql.Ref.class))
			{
				return resultSet.getRef(index);
			}

			if (type.equals(java.sql.RowId.class))
			{
				return resultSet.getRowId(index);
			}

			if (type.equals(java.sql.SQLXML.class))
			{
				return resultSet.getSQLXML(index);
			}

			if (type.equals(java.sql.Time.class))
			{
				return resultSet.getTime(index);
			}

			if (type.equals(java.sql.Timestamp.class))
			{
				return resultSet.getTimestamp(index);
			}
		}
		
		if (type.equals(java.net.URL.class))
		{
			return resultSet.getURL(index);
		}
		
		return null;
	}
	
	EntityObjectAssociation tranlateToAssociation(ObjectScope entityObject, Object object)
			throws SQLException
	{
		
		Class<?> type = entityObject.identity.getType();
		int index = columnMapping.get(entityObject.label.toLowerCase());
		
		Object result = translatePrimaryType(type, index);
		
		if(result != null)
		{
			return new EntityObjectAssociation(object, result, entityObject);
		}
		
		throw new RuntimeException("invalid identity type '" + entityObject.identity.getType()
				+ "' at " + entityObject.getFieldName() + " field on '"
				+ entityObject.field.getType() + "'");
	}

	void translate(Variable variable, Object object) throws SQLException, 
						 IllegalArgumentException, IllegalAccessException
	{
		String label = variable.label.toLowerCase();
		
		if(!columnMapping.containsKey(label))
		{
			return;
		}
		
		int index = columnMapping.get(label);
		
		Class<?> type = variable.field.getType();

		Object result = translatePrimaryType(type, index);
		
		if(result != null)
		{
			variable.field.set(object, result);
			return;
		}
		
		result = translateOtherType(type, index);
		
		if(result != null)
		{
			variable.field.set(object, result);
			return;
		}
		
		if (type.isEnum() && EnumType.class.isAssignableFrom(type))
		{
			variable.field.set(object, translateEnumType(resultSet.getString(index), type));
			return;
		}

		if (type.equals(SetType.class))
		{
			variable.field.set(
					object, translateSetType(resultSet.getString(index),
							( (ColumnScope) variable ).genericType));
			return;
		}

	}

	@SuppressWarnings("unchecked")
	final <T> T translateGeneratedKeys(ResultSet generatedKeys, Class<T> type) throws SQLException
	{
		Object result = translatePrimaryType(type, 1);
		
		if(result != null)
		{
			return (T) result;
		}
		
		throw new BlueprintException(type + " type not supported for auto generated keys.");
	}
	
	@SuppressWarnings("unchecked") 
	private SetType<EnumType> translateSetType(String values, Type genericType)
	{
		SetType<EnumType> setType = new SetType<>((Class<EnumType>) genericType);
		setType.addByComma(values);

		return setType;
	}
	

	private EnumType translateEnumType(String value, Class<?> type)
	{
		for (Object constant : type.getEnumConstants())
		{
			if (value.equals(( (EnumType) constant ).getValue()))
			{
				return (EnumType) constant;
			}
		}

		return null;
	}
}
