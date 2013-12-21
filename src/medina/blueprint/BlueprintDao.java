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

import java.util.List;

import medina.blueprint.dao.DaoLayer;
import medina.blueprint.exception.BlueprintException;

public abstract class BlueprintDao<E> extends Blueprint<E> implements DaoLayer<E>
{
	//TODO criar factory
	// Constructors____________________________________________________________________ //

	public BlueprintDao(SessionManager session)
	{
		super(session);
	}

	// Public Methods__________________________________________________________________ //

	@Override
	public void useAutoIncrement(boolean increment)
	{
		statementTool.useAutoIncrement(increment);
	}
	
	@Override
	public void useAutoIncrement(String sequence)
	{
		statementTool.useAutoIncrement(true, sequence);
	}
	
	@Override
	public List<E> list()
	{
		setStatement(statementTool.select());

		return (List<E>) runSeveralRows();
	}

	@Override
	public E search(String identity)
	{
		return genericSearch(identity);
	}

	@Override
	public <N extends Number> E search(N identity)
	{
		return genericSearch(identity);
	}
	
	@Override
	public void save(E instance) throws BlueprintException
	{
		addAllPlaceholderValues(statementTool.insert(instance));
		setStatement(statementTool);
		
		if(statementTool.isAutoIncrement())
		{
			try
			{
				getEntity().getIdentity().field.set
				(
					instance, 
					runCustomAutoIncrementInsert
					(
						getEntity().getIdentity().field.getType(), 
						statementTool.getInsertColumns()
					)
				);
			}
			catch (IllegalArgumentException | IllegalAccessException e)
			{
				throw new BlueprintException(e);
			}
			
			System.err.println
			(
				"	Saved successfully, the auto generated key " +
				"was inserted on the " + getEntity().getIdentity().getFieldName() + 
				" field of " + getEntity().getEntityClass().getSimpleName() + "."
			);
		}
		else
		{
			int rowsAffected = runUpdate();
			
			if(rowsAffected == 0)
			{
				System.err.println("	Insert was no effect, no rows affected.");
			}
			else
			{
				System.err.println("	Saved successfully, " + rowsAffected + " rows affected.");
			}
		}
	}
	
	@Override
	public void update(E instance) throws BlueprintException
	{
		addAllPlaceholderValues(statementTool.update(instance));
		addPlaceholderValue(statementTool.where(instance));
		setStatement(statementTool);
		
		int rowsAffected = runUpdate();
		
		if(rowsAffected == 0)
		{
			System.err.println("	Update was no effect, no rows affected.");
		}
		else
		{
			System.err.println("	Updated successfully, " + rowsAffected + " rows affected.");
		}
	}
	
	@Override
	public void delete(String identity)
	{
		genericDelete(identity);
	}
	
	@Override
	public <N extends Number> void delete(N identity)
	{
		genericDelete(identity);
	}
	
	@Override
	public void delete(E instance) throws BlueprintException
	{
		addPlaceholderValue(statementTool.delete().where(instance));
		setStatement(statementTool);
		
		int rowsAffected = runUpdate();
		
		if(rowsAffected == 0)
		{
			System.err.println("	Delete was no effect, no rows affected.");
		}
		else
		{
			System.err.println("	Delete successfully, " + rowsAffected + " rows affected.");
		}
	}
	
	// Private Methods_________________________________________________________________ //

	private E genericSearch(Object identity)
	{
		setStatement(statementTool.select().where());

		addPlaceholderValue(identity);

		return runSingleRow();
	}
	
	private void genericDelete(Object identity) throws BlueprintException
	{
		setStatement(statementTool.delete().where());

		addPlaceholderValue(identity);
		
		int rowsAffected = runUpdate();
		
		if(rowsAffected == 0)
		{
			System.err.println("	Delete was no effect, no rows affected.");
		}
		else
		{
			System.err.println("	Delete successfully, " + rowsAffected + " rows affected.");
		}
	}
}
