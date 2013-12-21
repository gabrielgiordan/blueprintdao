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
/**
 * 
 */
package medina.blueprint;

import java.util.Collection;

import medina.blueprint.AbstractEntity.ColumnScope;
import medina.blueprint.AbstractEntity.ListScope;
import medina.blueprint.AbstractEntity.EntityListener;
import medina.blueprint.AbstractEntity.ObjectScope;
import medina.blueprint.AbstractEntity.IdentityScope;

/**
 * @author Gabriel Giordano
 * 
 */
public interface EntitySpecification {

	Collection<String> getAllLabels();
	
	Class<?> getEntityClass();

	String getTable();

	IdentityScope getIdentity();

	Collection<ColumnScope> getColumns();

	Collection<ObjectScope> getEntityObjects();

	Collection<ListScope> getEntityLists();

	boolean hasIdentity();

	boolean hasColumn();

	boolean hasEntityList();

	boolean hasEntityObject();

	boolean hasSuperEntityClass();
	
	void nextSeveralVariables(EntityListener listener);
	
	void nextSeveralVariables(EntityListener listener, Object instance) throws IllegalArgumentException, IllegalAccessException;

	Object getIdentityValue(Object instance) throws IllegalArgumentException, IllegalAccessException;
	
}
