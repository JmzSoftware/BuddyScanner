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

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class SharedPrefsHelper {

    SharedPreferences sharedPreferences;

    public SharedPrefsHelper(Context mContext, String name) {
        sharedPreferences = mContext.getSharedPreferences(mContext.getPackageName() + "." + name, MODE_PRIVATE);
    }

    public String getString(String key) {
        return sharedPreferences.getString(key, "");
    }

    public Boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    public void putString(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    public void putBoolean(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public void Add(String itemUpc) {
        String index = String.valueOf(getCount() + 1);
        sharedPreferences.edit().putString(index, itemUpc).apply();
    }

    public void Delete(int rowId) {
        sharedPreferences.edit().remove(String.valueOf(rowId+1)).apply();
    }

    public int getCount() {
        return sharedPreferences.getAll().size();
    }

    public ArrayList<Item> getAll() {
        Map<String, ?> allItems = sharedPreferences.getAll();
        ArrayList<Item> items = new ArrayList<>();
        
        for (Map.Entry<String, ?> entry : allItems.entrySet()) {
            items.add(new Item(entry.getKey(), entry.getValue().toString()));
        }
        
        return items;
    }

    public boolean contains(String key) {
        return sharedPreferences.contains(key);
    }
}
