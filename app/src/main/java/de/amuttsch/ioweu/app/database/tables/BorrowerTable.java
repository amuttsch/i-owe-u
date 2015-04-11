/*
 * This file is part of I.owe.U.
 * Copyright (C) 2014, Andreas Muttscheller <andreas.muttscheller@gmail.com>
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.amuttsch.ioweu.app.database.tables;

import android.database.sqlite.SQLiteDatabase;

public class BorrowerTable {

    public final static String BORROWER_TABLE = "borrower";
    public final static String COLUMN_ID = "_id";
    public final static String COLUMN_NAME = "name";
    public final static String COLUMN_IS_CONTACT = "is_contact";
    public final static String COLUMN_LOOKUP_KEY = "lookup_key";

    private final static String BORROWER_CREATE_TALBE = "create table "
            + BORROWER_TABLE
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_NAME + " text not null, "
            + COLUMN_IS_CONTACT + " integer, "
            + COLUMN_LOOKUP_KEY + " text not null"
            + ")";

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(BORROWER_CREATE_TALBE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        return;
    }
}
