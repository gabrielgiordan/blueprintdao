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
package medina.blueprint.util;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

import medina.blueprint.AbstractEntity.EntityListener;
import medina.blueprint.Entity;
import medina.blueprint.Session;
import medina.blueprint.exception.BlueprintException;

public abstract class StatementWriter<E> implements StatementWriterInterface<E>
{
	//TODO
	
	protected enum ENTITY { ID, FOREIGNERS, COLUMNS};
	protected enum LIKE { PREFIX_MATCH, GLOBAL_MATCH, SUFFIX_MATCH };

	private final static byte SELECT = 1;
	private final static byte INSERT = 2;
	private final static byte UPDATE = 3;
	private final static byte DELETE = 4;
	
	private byte type = 0;
	
	private final Entity entity;
	private final Collection<Object> values;
	private final Collection<String> insertColumns;
	private final StringBuilder builder;
	
	//_________________________________________________________________________________ //
	
	
	public StatementWriter()
	{
		values = new ArrayList<>();
		insertColumns = new LinkedHashSet<>();
		builder = new StringBuilder();
		
		entity = Session.getEntity
		(
			(Class<?>) ((ParameterizedType) getClass().getGenericSuperclass()
					
		).getActualTypeArguments()[0]);
		
	}
	
	// Getters_________________________________________________________________________ //
	
	@Override
	public String getGeneratedStatement()
	{	
		return builder.toString();
	}

	@Override
	public Collection<Object> getGeneratedValues()
	{
		return values;
	}
	
	@Override
	public String[] getGeneratedInsertColumns()
	{
		return insertColumns.toArray(new String[insertColumns.size()]);
	}

	// Behavioral Methods______________________________________________________________ //
	
	@Override
	public StatementWriterInterface<E> SET_CONDITION(boolean condition)
	{
		return this;
	}

	@Override
	public StatementWriterInterface<E> SET_VALUE(Object value)
	{
		values.add(value);
		return this;
	}

	@Override
	public StatementWriterInterface<E> SET_VALUES(Object... values)
	{
		this.values.addAll(Arrays.asList(values));
		return this;
	}

	@Override
	public StatementWriterInterface<E> SET_VALUES_FROM(E instance)
	{
		try
		{
			entity.nextSeveralVariables
			(
				new EntityListener() {
				
				@Override
				public void performAction(EntityEvent event)
				{
					values.add(event.getValue());
				}
				
			}, instance
			);
		}
		catch (IllegalArgumentException | IllegalAccessException e)
		{
			throw new BlueprintException(e);
		}
		
		return this;
	}

	@Override
	public StatementWriterInterface<E> RESET()
	{
		builder.setLength(0);
		builder.trimToSize();
		return this;
	}

	// Basic SQL_______________________________________________________________________ //
	
	@Override
	public StatementWriterInterface<E> SELECT()
	{
		type = SELECT;
		builder.append("SELECT " + entity.getTable() + ".*");
		
		return this;
	}

	@Override
	public StatementWriterInterface<E> SELECT(String sql)
	{
		type = SELECT;
		builder.append("SELECT " + sql);
		
		return this;
	}

	@Override
	public StatementWriterInterface<E> SELECT_DISTINCT(String sql)
	{
		type = SELECT;
		builder.append("SELECT DISTINCT" + sql);
		
		return this;
	}

	@Override
	public StatementWriterInterface<E> FROM()
	{
		builder.append(" FROM " + entity.getTable());
		return this;
	}
	
	//_________________________________________________________________________________ //

	@Override
	public StatementWriterInterface<E> UPDATE()
	{
		builder.append("UPDATE " + entity.getTable());
		return this;
	}

	@Override
	public StatementWriterInterface<E> UPDATE(String sql)
	{
		return this;
	}

	@Override
	public StatementWriterInterface<E> UPDATE(E instance)
	{
		return this;
	}

	@Override
	public StatementWriterInterface<E> SET()
	{
		return this;
	}

	@Override
	public StatementWriterInterface<E> SET(String sql)
	{
		return this;
	}

	@Override
	public StatementWriterInterface<E> SET(E instance)
	{
		return this;
	}

	//_________________________________________________________________________________ //
	
	@Override
	public StatementWriterInterface<E> WHERE()
	{
		return this;
	}

	@Override
	public StatementWriterInterface<E> WHERE(String sql)
	{
		return this;
	}

	@Override
	public StatementWriterInterface<E> AND(String sql)
	{
		return this;
	}

	@Override
	public StatementWriterInterface<E> OR(String sql)
	{
		return this;
	}
	
	@Override
	public StatementWriterInterface<E> LIKE(String sql)
	{
		return this;
	}
	
	@Override
	public StatementWriterInterface<E> LIKE(String sql, LIKE matchType)
	{
		return this;
	}
	
	//_________________________________________________________________________________ //

	@Override
	public StatementWriterInterface<E> INSERT_INTO()
	{
		return this;
	}

	@Override
	public StatementWriterInterface<E> VALUES(E instance)
	{
		return this;
	}

	@Override
	public StatementWriterInterface<E> VALUES(String sql)
	{
		return this;
	}
	
	//_________________________________________________________________________________ //

	@Override
	public StatementWriterInterface<E> DELETE()
	{
		return this;
	}

	@Override
	public StatementWriterInterface<E> ORDER_BY(String sql)
	{
		return this;
	}

	@Override
	public StatementWriterInterface<E> HAVING(String sql)
	{
		return this;
	}

	@Override
	public StatementWriterInterface<E> GROUP_BY(String sql)
	{
		return this;
	}
	
	//_________________________________________________________________________________ //

	@Override
	public StatementWriterInterface<E> INNER_JOIN(String sql)
	{
		return this;
	}

	@Override
	public StatementWriterInterface<E> LEFT_JOIN(String sql)
	{
		return this;
	}

	@Override
	public StatementWriterInterface<E> LEFT_OUTER_JOIN(String sql)
	{
		return this;
	}

	@Override
	public StatementWriterInterface<E> RIGHT_JOIN(String sql)
	{
		return this;
	}

	@Override
	public StatementWriterInterface<E> RIGHT_OUTER_JOIN(String sql)
	{
		return this;
	}

	@Override
	public StatementWriterInterface<E> FULL_OUTER_JOIN(String sql)
	{
		return this;
	}
	
	//_________________________________________________________________________________ //

}
