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

package de.amuttsch.ioweu.app.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import de.amuttsch.ioweu.app.R;

public class ContactsAdapter extends CursorAdapter implements Filterable {

    private final static String TAG = ContactsAdapter.class.getSimpleName();

    public class Contact {
        public String Name;
        public String LookupKey;
        public String ThumbnailUri;
    }

    public final static String[] PROJECTION = {
            ContactsContract.Data._ID,
            ContactsContract.Data.LOOKUP_KEY,
            ContactsContract.Data.DISPLAY_NAME_PRIMARY,
            ContactsContract.Data.PHOTO_THUMBNAIL_URI,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
    };

    public final static String SELECTION = ContactsContract.Data.MIMETYPE + "=?";
    public final static String[] SELECTION_ARG = {ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE};

    private Context mContext;

    public ContactsAdapter(Context context, Cursor c) {
        super(context, c, false);

        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.Picture = (ImageView) view.findViewById(R.id.img_picture);
        viewHolder.Name = (TextView) view.findViewById(R.id.txt_name);
        viewHolder.TypePhone = (TextView) view.findViewById(R.id.txt_type_phone);

        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        //int id = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID));
        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
        String photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));

        viewHolder.Name.setText(name);
        if (photoUri == null) {
            viewHolder.Picture.setImageResource(R.drawable.ic_contact_picture);
        } else {
            viewHolder.Picture.setImageURI(Uri.parse(photoUri));
        }

        String number = cursor.getString(
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        int type = cursor.getInt(
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
        StringBuilder sb = new StringBuilder();
        sb.append(context.getString(
                ContactsContract.CommonDataKinds.Phone.getTypeLabelResource(type)));
        sb.append(" <");
        sb.append(number);
        sb.append(">");
        viewHolder.TypePhone.setText(sb.toString());
    }

    public Contact getContactAtPosition(int position) {
        Contact contact = new Contact();

        Cursor c = getCursor();
        c.moveToPosition(position);
        contact.Name = c.getString(
                c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
        contact.LookupKey = getCursor().getString(
                c.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
        contact.ThumbnailUri = getCursor().getString(
                c.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));

        return contact;
    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        if (getFilterQueryProvider() != null) {
            return getFilterQueryProvider().runQuery(constraint);
        }

        if (constraint != null) {
            Cursor selection = mContext.getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI,
                    ContactsAdapter.PROJECTION,
                    ContactsAdapter.SELECTION + " and " +
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?",
                    new String[]{
                            SELECTION_ARG[0],
                            "%" + constraint.toString() + "%"},
                    null
            );

            return selection;
        }

        return null;
    }

    @Override
    public CharSequence convertToString(Cursor cursor) {
        String s = cursor.getString(
                getCursor().getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
        return s;
    }

    private class ViewHolder {
        private ImageView Picture;
        private TextView Name;
        private TextView TypePhone;
    }
}