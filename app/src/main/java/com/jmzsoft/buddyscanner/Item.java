/*
 * Buddy Scanner
 *
 * Authors: James Taylor <james.taylor@jmzsoft.com>
 *
 * Copyright (C) 2020 James Taylor
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.jmzsoft.buddyscanner;

public class Item {

    private String itemId;
    private String value;

    public Item(String _itemId, String _value) {
        itemId = _itemId;
        value = _value;
    }

    public String getItemId() {
        return itemId;
    }

    public String getValue() {
        return value;
    }
}
