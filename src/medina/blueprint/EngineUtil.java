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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import medina.blueprint.entity.EntityID;

/** @author Gabriel Giordano */
class EngineUtil
{
	
	private EngineUtil() {}

	static Class<?> resolveGenericType(Class<?> clazz)
	{
		return (Class<?>) ( (ParameterizedType) clazz.getGenericSuperclass() )
				.getActualTypeArguments()[0];
	}

	static Class<?> getLastSuperClass(Class<?> clazz)
	{
		while (!clazz.getSuperclass().equals(Object.class))
			clazz = clazz.getSuperclass();

		return clazz;
	}

	static Field searchIdentity(Class<?> clazz)
	{
		for (Field field : clazz.getDeclaredFields())
		{
			if (field.isAnnotationPresent(EntityID.class))
			{
				return field;
			}
		}

		return null;
	}

	static Type searchGenericType(Field field)
	{
		try
		{
			return ( (ParameterizedType) field.getGenericType() ).getActualTypeArguments()[0];
		}
		catch (ClassCastException e) {}

		return null;
	}
}
