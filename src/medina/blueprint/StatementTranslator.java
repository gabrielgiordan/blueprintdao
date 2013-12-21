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

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

import medina.blueprint.exception.BlueprintException;
import medina.blueprint.type.EnumType;
import medina.blueprint.type.SetType;

class StatementTranslator
{
	private PreparedStatement statement;
	
	StatementTranslator() {}

	void setStatement(PreparedStatement statement)
	{
		this.statement = statement;
	}
	
	void translate(Collection<Object> values) throws SQLException 
	{
		int index = 1;
		
		for (Object value : values)
		{
			if(value instanceof String)
			{
				statement.setString(index++, (String) value); continue;
			}
			
			if(value instanceof Number)
			{
				if (value instanceof Long)
				{
					statement.setLong(index++, (long) value); continue;
				}

				if (value instanceof Integer)
				{
					statement.setInt(index++, (int) value); continue;
				}

				if (value instanceof Double)
				{
					statement.setDouble(index++, (double) value); continue;
				}

				if (value instanceof Float)
				{
					statement.setFloat(index++, (float) value); continue;
				}

				if (value instanceof Short)
				{
					statement.setShort(index++, (short) value); continue;
				}

				if (value instanceof Byte)
				{
					statement.setByte(index++, (byte) value); continue;
				}
				
				if (value instanceof BigDecimal)
				{
					statement.setBigDecimal(index++, (BigDecimal) value); continue;
				}
				
				throw new BlueprintException
				(
					"Number type " + value.getClass().getSimpleName() + " is not supported."
				);
			}
			
			if(EnumType.class.isAssignableFrom(value.getClass())) 
			{
				statement.setString(index++, translateEnumType(value)); continue;
			}
			
			if(value.getClass().equals(SetType.class)) 
			{
				statement.setString(index++, translateSetType(value)); continue;
			}
			
			statement.setObject(index++, value);
		}
	}
	
	private String translateEnumType(Object enumType) 
	{
		return ((EnumType) enumType).getValue();
	}
	
	private String translateSetType(Object setType) 
	{
		@SuppressWarnings("unchecked")
		String value = ((SetType<EnumType>) setType).getAllValues();
		return value;
	}
}
