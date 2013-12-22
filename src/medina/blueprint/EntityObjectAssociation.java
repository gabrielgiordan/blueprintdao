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
package medina.blueprint;

import java.security.InvalidParameterException;

import medina.blueprint.AbstractEntity.Variable;

final class EntityObjectAssociation extends SimpleEntityAssociation
    implements
      Comparable<EntityObjectAssociation> {

  final Variable variable;

  EntityObjectAssociation(final Object next, final Object code, final Variable variable) {
    super(next, code);
    this.variable = variable;
  }

  boolean isCodeEquals(final EntityObjectAssociation otherAssociation) {
    if (code.getClass().equals(String.class)) {
      return code.equals(otherAssociation.code);
    } else {
      return ((Number) code).longValue() == ((Number) otherAssociation.code).longValue();
    }
  }

  @Override
  public int compareTo(final EntityObjectAssociation o) {
    int comparison = variable.label.compareTo(o.variable.label);

    if (comparison == 0) {
      comparison = compareCode(this, o);
    }

    return comparison;
  }

  private static int compareCode(final EntityObjectAssociation o1, final EntityObjectAssociation o2) {
    final Class<?> type = o1.code.getClass();

    if (!type.equals(o2.code.getClass())) {
      return 0;
    }

    if (type.equals(Integer.TYPE) || type.equals(Integer.class)) {
      if ((int) o1.code > (int) o2.code) {
        return 1;
      } else if ((int) o1.code < (int) o2.code) {
        return -1;
      }

      return 0;
    }

    if (type.equals(Long.TYPE) || type.equals(Long.class)) {
      if ((long) o1.code > (long) o2.code) {
        return 1;
      } else if ((long) o1.code < (long) o2.code) {
        return -1;
      }

      return 0;
    }

    if (type.equals(String.class)) {
      return ((String) o1.code).compareTo((String) o2.code);
    }

    if (type.equals(Double.TYPE) || type.equals(Double.class)) {
      if ((double) o1.code > (double) o2.code) {
        return 1;
      } else if ((double) o1.code < (double) o2.code) {
        return -1;
      }

      return 0;
    }

    if (type.equals(Float.TYPE) || type.equals(Float.class)) {
      if ((float) o1.code > (float) o2.code) {
        return 1;
      } else if ((float) o1.code < (float) o2.code) {
        return -1;
      }

      return 0;
    }

    if (type.equals(Short.TYPE) || type.equals(Short.class)) {
      if ((short) o1.code > (short) o2.code) {
        return 1;
      } else if ((short) o1.code < (short) o2.code) {
        return -1;
      }

      return 0;
    }

    if (type.equals(Byte.TYPE) || type.equals(Byte.class)) {
      if ((byte) o1.code > (byte) o2.code) {
        return 1;
      } else if ((byte) o1.code < (byte) o2.code) {
        return -1;
      }

      return 0;
    }

    throw new InvalidParameterException("invalid primary type " + type + " at "
        + o1.variable.field.getType());
  }
}
