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
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;

import de.hdodenhof.circleimageview.CircleImageView;
import de.amuttsch.ioweu.app.R;
import de.amuttsch.ioweu.app.database.tables.DebtTable;
import de.amuttsch.ioweu.app.utils.CalendarHelper;
import de.amuttsch.ioweu.app.utils.CurrencyHelper;

public class DebtAdapter extends CursorAdapter {

    private DecimalFormat mMoneyDecimalFormat;

    private DecimalFormat mItemAmountFormat;

    private CalendarHelper mCalendarHelper;

    public DebtAdapter(Context context, Cursor c, SQLiteDatabase database) {
        super(context, c, 0);

        mMoneyDecimalFormat = new CurrencyHelper().getDecimalFormat();
        mItemAmountFormat = new DecimalFormat();
        mItemAmountFormat.applyPattern("#0");
        mCalendarHelper = new CalendarHelper(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_debt, parent, false);

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.ImageDebt = (CircleImageView) view.findViewById(R.id.image_debt);
        viewHolder.DebtTitle = (TextView) view.findViewById(R.id.debt_item_title);
        viewHolder.LendDate = (TextView) view.findViewById(R.id.debt_item_lend_date);
        viewHolder.DueDate = (TextView) view.findViewById(R.id.debt_item_due_date);
        viewHolder.DebtRepaid = (ImageView) view.findViewById(R.id.img_debt_repaid);

        view.setTag(viewHolder);

        view.setBackgroundResource(R.drawable.debt_item_selector);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int direction = cursor.getInt(cursor.getColumnIndex(DebtTable.COLUMN_DIRECTION));
        int debtType = cursor.getInt(cursor.getColumnIndex(DebtTable.COLUMN_DEBT_TYPE));
        String description = cursor.getString(cursor.getColumnIndex(DebtTable.COLUMN_DESCRIPTION));
        String lendDateIso = cursor.getString(cursor.getColumnIndex(DebtTable.COLUMN_LEND_DATE));
        String dueDateIso = cursor.getString(cursor.getColumnIndex(DebtTable.COLUMN_DUE_DATE));
        String imageUri = cursor.getString(cursor.getColumnIndex(DebtTable.COLUMN_IMAGE_URI));
        double amount = cursor.getDouble(cursor.getColumnIndex(DebtTable.COLUMN_AMOUNT));
        double repaid = cursor.getDouble(cursor.getColumnIndex(DebtTable.COLUMN_PAYED_BACK_AMOUNT));

        // Show either the default icons or the taken picture
        if (imageUri.isEmpty()) {
            switch (debtType) {
                case DebtTable.DEBT_MONEY:
                    viewHolder.ImageDebt.setImageResource(R.drawable.dollar);
                    break;
                case DebtTable.DEBT_ITEM:
                    viewHolder.ImageDebt.setImageResource(R.drawable.questionmark);
                    break;
            }
        } else {
            viewHolder.ImageDebt.setImageURI(Uri.parse(imageUri));
        }

        // Set the dates
        viewHolder.LendDate.setText(mCalendarHelper.fromIsoToLocal(lendDateIso));
        viewHolder.DueDate.setText(mCalendarHelper.fromIsoToLocal(dueDateIso));

        // Now set the debt
        StringBuilder sb = new StringBuilder();
        sb.append("... ");
        switch (direction) {
            case DebtTable.I_OWE_THEM:
                sb.append(context.getString(R.string.lend));
                break;
            case DebtTable.THEY_OWE_ME:
                sb.append(context.getString(R.string.borrowed));
                break;
        }
        sb.append(" ");

        // If we have a history record, show the actual amount
        double displayAmount = (amount == repaid) ? amount : amount - repaid;
        viewHolder.DebtRepaid.setVisibility((amount == repaid) ? View.VISIBLE : View.GONE);

        switch (debtType) {
            case DebtTable.DEBT_MONEY:
                sb.append(mMoneyDecimalFormat.format(displayAmount));
                sb.append(" ");
                break;
            case DebtTable.DEBT_ITEM:
                if (amount > 1) {
                    sb.append(mItemAmountFormat.format(displayAmount));
                    sb.append(" ");
                }
                break;
        }

        sb.append(description);

        viewHolder.DebtTitle.setText(sb.toString());
    }

    public int getDebtId(int position) {
        Cursor cursor = getCursor();
        if (cursor.moveToPosition(position)) {
            return cursor.getInt(cursor.getColumnIndex(DebtTable.COLUMN_ID));
        } else {
            return -1;
        }
    }

    private class ViewHolder {
        public CircleImageView ImageDebt;
        public TextView DebtTitle;
        public TextView LendDate;
        public TextView DueDate;
        public ImageView DebtRepaid;
    }
}
