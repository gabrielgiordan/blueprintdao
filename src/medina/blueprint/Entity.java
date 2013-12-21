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

import medina.blueprint.entity.EntityColumn;
import medina.blueprint.entity.EntityList;
import medina.blueprint.entity.EntityObject;
import medina.blueprint.entity.EntityID;

public final class Entity extends AbstractEntity
{
	
	// Constructors____________________________________________________________________ //

	Entity(Class<?> clazz)
	{
		super(clazz);

		for (Field field : clazz.getDeclaredFields())
		{
			if (field.isAnnotationPresent(EntityColumn.class))
			{
				columns.add(new ColumnScope(field));
			}
			else if (field.isAnnotationPresent(EntityID.class))
			{
				identity = new IdentityScope(field);
			}
			else if (field.isAnnotationPresent(EntityObject.class))
			{
				objects.add(new ObjectScope(field));
			}
			else if (field.isAnnotationPresent(EntityList.class))
			{
				lists.add(new ListScope(field));
			}
		}

		if (columns.isEmpty())
		{
			columns = null;
		}

		if (objects.isEmpty())
		{
			objects = null;
		}

		if (lists.isEmpty())
		{
			lists = null;
		}

		if (!hasIdentity() && clazz.isAnnotationPresent(EntityID.class))
		{
			identity = new IdentityScope(clazz);
		}
	}
}
