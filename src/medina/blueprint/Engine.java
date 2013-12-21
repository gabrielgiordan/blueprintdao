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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import medina.blueprint.AbstractEntity.ListScope;
import medina.blueprint.AbstractEntity.ObjectScope;
import medina.blueprint.AbstractEntity.Variable;
import medina.blueprint.exception.BlueprintException;

class Engine<E> extends AbstractEngine<E>
{
	
	private PriorityQueue<EntityObjectAssociation> objectsAssociations;
	private Collection<SimpleEntityAssociation> listAssociations;
	private Collection<SimpleEntityAssociation> inheritedAssociations;

	private Entity defaultEntity;
	private Entity currentEntity;
	
	// Constructors____________________________________________________________________ //

	Engine(SessionManager session)
	{
		super(session);
		
		objectsAssociations = new PriorityQueue<>(100);
		listAssociations = new ArrayList<>();
		inheritedAssociations = new ArrayList<>();
	}

	// Private Methods_________________________________________________________________ //
	
	private Object fillObject(Object object, boolean fillObjects, boolean fillLists) throws SQLException {
		
		try
		{	
			if(currentEntity.hasIdentity() && currentEntity.getIdentity().hasField())
			{
				if(resultSetTranslator.hasColumn(currentEntity.getIdentity().label))
				{
					resultSetTranslator.translate
					(
						currentEntity.getIdentity(), 
						object
					);
				}
			}
			
			if(currentEntity.hasColumn())
			{
				for (Variable variable : currentEntity.getColumns())
				{
					if(resultSetTranslator.hasColumn(variable.label))
					{
						resultSetTranslator.translate(variable, object);
					}
				}
			}
			
			if(currentEntity.hasEntityObject() && fillObjects)
			{
				for (ObjectScope entityObject : currentEntity.getEntityObjects())
				{
					if(resultSetTranslator.hasColumn(entityObject.label))
					{
						objectsAssociations.add
						(
							resultSetTranslator.tranlateToAssociation
							(
								entityObject, 
								object
							)
						);
					}
				}
			}
			
			if(currentEntity.hasIdentity() && currentEntity.getIdentity().isInherited())
			{
				inheritedAssociations.add
				(
					new SimpleEntityAssociation
					(
						object,
						currentEntity.getIdentity().field.get(object)
					)
				);
			}
			
			if(currentEntity.hasEntityList() && fillLists)
			{
				listAssociations.add
				(
					new SimpleEntityAssociation
					(
						object,
						currentEntity.getIdentity().field.get(object)
					)
				);
			}
			
			return object;
		}
		catch(IllegalAccessException | IllegalArgumentException e)
		{
			throw new BlueprintException(e);
		}
	}
	
	private void fillSuperClasses() throws SQLException
	{	
		
		if(!currentEntity.hasSuperEntityClass())
		{
			return;
		}

		Entity defaultCurrent = currentEntity;
		
		while (currentEntity.hasSuperEntityClass())
		{
			currentEntity = SessionManager.getEntity(currentEntity.clazz.getSuperclass());

			if (objectsSettings.containsRestrictions(currentEntity.clazz))
			{

				setStatement(new EngineStatementTool(currentEntity)
					.select(objectsSettings.getPermissions(currentEntity.clazz))
					.where(currentEntity.getIdentity().getLabel()));
				
				openStatement();
			}
			else
			{
				
				setStatement(new EngineStatementTool(currentEntity)
					.select()
					.where(currentEntity.getIdentity().getLabel()));
				
				openStatement();
			}
			
			Collection<SimpleEntityAssociation> associations = inheritedAssociations;
			inheritedAssociations = new ArrayList<>();
			
			for (SimpleEntityAssociation association : associations)
			{
				addPlaceholderValue(association.code);
				openResultSet();

				if(resultSet.next())
				{
					fillObject
					(
						association.next, 
						objectsSettings.isFillObjects(), 
						listsSettings.isFillLists()
					);
				}
				
				closeResultSet();
			}	
		}
		
		currentEntity = defaultCurrent;
	}
	
	private void fillEntityObjects() throws SQLException
	{

		if (objectsAssociations.isEmpty())
		{
			return;
		}
		
		PriorityQueue<EntityObjectAssociation> associations = objectsAssociations;
		objectsAssociations = new PriorityQueue<>(100);
		
		EntityObjectAssociation previous = null;
		
		Object result = null;
		Set<Entity> inherited = new HashSet<>();
		
		while (!associations.isEmpty())
		{
			EntityObjectAssociation current = associations.poll();
			
			try 
			{
				if(previous == null || !current.variable.label.equals(previous.variable.label)) 
				{
					currentEntity = SessionManager.getEntity(current.variable.field.getType());

					if(currentEntity.hasSuperEntityClass())
					{
						inherited.add(currentEntity);
					}
					
					if(objectsSettings.containsRestrictions(currentEntity))
					{
						setStatement
						(
							new EngineStatementTool(currentEntity)
							.select(objectsSettings.getPermissions(currentEntity))
							.where(currentEntity.getIdentity().getLabel())
						);
						
						openStatement();
					}
					else
					{
						setStatement
						(
							new EngineStatementTool(currentEntity)
							.select()
							.where(currentEntity.getIdentity().getLabel())
						);
						
						openStatement();
					}
				}
				
				if(previous == null || !current.isCodeEquals(previous) ||
					!current.variable.getLabel().equals(previous.variable.getLabel()))
				{
					addPlaceholderValue(current.code);
					openResultSet();
					
					if(resultSet.next())
					{
						result = currentEntity.clazz.newInstance();
						fillObject
						(
							result, 
							objectsSettings.isFillSubObjects(), 
							listsSettings.isFillLists()
						);
					}
					else
					{
						result = null;
					}
					
					closeResultSet();
				}
				
				if(result != null)
				{
					current.variable.field.set(current.next, result);
				}
				
			}
			catch (InstantiationException | IllegalAccessException e)
			{
				throw new BlueprintException(e);
			}
			
			previous = current;
		}
		
		if (objectsSettings.isFillSubObjects())
		{
			fillEntityObjects();
		}
		
		for (Entity entity : inherited)
		{
			currentEntity = entity;
			fillSuperClasses();
		}
		
		currentEntity = defaultEntity;
	}
	
