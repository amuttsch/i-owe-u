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

package de.amuttsch.ioweu.app.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import de.amuttsch.ioweu.app.R;
import de.amuttsch.ioweu.app.ui.fragments.debtlists.BorrowerListFragment;
import de.amuttsch.ioweu.app.ui.fragments.debtlists.DebtListFragment;
import de.amuttsch.ioweu.app.ui.fragments.FragmentInteractionListener;

public class DebtListsActivity extends ActionBarActivity
        implements FragmentInteractionListener {

    public interface Callback {
        public void showHistory(boolean showHistory);
        public boolean onBackPressed();
    }

    private DrawerLayout mDrawerLayout;

    private ListView mDrawerList;

    private ActionBarDrawerToggle mDrawerToggle;

    private boolean mShowHistory = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new BorrowerListFragment(), BorrowerListFragment.TAG)
                    .addToBackStack(BorrowerListFragment.TAG)
                    .commit();
        }

        Toolbar actionBar = (Toolbar) findViewById(R.id.actionBar);
        setSupportActionBar(actionBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Set the adapter for the list view
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.navigation_drawer_items)));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);

        if (!drawerOpen) {
            getMenuInflater().inflate(R.menu.menu_list_activity, menu);

            boolean debtMenus = getCurrentFragmentTag().equals(DebtListFragment.TAG);
            menu.findItem(R.id.action_make_all_even).setVisible(debtMenus);
            menu.findItem(R.id.action_delete_all).setVisible(debtMenus);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        Callback currentFragment = getCurrentFragment();

        switch (item.getItemId()) {
            case R.id.action_show_history:
                item.setChecked(!item.isChecked());
                mShowHistory = item.isChecked();
                currentFragment.showHistory(mShowHistory);
                break;
            case R.id.action_make_all_even:
                ((DebtListFragment) currentFragment).makeAllDebtsEven();
                break;
            case R.id.action_delete_all:
                ((DebtListFragment) currentFragment).deleteAllDebts();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getCurrentFragment().onBackPressed())
            return;

        if (getFragmentManager().getBackStackEntryCount() <= 1) {
            this.finish();
        } else {
            getFragmentManager().popBackStack();
            invalidateOptionsMenu();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateNewDebt(int borrowerId) {
        Intent intent = new Intent(this, DebtEditorActivity.class);
        if (borrowerId != -1) {
            intent.putExtra(DebtEditorActivity.EXTRA_BORROWER_ID, borrowerId);
        }
        startActivity(intent);
    }

    @Override
    public void onBorrowerSelected(int borrowerId) {
        DebtListFragment debtListFragment = new DebtListFragment();
        debtListFragment.setBorrowerId(borrowerId);
        debtListFragment.showHistory(mShowHistory);

        getFragmentManager().beginTransaction()
                .replace(R.id.container, debtListFragment, DebtListFragment.TAG)
                .addToBackStack(DebtListFragment.TAG)
                .commit();

        getFragmentManager().executePendingTransactions();

        invalidateOptionsMenu();
    }

    @Override
    public void onDebtSelected(int debtId) {
        Intent intent = new Intent(this, DebtEditorActivity.class);
        intent.setAction(Intent.ACTION_EDIT);
        intent.putExtra(DebtEditorActivity.EXTRA_DEBT_ID, debtId);
        startActivity(intent);
    }

    private String getCurrentFragmentTag() {
        return getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 1).getName();
    }

    private Callback getCurrentFragment() {
        String tag = getCurrentFragmentTag();
        return (Callback) getFragmentManager().findFragmentByTag(tag);
    }
}
