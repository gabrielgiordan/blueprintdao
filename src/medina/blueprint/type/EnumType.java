/*
 * Copyright (C) 2013 Gabriel Giordano
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package medina.blueprint.type;


/**
 * Use this interface on your enum or class implementations to pass it dynamically as an enum
 * parameter value.
 *
 * <p>
 * It's equivalent to the <tt>ENUM</tt> constraint on databases.
 * </p>
 *
 * <p>
 * The implementation of this class is also needed when using a {@link SetType} class.
 * </p>
 *
 * @author Gabriel Giordano
 *
 * @see SetType
 */
public interface EnumType {

  /**
   * Returns the value of the enumerator.
   *
   * @return a string value.
   */
  String getValue();
}
