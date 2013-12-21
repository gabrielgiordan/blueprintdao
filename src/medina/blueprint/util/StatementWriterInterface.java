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

import java.util.Collection;

import medina.blueprint.util.StatementWriter.LIKE;

public interface StatementWriterInterface<E>
{
	
	// Getters_________________________________________________________________________ //
	
	String getGeneratedStatement();
	
	Collection<Object> getGeneratedValues();
	
	String[] getGeneratedInsertColumns();
	
	// Behavioral Methods______________________________________________________________ //

	StatementWriterInterface<E> SET_CONDITION(boolean condition);

	StatementWriterInterface<E> SET_VALUE(Object value);

	StatementWriterInterface<E> SET_VALUES(Object... values);
	
	StatementWriterInterface<E> SET_VALUES_FROM(E instance);

	StatementWriterInterface<E> RESET();

	// Basic SQL_______________________________________________________________________ //

	StatementWriterInterface<E> SELECT();
	
	StatementWriterInterface<E> SELECT(String sql);

	StatementWriterInterface<E> SELECT_DISTINCT(String sql);

	StatementWriterInterface<E> FROM();

	//_________________________________________________________________________________ //
	
	StatementWriterInterface<E> UPDATE();
	
	StatementWriterInterface<E> UPDATE(String sql);
	
	StatementWriterInterface<E> UPDATE(E instance);

	StatementWriterInterface<E> SET();
	
	StatementWriterInterface<E> SET(String sql);
	
	StatementWriterInterface<E> SET(E instance);
	
	//_________________________________________________________________________________ //
	
	StatementWriterInterface<E> WHERE();
	
	StatementWriterInterface<E> WHERE(String sql);

	StatementWriterInterface<E> AND(String sql);

	StatementWriterInterface<E> OR(String sql);
	
	StatementWriterInterface<E> LIKE(String sql);
	
	StatementWriterInterface<E> LIKE(String sql, LIKE matchType);
	
	//_________________________________________________________________________________ //
	
	StatementWriterInterface<E> INSERT_INTO();

	StatementWriterInterface<E> VALUES(E instance);
	
	StatementWriterInterface<E> VALUES(String sql);
	
	//_________________________________________________________________________________ //
	
	StatementWriterInterface<E> DELETE();
	
	//_________________________________________________________________________________ //

	StatementWriterInterface<E> ORDER_BY(String sql);

	StatementWriterInterface<E> HAVING(String sql);
	
	StatementWriterInterface<E> GROUP_BY(String sql);
	
	//_________________________________________________________________________________ //

	StatementWriterInterface<E> INNER_JOIN(String sql);

	StatementWriterInterface<E> LEFT_JOIN(String sql);

	StatementWriterInterface<E> LEFT_OUTER_JOIN(String sql);

	StatementWriterInterface<E> RIGHT_JOIN(String sql);

	StatementWriterInterface<E> RIGHT_OUTER_JOIN(String sql);

	StatementWriterInterface<E> FULL_OUTER_JOIN(String sql);
	
	//_________________________________________________________________________________ //
}
