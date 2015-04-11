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
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import de.amuttsch.ioweu.app.R;
import de.amuttsch.ioweu.app.database.SqliteHelper;
import de.amuttsch.ioweu.app.database.tables.BorrowerTable;
import de.amuttsch.ioweu.app.database.tables.DebtTable;
import de.amuttsch.ioweu.app.ui.DebtListsActivity;
import de.amuttsch.ioweu.app.ui.adapter.DebtAdapter;
import de.amuttsch.ioweu.app.ui.fragments.FragmentInteractionListener;
import de.amuttsch.ioweu.app.utils.CalendarHelper;

public class DebtListFragment extends Fragment implements DebtListsActivity.Callback {

    public final static String TAG = DebtListFragment.class.getSimpleName();

    private FragmentInteractionListener mListener;

    private CalendarHelper mCalendarHelper;

    private SqliteHelper mSqliteHelper;

    private SQLiteDatabase mSQLiteDatabase;

    private ListView mListView;

    private ActionMode mActionMode;

    private DebtAdapter mDebtAdapter;

    private int mBorrowerId;

    private boolean mShowHistory = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSqliteHelper = new SqliteHelper(getActivity());
        mSQLiteDatabase = mSqliteHelper.getReadableDatabase();

        mCalendarHelper = new CalendarHelper(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_debts_list, container, false);

        mListView = (ListView) view.findViewById(R.id.lv_debts);
        mListView.setEmptyView(view.findViewById(android.R.id.empty));

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onCreateNewDebt(mBorrowerId);
            }
        });

        Cursor cursor = mSQLiteDatabase.query(BorrowerTable.BORROWER_TABLE,
                new String[] {BorrowerTable.COLUMN_NAME},
                BorrowerTable.COLUMN_ID + "=?",
                new String[] {String.valueOf(mBorrowerId)},
                null,
                null,
                null
        );
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex(BorrowerTable.COLUMN_NAME));
            ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(name + "...");
        }
        cursor.close();

        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int debtId = mDebtAdapter.getDebtId(position);
                mListener.onDebtSelected(debtId);
            }
        });
        mListView.setMultiChoiceModeListener(mListActionMode);

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
        updateDebtCursor();
    }

    @Override
    public boolean onBackPressed() {
        if (mActionMode != null) {
            mActionMode.finish();
            return true;
        } else {
            return false;
        }
    }

    public void setBorrowerId(int id) {
        mBorrowerId = id;
    }

    public void showHistory(boolean showHistory) {
        mShowHistory = showHistory;
        if (isVisible()) {
            updateDebtCursor();
        }
    }

    public void makeAllDebtsEven() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setMessage(R.string.dialog_make_all_even);
        dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialog.setPositiveButton(R.string.make_even, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSQLiteDatabase.execSQL("UPDATE " + DebtTable.DEBT_TABLE +
                        " SET " + DebtTable.COLUMN_PAYED_BACK_AMOUNT + "=" + DebtTable.COLUMN_AMOUNT
                        +
                        ", " + DebtTable.COLUMN_PAYED_BACK_DATE + "='" + mCalendarHelper.nowIso() +
                        "' WHERE " + DebtTable.COLUMN_BORROWER_ID + "=" + mBorrowerId);

                updateDebtCursor();

                Toast.makeText(getActivity(), R.string.made_all_even, Toast.LENGTH_LONG).show();
            }
        });

        dialog.create().show();
    }

    public void deleteAllDebts() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setMessage(R.string.dialog_delete_all_debts);
        dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSQLiteDatabase.delete(DebtTable.DEBT_TABLE,
                        DebtTable.COLUMN_BORROWER_ID + "=?",
                        new String[] {String.valueOf(mBorrowerId)} );

                updateDebtCursor();

                Toast.makeText(getActivity(), R.string.deleted_all_debts, Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        dialog.create().show();
    }

    private void makeDebtEvenAtPosition(int position) {
        mSQLiteDatabase.execSQL("UPDATE " + DebtTable.DEBT_TABLE +
                " SET " + DebtTable.COLUMN_PAYED_BACK_AMOUNT + "=" + DebtTable.COLUMN_AMOUNT
                +
                ", " + DebtTable.COLUMN_PAYED_BACK_DATE + "='" + mCalendarHelper.nowIso() +
                "' WHERE " + DebtTable.COLUMN_ID + "=" + mDebtAdapter.getDebtId(position));
    }

    private void deleteDebtAtPosition(int position) {
        mSQLiteDatabase.delete(DebtTable.DEBT_TABLE,
                DebtTable.COLUMN_ID + "=?",
                new String[] {String.valueOf(mDebtAdapter.getDebtId(position))} );
    }

    private void updateDebtCursor() {
        String showHistoryWhere = "";
        if (!mShowHistory) {
            showHistoryWhere = " and " + DebtTable.COLUMN_AMOUNT + ">" + DebtTable.COLUMN_PAYED_BACK_AMOUNT;
        }
        Cursor cursor = mSQLiteDatabase.rawQuery("SELECT * FROM " +
                        DebtTable.DEBT_TABLE +
                        " WHERE " +
                        DebtTable.COLUMN_BORROWER_ID +
                        "=?" +
                        showHistoryWhere +
                        " ORDER BY " + DebtTable.COLUMN_LEND_DATE + " DESC",
                new String[] {String.valueOf(mBorrowerId)});
        mDebtAdapter = new DebtAdapter(getActivity(), cursor, mSQLiteDatabase);
        mListView.setAdapter(mDebtAdapter);
        mDebtAdapter.notifyDataSetChanged();
    }

    private AbsListView.MultiChoiceModeListener mListActionMode = new AbsListView.MultiChoiceModeListener() {
        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
                boolean checked) {

        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mActionMode = mode;

            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.cab_debts, menu);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.grey_cab_status));
            return true;

        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
            // We need a copy of the checked item positions, since the sparse array is
            // initialized when we get to the positive button code.
            final SparseBooleanArray checkedPositions = mListView.getCheckedItemPositions().clone();

            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            int positiveButtonResource;
            switch (item.getItemId()) {
                case R.id.action_make_even:
                    dialog.setMessage(getResources().getQuantityString(
                            R.plurals.dialog_make_n_even,
                            mListView.getCheckedItemCount(),
                            mListView.getCheckedItemCount()));
                    positiveButtonResource = R.string.make_even;
                    break;
                case R.id.action_delete:
                    dialog.setMessage(getResources().getQuantityString(
                            R.plurals.dialog_delete_n_debts,
                            mListView.getCheckedItemCount(),
                            mListView.getCheckedItemCount()));
                    positiveButtonResource = R.string.delete;
                    break;
                default:
                    return false;
            }
            dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            dialog.setPositiveButton(positiveButtonResource, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    for (int i = 0; i < checkedPositions.size(); ++i) {
                        int position = checkedPositions.keyAt(i);
                        if (checkedPositions.valueAt(i)) {
                            switch (item.getItemId()) {
                                case R.id.action_make_even:
                                    makeDebtEvenAtPosition(position);
                                    break;
                                case R.id.action_delete:
                                    deleteDebtAtPosition(position);
                                    break;
                            }
                        }
                    }

                    updateDebtCursor();
                    mode.finish();
                }
            });

            dialog.create().show();

            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.orange_dark));
        }
    };
}
