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

import java.util.HashSet;


/**
 * The SetType is a an enum type set that restricts the String types on it, which it can have a
 * maximum of 64 distinct members.
 *
 * <p>
 * It's equivalent to the <tt>SET</tt> constraint on databases.
 * </p>
 *
 * @author Gabriel Giordano
 * @see EnumType
 *
 * @param <E> - the enum type with particular values.
 */
public class SetType<E extends EnumType> extends HashSet<E> {

  private static final long serialVersionUID = 1L;
  private static final byte MAX_SIZE = 64;

  private final Class<E> type;

  /**
   * Constructs a new set type, using an determined {@link EnumType} implementation.
   *
   * @author Gabriel Giordano
   *
   * @param type - the enum class type that will be used on this particular set.
   *
   * @see EnumType
   */
  public SetType(final Class<E> type) {
    super();

    this.type = type;
  }

  /**
   * Convert and add a set of types on the current set.
   *
   * @author Gabriel Giordano
   *
   * @param types - the types separated by comma and without spaces between then.
   *
   * @see EnumSetType#getAllValues()
   */
  public void addByComma(final String types) {

    for (final String type : types.split(",")) {

      for (final Object constant : this.type.getEnumConstants()) {

        if (((EnumType) constant).getValue().equals(type)) {

          @SuppressWarnings("unchecked")
          final
          E result = (E) constant;

          add(result);
          break;
        }
      }
    }
  }

  @Override
  public boolean add(final E e) {

    if (size() >= MAX_SIZE) {

      throw new RuntimeException("maximum SetType size exceeded");
    }

    return super.add(e);
  }

  /**
   * Returns a string containing all the types ordered by comma.
   *
   * @author Gabriel Giordano
   * @return the type ordered by comma
   *
   * @see EnumSetType#addByComma(String)
   */
  public String getAllValues() {

    final StringBuilder result = new StringBuilder();

    for (final E enumType : this) {

      result.append(enumType.getValue() + ",");
    }

    return result.toString();
  }
}
