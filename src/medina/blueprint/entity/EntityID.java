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
 * Indicates that the named column should be 
 * associated with the field or class containing
 * this annotation.
 * 
 * If the identity key is inherited, the annotation
 * with the named column must be declared above the class.
 * 
 * <p>The declaring class of the field 
 * containing this annotation must have a 
 * <tt>@Table</tt> annotation.</p>
 * 
 * @author Gabriel Giordano
 * @see EntityTable
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.TYPE })
public @interface EntityID {
	
	String value() default "";
	
}