	private void fillEntityLists() throws SQLException
	{
		if(!currentEntity.hasEntityList())
		{
			return;
		}
		
		Collection<SimpleEntityAssociation> associations = listAssociations;
		listAssociations = new ArrayList<>();
		
		for (ListScope entityList : currentEntity.getEntityLists())
		{
			Class<?> genericClazz = (Class<?>) entityList.getGenericType();
			
			if(listsSettings.isClassRestricted(genericClazz))
			{
				continue;
			}
			
			String label = listsSettings.restrictColumnByObject
			( 
				genericClazz,
				currentEntity.getEntityClass()
			);
			
			currentEntity = SessionManager.getEntity(genericClazz);
			
			setStatement(new EngineStatementTool(currentEntity)
					.select(listsSettings.getPermissions(genericClazz))
					.where(label));
			
			openStatement();
			
			try
			{
				for (SimpleEntityAssociation association : associations)
				{
					Collection<Object> result = new ArrayList<>();
					
					addPlaceholderValue(association.code);
					openResultSet();
					
					while(resultSet.next())
					{
						result.add
						(
							fillObject
							(
								currentEntity.clazz.newInstance(), 
								objectsSettings.isFillObjects(), 
								listsSettings.isFillSubLists()
							)
						);
					}
					
					closeResultSet();
					
					entityList.field.set(association.next, result);
				}
			}
			catch(InstantiationException | IllegalAccessException e)
			{
				throw new BlueprintException(e);
			}
			
			currentEntity = defaultEntity;
		}
	}
	
	private void fillRemaining() throws SQLException
	{
		fillSuperClasses();
		fillEntityObjects();
		
		if(listsSettings.isFillLists())
		{
			fillEntityLists();
			fillEntityObjects();
		}
	}

	// Package Methods_________________________________________________________________ //
	
	final void setDefaultEntity(Class<?> clazz)
	{
		defaultEntity = SessionManager.getEntity(clazz);
		setCurrentEntity(defaultEntity);
	}
	
	final void setCurrentEntity(Class<?> clazz) 
	{
		currentEntity = SessionManager.getEntity(clazz);
	}
	
	final void setCurrentEntity(Entity entity) 
	{
		currentEntity = entity;
	}
	
	// Protected Methods_______________________________________________________________ //
	
	@Override
	protected final Entity getEntity()
	{
		return defaultEntity;
	}
	
	@Override
	protected final E runSingleRow() throws BlueprintException
	{
		try
		{
			runQuery();
			
			Object object = null;
			
			if (resultSet.next())
			{	
				object = defaultEntity.clazz.newInstance();
				
				fillObject
				(
					object, 
					objectsSettings.isFillObjects(), 
					listsSettings.isFillLists()
				);
			}
			
			fillRemaining();
			
			@SuppressWarnings("unchecked")
			E row = (E) object;
			
			return row;
		}
		catch (SQLException | InstantiationException | IllegalAccessException e)
		{
			throw new BlueprintException(e);
		}
		finally
		{
			closeResultSet();
		}
	}
	
	@Override
	protected final List<E> runSeveralRows() throws BlueprintException
	{
		try
		{
			runQuery();
			
			List<Object> collection = new ArrayList<>();
			
			while (resultSet.next())
			{
				collection.add
				(
					fillObject
					(
						defaultEntity.clazz.newInstance(), 
						objectsSettings.isFillObjects(), 
						listsSettings.isFillLists()
					)
				);
			}
			
			fillRemaining();
			
			@SuppressWarnings("unchecked")
			List<E> rows = (List<E>) collection;
			
			return rows;
		}
		catch (SQLException | InstantiationException | IllegalAccessException e)
		{
			throw new BlueprintException(e);
		}
		finally
		{
			closeResultSet();
		}
	}
	
	@Override
	protected final void nextSingleRow(final ResultSetListener listener) throws BlueprintException
	{
		try
		{
			runQuery();
			
			if (resultSet.next())
			{	
				@SuppressWarnings("unchecked")
				E row = (E) defaultEntity.clazz.newInstance();
				
				fillObject
				(
					row, 
					objectsSettings.isFillObjects(), 
					listsSettings.isFillLists()
				);
				
				listener.performAction(resultSet, row);
			}
			
			fillRemaining();
			
		}
		catch (SQLException | InstantiationException | IllegalAccessException e)
		{
			throw new BlueprintException(e);
		}
		finally
		{
			closeResultSet();
		}
	}
	
	@Override
	protected final void nextSeveralRows(final ResultSetListener listener) throws BlueprintException
	{
		try
		{
			runQuery();
			
			while(resultSet.next())
			{	
				@SuppressWarnings("unchecked")
				E row = (E) defaultEntity.clazz.newInstance();
				
				fillObject
				(
					row, 
					objectsSettings.isFillObjects(), 
					listsSettings.isFillLists()
				);
				
				listener.performAction(resultSet, row);
			}
			
			fillRemaining();
			
		}
		catch (SQLException | InstantiationException | IllegalAccessException e)
		{
			throw new BlueprintException(e);
		}
		finally
		{
			closeResultSet();
		}
	}
}
