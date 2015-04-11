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

public class DebtTable {

    public final static int DEBT_MONEY = 0;
    public final static int DEBT_ITEM = 1;

    public final static int THEY_OWE_ME = 0;
    public final static int I_OWE_THEM = 1;

    public final static String DEBT_TABLE = "debts";
    public final static String COLUMN_ID = "_id";

    public final static String COLUMN_BORROWER_ID = "borrower_id";
    public final static String COLUMN_DIRECTION = "directon";
    public final static String COLUMN_DEBT_TYPE = "debt_type";

    public final static String COLUMN_AMOUNT = "amount";
    public final static String COLUMN_PAYED_BACK_AMOUNT = "played_back_amout";

    public final static String COLUMN_DESCRIPTION = "description";
    public final static String COLUMN_BARCODE = "barcode";
    public final static String COLUMN_IMAGE_URI = "image_uri";

    public final static String COLUMN_LEND_DATE = "lend_date";
    public final static String COLUMN_DUE_DATE = "due_date";
    public final static String COLUMN_PAYED_BACK_DATE = "payed_back_date";

    private final static String DEBT_CREATE_TALBE = "create table "
            + DEBT_TABLE
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_BORROWER_ID + " integer, "
            + COLUMN_DIRECTION + " integer, "
            + COLUMN_DEBT_TYPE + " integer, "
            + COLUMN_AMOUNT + " real, "
            + COLUMN_PAYED_BACK_AMOUNT + " real, "
            + COLUMN_DESCRIPTION + " text not null, "
            + COLUMN_BARCODE + " text not null, "
            + COLUMN_IMAGE_URI + " text not null, "
            + COLUMN_LEND_DATE + " text not null, "
            + COLUMN_DUE_DATE + " text not null, "
            + COLUMN_PAYED_BACK_DATE + " text not null, "
            + " FOREIGN KEY(" + COLUMN_BORROWER_ID + ")"
            + " REFERENCES " + BorrowerTable.BORROWER_TABLE + "(" + BorrowerTable.COLUMN_ID + ")"
            + ")";

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(DEBT_CREATE_TALBE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        return;
    }
}
