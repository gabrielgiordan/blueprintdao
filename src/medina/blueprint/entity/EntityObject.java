/*
 * Copyright (C) 2013 Gabriel Giordano
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
 * limitations under the License.
 */

package medina.blueprint.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the named foreign 
 * Many-to-One should be associated with 
 * the field object containing this annotation.
 * 
 * <p>The return type of the field
 * containing this annotation must have a
 * <tt>@Identity</tt> annotation and also a 
 * <tt>@Table</tt> annotation as well as 
 * the declaring class of the field must have a 
 * <tt>@Table</tt> annotation too.</p>
 * 
 * @author Gabriel Giordano
 * @see EntityTable
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface EntityObject {
	
	String value();
	
}
