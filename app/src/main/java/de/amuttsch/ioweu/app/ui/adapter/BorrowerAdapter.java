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
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;

import de.amuttsch.ioweu.app.database.PhotoLookupHelper;
import de.hdodenhof.circleimageview.CircleImageView;
import de.amuttsch.ioweu.app.R;
import de.amuttsch.ioweu.app.database.tables.BorrowerTable;
import de.amuttsch.ioweu.app.database.tables.DebtTable;
import de.amuttsch.ioweu.app.utils.CurrencyHelper;

public class BorrowerAdapter extends CursorRecyclerViewAdapter<BorrowerAdapter.ViewHolder> {

    private static final String TAG = BorrowerAdapter.class.getSimpleName();

    private SQLiteDatabase mDatabase;

    private DecimalFormat mMoneyDecimalFormat;

    private boolean mShowHistory = false;

    public BorrowerAdapter(Context context, Cursor c, SQLiteDatabase database) {
        super(context, c);

        mDatabase = database;
        mMoneyDecimalFormat = new CurrencyHelper().getDecimalFormat();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_borrower, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        viewHolder.Borrower.setText(cursor.getString(cursor.getColumnIndex(BorrowerTable.COLUMN_NAME)));

        int borrowerId = cursor.getInt(cursor.getColumnIndex(BorrowerTable.COLUMN_ID));
        String lookupKey = cursor.getString(cursor.getColumnIndex(BorrowerTable.COLUMN_LOOKUP_KEY));

        String showHistoryWhere = "";
        if (!mShowHistory) {
            showHistoryWhere = " and " + DebtTable.COLUMN_AMOUNT + ">" + DebtTable.COLUMN_PAYED_BACK_AMOUNT;
        }

        Cursor debts = mDatabase.rawQuery(
                "select * from " +
                        DebtTable.DEBT_TABLE +
                        " WHERE " +
                        DebtTable.COLUMN_BORROWER_ID + "=?" +
                        showHistoryWhere,
                new String[] {String.valueOf(borrowerId)}
        );

        int itemsLend = 0;
        int itemsBorrowed = 0;
        double owingMoney = 0;
        while (debts.moveToNext()) {
            int debtType  = debts.getInt(debts.getColumnIndex(DebtTable.COLUMN_DEBT_TYPE));
            int direction = debts.getInt(debts.getColumnIndex(DebtTable.COLUMN_DIRECTION));
            double amount = debts.getDouble(debts.getColumnIndex(DebtTable.COLUMN_AMOUNT));

            switch (debtType) {
                case DebtTable.DEBT_ITEM:
                    if (direction == DebtTable.I_OWE_THEM) {
                        itemsBorrowed++;
                    } else {
                        itemsLend++;
                    }
                    break;
                case DebtTable.DEBT_MONEY:
                    if (direction == DebtTable.I_OWE_THEM) {
                        owingMoney -= amount;
                    } else {
                        owingMoney += amount;
                    }
                    break;
                default:
                    break;
            }
        }

        debts.close();

        viewHolder.DebtItemsBorrowed.setText(String.valueOf(itemsBorrowed));
        viewHolder.DebtItemsLend.setText(String.valueOf(itemsLend));

        viewHolder.DebtMoney.setText(mMoneyDecimalFormat.format(owingMoney));
        if (owingMoney > 0) {
            viewHolder.DebtMoney.setTextColor(mContext.getResources().getColor(R.color.green_debt));
        } else if (owingMoney < 0){
            viewHolder.DebtMoney.setTextColor(Color.RED);
        }

        // Get contact image if possible
        if (!lookupKey.isEmpty()) {
            String photoUri = PhotoLookupHelper.lookupPhotoThumbnail(mContext, lookupKey);
            if (!photoUri.isEmpty()) {
                viewHolder.ImageBorrower.setImageURI(Uri.parse(photoUri));
            }
        }
    }

    public int getBorrowerId(int position) {
        Cursor cursor = getCursor();
        if (cursor.moveToPosition(position)) {
            return cursor.getInt(cursor.getColumnIndex(BorrowerTable.COLUMN_ID));
        } else {
            return -1;
        }
    }

    public void showHistory(boolean showHistory) {
        mShowHistory = showHistory;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView ImageBorrower;
        public TextView Borrower;
        public TextView DebtMoney;
        public TextView DebtItemsBorrowed;
        public TextView DebtItemsLend;

        public ViewHolder(View itemView) {
            super(itemView);

            ImageBorrower = (CircleImageView) itemView.findViewById(R.id.image_borrower);
            Borrower = (TextView) itemView.findViewById(R.id.borrower);
            DebtMoney = (TextView) itemView.findViewById(R.id.debt_money);
            DebtItemsBorrowed = (TextView) itemView.findViewById(R.id.debt_items_borrowed);
            DebtItemsLend = (TextView) itemView.findViewById(R.id.debt_items_lend);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnCardClickListener != null) {
                        mOnCardClickListener.onClick(v, getAdapterPosition());
                    }
                }
            });
        }
    }
}
