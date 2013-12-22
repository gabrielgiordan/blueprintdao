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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import medina.blueprint.exception.BlueprintException;
import medina.blueprint.util.StatementBuilder;

abstract class EngineSpecification<E>
{
	// Basic Methods__________________________________________________________________ //
	
	protected abstract Entity getEntity();

	protected abstract void setStatement(String sql);
	
	protected abstract void setStatement(StatementBuilder<E> builder);

	protected abstract void addPlaceholderValue(Object value);
	
	protected abstract void addAllPlaceholderValues(Collection<Object> values);
	
	protected abstract void resetAllPlaceholderValues();
	
	protected abstract int runAutoIncrementInsert() throws BlueprintException;
	
	protected abstract int runAutoIncrementInsert(String[] columns) throws BlueprintException;
	
	protected abstract <T> T runCustomAutoIncrementInsert(Class<T> keyType) throws BlueprintException;
	
	protected abstract <T> T runCustomAutoIncrementInsert(Class<T> keyType, String[] columns) throws BlueprintException;
	
	protected abstract int runUpdate() throws BlueprintException;
	
	protected abstract void initBatch() throws BlueprintException;
	
	protected abstract void addBatch() throws BlueprintException;
	
	protected abstract int[] runBatch() throws BlueprintException;
	
	protected abstract void setFetchSize(int size);
	
	public abstract int getFetchSize();
	
	// Query Methods___________________________________________________________________ //
	
	protected abstract E runSingleRow() throws BlueprintException;
	
	protected abstract List<E> runSeveralRows() throws BlueprintException;
	
	protected abstract void nextSingleRow(ResultSetListener listener) throws BlueprintException;
	
	protected abstract void nextSeveralRows(ResultSetListener listener) throws BlueprintException;
	
	// Protected Interfaces____________________________________________________________ //
	
	protected abstract class ResultSetListener
	{
		protected abstract void performAction(ResultSet resultSet, E next) throws BlueprintException, SQLException;
	}
}
