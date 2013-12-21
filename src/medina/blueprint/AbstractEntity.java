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

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedHashSet;

import medina.blueprint.AbstractEntity.EntityListener.EntityEvent;
import medina.blueprint.entity.EntityColumn;
import medina.blueprint.entity.EntityID;
import medina.blueprint.entity.EntityObject;
import medina.blueprint.entity.EntityTable;

public abstract class AbstractEntity implements EntitySpecification
{

	final protected Class<?> clazz;
	final protected String table;

	protected IdentityScope identity;
	protected Collection<ColumnScope> columns;
	protected Collection<ObjectScope> objects;
	protected Collection<ListScope> lists;

	// Constructors____________________________________________________________________ //

	AbstractEntity(Class<?> clazz)
	{
		this.clazz = clazz;
		this.columns = new LinkedHashSet<>();
		this.objects = new LinkedHashSet<>();
		this.lists = new LinkedHashSet<>();

		table = clazz.getAnnotation(EntityTable.class).value();
	}

	// EntityAccessor - Public Methods_________________________________________________ //

	@Override
	public Object getIdentityValue(Object instance) throws IllegalArgumentException, IllegalAccessException 
	{
		return identity.field.get(instance);
	}
	
	@Override
	public void nextSeveralVariables(EntityListener listener, Object instance) throws IllegalArgumentException, IllegalAccessException 
	{
		EntityEvent event = new EntityEvent();

		if (hasIdentity())
		{
			event.isIdentity = true;
			event.label = identity.label;

			if (identity.hasField())
			{
				event.value = identity.field.get(instance);
				event.field = identity.field.getName();
				listener.performAction(event);
			}
		}

		if (hasColumn())
		{
			event.reset();
			event.isColumn = true;

			for (Variable variable : this.columns)
			{
				event.value = variable.field.get(instance);
				event.field = variable.field.getName();
				event.label = variable.label;
				listener.performAction(event);
			}
		}

		if (hasEntityObject())
		{
			event.reset();
			event.isEntityObject = true;

			for (Variable variable : this.objects)
			{
				event.value = variable.field.get(instance);
				event.field = variable.field.getName();
				event.label = variable.label;
				listener.performAction(event);
			}
		}
	}
	
	@Override
	public void nextSeveralVariables(EntityListener listener)
	{
		EntityEvent event = new EntityEvent();

		if (hasIdentity())
		{
			event.isIdentity = true;
			event.label = identity.label;

			if (identity.hasField())
			{
				event.field = identity.field.getName();
				listener.performAction(event);
			}
		}

		if (hasColumn())
		{
			event.reset();
			event.isColumn = true;

			for (Variable variable : this.columns)
			{
				event.field = variable.field.getName();
				event.label = variable.label;
				listener.performAction(event);
			}
		}

		if (hasEntityObject())
		{
			event.reset();
			event.isEntityObject = true;

			for (Variable variable : this.objects)
			{
				event.field = variable.field.getName();
				event.label = variable.label;
				listener.performAction(event);
			}
		}
	}

	@Override
	public Collection<String> getAllLabels()
	{
		Collection<String> labels = new LinkedHashSet<>();
		
		if (hasIdentity())
		{
			if (identity.hasField())
			{
				labels.add(identity.getLabel());
			}
		}

		if (hasColumn())
		{
			for (Variable variable : this.columns)
			{
				labels.add(variable.getLabel());
			}
		}

		if (hasEntityObject())
		{
			for (Variable variable : this.objects)
			{
				labels.add(variable.getLabel());
			}
		}
		
		return labels;
	}	

	@Override
	public Class<?> getEntityClass()
	{
		return clazz;
	}

	@Override
	public String getTable()
	{
		return table;
	}

	@Override
	public IdentityScope getIdentity()
	{
		return identity;
	}

	@Override
	public Collection<ColumnScope> getColumns()
	{
		return columns;
	}

	@Override
	public Collection<ObjectScope> getEntityObjects()
	{
		return objects;
	}

	@Override
	public Collection<ListScope> getEntityLists()
	{
		return lists;
	}

	@Override
	public boolean hasIdentity()
	{
		return identity != null;
	}

	@Override
	public boolean hasEntityList()
	{
		return lists != null;
	}

	@Override
	public boolean hasEntityObject()
	{
		return objects != null;
	}

	@Override
	public boolean hasColumn()
	{
		return columns != null;
	}

	@Override
	public boolean hasSuperEntityClass()
	{
		return !clazz.getSuperclass().equals(Object.class);
	}

	// Package Inner Classes___________________________________________________________ //

	class Variable
	{

		protected String label;
		protected Field field;

		private Variable() {}

		protected Variable(Field field)
		{
			this.field = field;
			this.field.setAccessible(true);
		}

		public String getFieldName()
		{
			return field.getName();
		}

		public String getLabel()
		{
			return label;
		}
	}

	// Public Inner Classes____________________________________________________________ //

	public final class IdentityScope extends Variable
	{

		private boolean inherited;

		IdentityScope(Class<?> clazz)
		{
			label = clazz.getAnnotation(EntityID.class).value();

			Class<?> superClazz = EngineUtil.getLastSuperClass(clazz);

			field = EngineUtil.searchIdentity(superClazz);
			field.setAccessible(true);

			inherited = true;
		}

		IdentityScope(Field field)
		{
			super(field);
			label = field.getAnnotation(EntityID.class).value();
		}

		public boolean hasField()
		{
			return field != null;
		}

		public boolean isInherited()
		{
			return inherited;
		}
	}

	public final class ColumnScope extends Variable
	{

		Type genericType;

		ColumnScope(Field field)
		{
			super(field);
			label = field.getAnnotation(EntityColumn.class).value();
			genericType = EngineUtil.searchGenericType(field);
		}

		public boolean hasGenericType()
		{
			return genericType != null;
		}

		public Type getGenericType()
		{
			return genericType;
		}
	}

	public final class ObjectScope extends Variable
	{

		Field identity;

		ObjectScope(Field field)
		{
			super(field);
			label = field.getAnnotation(EntityObject.class).value();
			identity = EngineUtil
					.searchIdentity(EngineUtil.getLastSuperClass(this.field.getType()));
		}

		public boolean hasIdentity()
		{
			return identity != null;
		}

		public String getIdentityFieldName()
		{
			return identity.getName();
		}
	}

	public final class ListScope
	{

		protected Field field;
		private Type genericType;

		ListScope(Field field)
		{
			this.field = field;
			this.field.setAccessible(true);

			genericType = EngineUtil.searchGenericType(field);
		}

		public String getFieldName()
		{
			return field.getName();
		}

		public Type getGenericType()
		{
			return genericType;
		}

		public boolean hasGenericType()
		{
			return genericType != null;
		}
	}

	// Public Interfaces________________________________________________________________ //

	public interface EntityListener
	{

		void performAction(EntityEvent event);

		public final class EntityEvent
		{

			private boolean isIdentity;
			private boolean isColumn;
			private boolean isEntityObject;
			private String label;
			private String field;
			private Object value;

			private EntityEvent() {}

			public Object getValue()
			{
				return value;
			}
			
			public String getLabel()
			{
				return label;
			}

			public String getField()
			{
				return field;
			}

			public boolean hasField()
			{
				return field != null;
			}

			public boolean isIdentity()
			{
				return isIdentity;
			}

			public boolean isColumn()
			{
				return isColumn;
			}

			public boolean isEntityObject()
			{
				return isEntityObject;
			}

			private void reset()
			{
				isIdentity = false;
				isColumn = false;
				isEntityObject = false;
			}
		}
	}
}
