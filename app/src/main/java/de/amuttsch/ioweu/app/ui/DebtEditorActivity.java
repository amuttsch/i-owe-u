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

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import de.amuttsch.ioweu.app.R;
import de.amuttsch.ioweu.app.ui.fragments.DebtEditorFragment;

public class DebtEditorActivity extends ActionBarActivity
            implements DebtEditorFragment.Listener {

    public final static String EXTRA_BORROWER_ID = "extra_borrower_id";
    public final static String EXTRA_DEBT_ID = "extra_debt_id";

    private DebtEditorFragment mFragment;

    private boolean mIsEdit = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debt_editor);

        Intent intent = getIntent();
        String action = intent.getAction();

        Toolbar toolbar = (Toolbar) findViewById(R.id.actionBar);
        setSupportActionBar(toolbar);

        ImageView done = (ImageView) toolbar.findViewById(R.id.save_menu_item);
        TextView title = (TextView) toolbar.findViewById(R.id.title);
        done.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragment.doSaveAction();
            }
        });

        if (Intent.ACTION_EDIT.equals(action)) {
            title.setText(getResources().getString(
                    R.string.debt_editor_ab_edit_debt));
            mIsEdit = true;
        } else {
            title.setText(getResources().getString(
                    R.string.debt_editor_ab_new_debt));
            mIsEdit = false;
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        mFragment = (DebtEditorFragment) getFragmentManager().findFragmentById(R.id.fragment_debt_editor);
        mFragment.setListener(this);

        if (getIntent().hasExtra(EXTRA_DEBT_ID)) {
            int debtId = getIntent().getIntExtra(EXTRA_DEBT_ID, -1);
            mFragment.setDebtId(debtId);
        } else  if (getIntent().hasExtra(EXTRA_BORROWER_ID)) {
            int borrowerId = getIntent().getIntExtra(EXTRA_BORROWER_ID, -1);
            mFragment.setBorrowerId(borrowerId);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_debt_editor, menu);

        if (mIsEdit) {
            menu.findItem(R.id.action_make_even).setVisible(true);
        } else {
            menu.findItem(R.id.action_make_even).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_discard:
                finish();
                break;
            case R.id.action_make_even:
                mFragment.doMakeEven();
                break;
            case R.id.action_delete:
                mFragment.deleteDebt();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveComplete() {
        finish();
    }

    @Override
    public boolean isEdit() {
        return mIsEdit;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode,
                intent);
        if (scanResult != null) {
            mFragment.onBarcodeScanned(scanResult.getContents());
        }

        if (requestCode == DebtEditorFragment.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = intent.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mFragment.onPictureScanned(imageBitmap);
        }

    }
}
