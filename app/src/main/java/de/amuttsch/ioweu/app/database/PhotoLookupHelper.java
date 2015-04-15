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

package de.amuttsch.ioweu.app.database;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

public class PhotoLookupHelper {

    public static String lookupPhotoThumbnail(Context context, String lookupKey) {
        String photoUri = "";

        Cursor photoCursor = context.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                new String[] {ContactsContract.Contacts.PHOTO_THUMBNAIL_URI},
                ContactsContract.Contacts.LOOKUP_KEY + "=?",
                new String[] {lookupKey},
                null
        );

        if (photoCursor.moveToFirst()) {
            photoUri = photoCursor.getString(photoCursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
            if (photoUri == null) {
                photoUri = "";
            }
        }
        photoCursor.close();

        return photoUri;
    }
}
