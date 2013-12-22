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

public class EntityListsSettings extends EntityRestrictions {

  boolean fillLists;
  boolean fillSubLists;

  public void setFillLists(final boolean fillLists) {
    this.fillLists = fillLists;
  }

  public void setFillSubLists(final boolean fillSubLists) {
    this.fillSubLists = fillSubLists;
  }

  public boolean isFillLists() {
    return fillLists;
  }

  public boolean isFillSubLists() {
    return fillSubLists;
  }
}
