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

package de.amuttsch.ioweu.app.ui.fragments.debtlists;

import com.shamanland.fab.FloatingActionButton;

import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.amuttsch.ioweu.app.R;
import de.amuttsch.ioweu.app.database.SqliteHelper;
import de.amuttsch.ioweu.app.database.tables.BorrowerTable;
import de.amuttsch.ioweu.app.database.tables.DebtTable;
import de.amuttsch.ioweu.app.ui.DebtListsActivity;
import de.amuttsch.ioweu.app.ui.adapter.BorrowerAdapter;
import de.amuttsch.ioweu.app.ui.adapter.OnCardClickListener;
import de.amuttsch.ioweu.app.ui.fragments.FragmentInteractionListener;
import de.amuttsch.ioweu.app.ui.views.EmptyRecyclerView;

public class BorrowerListFragment extends Fragment implements DebtListsActivity.Callback {

    public final static String TAG = BorrowerListFragment.class.getSimpleName();

    private FragmentInteractionListener mListener;

    private SqliteHelper mSqliteHelper;

    private SQLiteDatabase mSQLiteDatabase;

    private EmptyRecyclerView mCardList;

    private BorrowerAdapter mBorrowerAdapter;

    private boolean mShowHistory = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSqliteHelper = new SqliteHelper(getActivity());
        mSQLiteDatabase = mSqliteHelper.getReadableDatabase();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_borrower_list, container, false);

        mCardList = (EmptyRecyclerView) view.findViewById(R.id.list);
        mCardList.setEmptyView(view.findViewById(android.R.id.empty));

        // use a linear layout manager
        mCardList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCardList.setItemAnimator(new DefaultItemAnimator());

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onCreateNewDebt(-1);
            }
        });

        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.app_name));

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateBorrowerCursor();
    }

    private void updateBorrowerCursor() {
        Cursor cursor;
        if (mShowHistory) {
            cursor = mSQLiteDatabase.rawQuery("SELECT * FROM " +
                    BorrowerTable.BORROWER_TABLE + " WHERE " + // Only where we have at least one debt
                    BorrowerTable.BORROWER_TABLE + "." + BorrowerTable.COLUMN_ID + " IN " +
                    "(SELECT " +
                    DebtTable.DEBT_TABLE + "." + DebtTable.COLUMN_BORROWER_ID +
                    " FROM " + DebtTable.DEBT_TABLE + ")", null);
        } else {
            cursor = mSQLiteDatabase.rawQuery("SELECT * FROM " +
                    BorrowerTable.BORROWER_TABLE + " WHERE " +
                    BorrowerTable.BORROWER_TABLE + "." + BorrowerTable.COLUMN_ID + " IN " +
                    "(SELECT " +
                    DebtTable.DEBT_TABLE + "." + DebtTable.COLUMN_BORROWER_ID +
                    " FROM " + DebtTable.DEBT_TABLE +
                    " WHERE " + DebtTable.COLUMN_AMOUNT + ">" + DebtTable.COLUMN_PAYED_BACK_AMOUNT +
                    ")",
                    null);
        }
        mBorrowerAdapter = new BorrowerAdapter(getActivity(), cursor, mSQLiteDatabase);
        mBorrowerAdapter.showHistory(mShowHistory);
        mBorrowerAdapter.setOnCardClickListener(new OnCardClickListener() {
            @Override
            public void onClick(View v, int position) {
                int borrowerId = mBorrowerAdapter.getBorrowerId(position);
                mListener.onBorrowerSelected(borrowerId);
            }
        });
        mCardList.setAdapter(mBorrowerAdapter);
        mBorrowerAdapter.notifyDataSetChanged();
    }

    @Override
    public void showHistory(boolean showHistory) {
        mShowHistory = showHistory;
        if (isVisible()) {
            updateBorrowerCursor();
        }
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
