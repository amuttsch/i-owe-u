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

package de.amuttsch.ioweu.app.ui.fragments;

import com.google.zxing.integration.android.IntentIntegrator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.util.GregorianCalendar;

import de.amuttsch.ioweu.app.R;
import de.amuttsch.ioweu.app.database.PhotoLookupHelper;
import de.amuttsch.ioweu.app.database.SqliteHelper;
import de.amuttsch.ioweu.app.database.tables.BorrowerTable;
import de.amuttsch.ioweu.app.database.tables.DebtTable;
import de.amuttsch.ioweu.app.ui.adapter.ContactsAdapter;
import de.amuttsch.ioweu.app.ui.dialogs.AmountPicker;
import de.amuttsch.ioweu.app.ui.dialogs.PickDateDialog;
import de.amuttsch.ioweu.app.utils.CalendarHelper;
import de.amuttsch.ioweu.app.utils.Utilities;

public class DebtEditorFragment extends Fragment {

    private static final String TAG = DebtEditorFragment.class.getSimpleName();

    public static int REQUEST_IMAGE_CAPTURE = 0x00000001;

    private final static int PICTURE_FILENAME_LENGTH = 16;

    public interface Listener {

        public void onSaveComplete();

        public boolean isEdit();
    }

    private Listener mListener;

    private SqliteHelper mSqliteHelper;

    private SQLiteDatabase mSQLiteDatabase;

    private CalendarHelper mCalendarHelper;

    private AutoCompleteTextView mBorrower;

    private ImageView mImageBorrower;

    private RadioGroup mDirection;

    private RadioButton mRadioButtonTheyOweMe;

    private RadioButton mRadioButtonIOweThem;

    private Button mLendDate;

    private Button mDueDate;

    private Button mRepaidDate;

    private Spinner mDebtType;

    private Button mAmount;

    private Button mRepaid;

    private EditText mDescription;

    private ImageButton mTakePictiure;

    private Bitmap mTakenBitmap = null;

    private Button mScanBarcode;

    private LinearLayout mContainerRepaidDate;

    private LinearLayout mContainerRepaidAmount;

    private boolean mHasBarcode = false;

    private String mIsoLendDate;

    private String mIsoDueDate;

    private String mIsoRepaidDate;

    private int mDebtId = -1;

    private int mBorrowerId = -1;

    // Hold a reference to the current animator,
    // so that it can be canceled mid-way.
    private Animator mCurrentAnimator;

    private FrameLayout mContainer;

    private ImageView mLargePicture;

    private AmountPicker mAmountPicker;

    private double mSelectedAmount;

    private double mSelectedRepaid;

    private ContactsAdapter mContactsAdapter;

    private ContactsAdapter.Contact mSelectedContact;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSqliteHelper = new SqliteHelper(getActivity());
        mSQLiteDatabase = mSqliteHelper.getWritableDatabase();

        mAmountPicker = new AmountPicker();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_debt_editor, container, false);
        mContainer = (FrameLayout) view.findViewById(R.id.debt_container);
        mLargePicture = (ImageView) view.findViewById(R.id.img_large_picture);

        mBorrower = (AutoCompleteTextView) view.findViewById(R.id.edt_borrower);
        mImageBorrower = (ImageView) view.findViewById(R.id.img_borrower);
        mDirection = (RadioGroup) view.findViewById(R.id.rg_direction);
        mRadioButtonTheyOweMe = (RadioButton) view.findViewById(R.id.rb_they_owe_me);
        mRadioButtonIOweThem = (RadioButton) view.findViewById(R.id.rb_i_ow_them);
        mLendDate = (Button) view.findViewById(R.id.btn_lend_date);
        mDueDate = (Button) view.findViewById(R.id.btn_due_date);
        mRepaidDate = (Button) view.findViewById(R.id.btn_repaid_date);
        mDebtType = (Spinner) view.findViewById(R.id.spinner_debt_type);
        mAmount = (Button) view.findViewById(R.id.btn_set_amount);
        mRepaid = (Button) view.findViewById(R.id.btn_set_repaid);
        mDescription = (EditText) view.findViewById(R.id.edt_description);
        mTakePictiure = (ImageButton) view.findViewById(R.id.btn_take_picture);
        mScanBarcode = (Button) view.findViewById(R.id.btn_scan_barcode);

        mContainerRepaidDate = (LinearLayout) view.findViewById(R.id.container_repaid_date);
        mContainerRepaidAmount = (LinearLayout) view.findViewById(R.id.container_repaid_amount);

        // Setup the borrower autocomplete
        mContactsAdapter = new ContactsAdapter(getActivity(), null);
        mBorrower.setAdapter(mContactsAdapter);
        mBorrower.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedContact = mContactsAdapter.getContactAtPosition(position);

                if (mSelectedContact.ThumbnailUri != null) {
                    mImageBorrower.setImageURI(Uri.parse(mSelectedContact.ThumbnailUri));
                } else {
                    mImageBorrower.setImageResource(R.drawable.ic_contact_picture);
                }
                hideKeyboard();
            }
        });
        mBorrower.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSelectedContact = null;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        getLoaderManager().initLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(
                        getActivity(),
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        ContactsAdapter.PROJECTION,
                        ContactsAdapter.SELECTION,
                        ContactsAdapter.SELECTION_ARG,
                        null);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                mContactsAdapter.swapCursor(data);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                mContactsAdapter.swapCursor(null);
            }
        });

        // Fill the type spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.debt_type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDebtType.setAdapter(adapter);

        mCalendarHelper = new CalendarHelper(getActivity());

        // Set current date
        mLendDate.setText(mCalendarHelper.nowLocale());
        mIsoLendDate = mCalendarHelper.nowIso();
        mDueDate.setText(mCalendarHelper.nowLocale());
        mIsoDueDate = mCalendarHelper.nowIso();
        mRepaidDate.setText(mCalendarHelper.nowLocale());
        mIsoRepaidDate = mCalendarHelper.nowIso();

        // Date picker
        mLendDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PickDateDialog dialog = new PickDateDialog();
                dialog.setGregorianCalendar(mCalendarHelper.fromStringToCalendarIso(mIsoLendDate));
                dialog.setListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                            int dayOfMonth) {
                        GregorianCalendar cal = new GregorianCalendar(year, monthOfYear,
                                dayOfMonth);
                        mIsoLendDate = mCalendarHelper.fromCalendarToIso(cal);
                        mLendDate.setText(mCalendarHelper.fromCalendarToLocal(cal));
                    }
                });
                dialog.show(getFragmentManager(), null);
            }
        });
        mDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PickDateDialog dialog = new PickDateDialog();
                dialog.setGregorianCalendar(mCalendarHelper.fromStringToCalendarIso(mIsoDueDate));
                dialog.setListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                            int dayOfMonth) {
                        GregorianCalendar cal = new GregorianCalendar(year, monthOfYear,
                                dayOfMonth);
                        mIsoDueDate = mCalendarHelper.fromCalendarToIso(cal);
                        mDueDate.setText(mCalendarHelper.fromCalendarToLocal(cal));
                    }
                });
                dialog.show(getFragmentManager(), null);
            }
        });
        mRepaidDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PickDateDialog dialog = new PickDateDialog();
                dialog.setGregorianCalendar(mCalendarHelper.fromStringToCalendarIso(mIsoRepaidDate));
                dialog.setListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                            int dayOfMonth) {
                        GregorianCalendar cal = new GregorianCalendar(year, monthOfYear,
                                dayOfMonth);
                        mIsoRepaidDate = mCalendarHelper.fromCalendarToIso(cal);
                        mRepaidDate.setText(mCalendarHelper.fromCalendarToLocal(cal));
                    }
                });
                dialog.show(getFragmentManager(), null);
            }
        });

        mAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAmountPicker
                        .setOnAmountSelectedListener(new AmountPicker.OnAmountSelectedListener() {
                            @Override
                            public void onAmountSelected(double amount) {
                                setAmount(amount);
                            }
                        });
                mAmountPicker.setAmount(mSelectedAmount);
                mAmountPicker.show(getFragmentManager(), "Amount");
            }
        });

        mRepaid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAmountPicker
                        .setOnAmountSelectedListener(new AmountPicker.OnAmountSelectedListener() {
                            @Override
                            public void onAmountSelected(double amount) {
                                setRepaid(amount);
                            }
                        });
                mAmountPicker.setAmount(mSelectedRepaid);
                mAmountPicker.show(getFragmentManager(), "Repaid");
            }
        });

        mAmountPicker.setUseCurrency(0 == mDebtType.getSelectedItemPosition());
        setAmount(0);
        setRepaid(0);

        mDebtType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                boolean isMoney = 0 == mDebtType.getSelectedItemPosition();
                mAmountPicker.setUseCurrency(isMoney);

                // Round down if an item is selected and update the amount picker
                if (!isMoney) {
                    mSelectedAmount = Utilities.RoundDown(mSelectedAmount, 0);
                    if (mSelectedAmount == 0) {
                        mSelectedAmount = 1;
                    }
                }
                setAmount(mSelectedAmount);
                setRepaid(mSelectedRepaid);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mTakePictiure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTakenBitmap == null) { //debt_image_longlclick
                    takePicture();
                } else {
                    zoomImageFromThumb(mTakePictiure);
                }
            }
        });
        mTakePictiure.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mTakenBitmap == null) {
                    return false;
                }

                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                        R.array.debt_image_longlclick, android.R.layout.simple_list_item_1);
                dialog.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                takePicture();
                                break;
                            case 1:
                                mTakePictiure.setImageDrawable(
                                        getResources().getDrawable(R.drawable.camera));
                                mTakenBitmap = null;
                                break;
                            default:
                                break;
                        }
                    }
                });
                dialog.show();
                return true;
            }
        });
        mTakePictiure.setBackground(null);

        mScanBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(getActivity());
                integrator.initiateScan();
            }
        });

        // Do we have an debt to load?
        if (mDebtId != -1) {
            loadDebt(mDebtId);
        } else if (mBorrowerId != -1) {
            loadBorrower(mBorrowerId);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!mListener.isEdit()) {
            mContainerRepaidDate.setVisibility(View.GONE);
            mContainerRepaidAmount.setVisibility(View.GONE);
        }
    }

    public boolean isValid() {
        if (mBorrower.getText().toString().isEmpty()) {
            mBorrower.setError(getString(R.string.error_borrower_empty));
            return false;
        }
        if (mSelectedAmount == 0) {
            Toast.makeText(getActivity(), R.string.error_amount_zero, Toast.LENGTH_LONG).show();
            return false;
        }
        if (mSelectedRepaid > mSelectedAmount) {
            Toast.makeText(getActivity(), R.string.error_repaid_exceeds_amount, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void saveDebt(boolean makeEven) {
        // Save image if possible
        String filename = "";
        if (mTakenBitmap != null) {
            File path = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            String randomName = Utilities.GetRandomString(PICTURE_FILENAME_LENGTH) + ".jpg";
            Utilities.SaveBitmapToExternalStorage(getActivity(),
                    mTakenBitmap,
                    path,
                    randomName);
            filename = path.getAbsolutePath() + "/" + randomName;
        }

        // Insert data into table
        ContentValues contentValues = new ContentValues();

        contentValues.put(DebtTable.COLUMN_BORROWER_ID, maybeInsertBorrower());
        if (mDirection.getCheckedRadioButtonId() == mRadioButtonTheyOweMe.getId()) {
            contentValues.put(DebtTable.COLUMN_DIRECTION, DebtTable.THEY_OWE_ME);
        } else if (mDirection.getCheckedRadioButtonId() == mRadioButtonIOweThem.getId()) {
            contentValues.put(DebtTable.COLUMN_DIRECTION, DebtTable.I_OWE_THEM);
        }

        contentValues.put(DebtTable.COLUMN_DEBT_TYPE, mDebtType.getSelectedItemPosition());
        contentValues.put(DebtTable.COLUMN_AMOUNT, mSelectedAmount);
        if (makeEven) {
            contentValues.put(DebtTable.COLUMN_PAYED_BACK_AMOUNT, mSelectedAmount);
            contentValues.put(DebtTable.COLUMN_PAYED_BACK_DATE, mCalendarHelper.nowIso());
        } else {
            contentValues.put(DebtTable.COLUMN_PAYED_BACK_AMOUNT, mSelectedRepaid);
            contentValues.put(DebtTable.COLUMN_PAYED_BACK_DATE, mIsoRepaidDate);
        }
        contentValues.put(DebtTable.COLUMN_DESCRIPTION, mDescription.getText().toString());
        contentValues.put(DebtTable.COLUMN_BARCODE,
                mHasBarcode ? mScanBarcode.getText().toString() : "");
        contentValues.put(DebtTable.COLUMN_IMAGE_URI, filename);
        contentValues.put(DebtTable.COLUMN_LEND_DATE, mIsoLendDate);
        contentValues.put(DebtTable.COLUMN_DUE_DATE, mIsoDueDate);

        if (mDebtId == -1) {
            mSQLiteDatabase.insert(DebtTable.DEBT_TABLE, null, contentValues);
        } else {
            String where = DebtTable.COLUMN_ID + "=" + mDebtId;
            mSQLiteDatabase.update(DebtTable.DEBT_TABLE, contentValues, where, null);
        }
    }

    /**
     * Check if for the given name a borrower exists. If not create it.
     *
     * @return The id from the borrower in the Borrower table.
     */
    private int maybeInsertBorrower() {
        int id;
        String borrower = mBorrower.getText().toString().trim();

        Cursor c = mSQLiteDatabase.query(BorrowerTable.BORROWER_TABLE,
                new String[]{BorrowerTable.COLUMN_ID}, // columns
                BorrowerTable.COLUMN_NAME + "=?", // select
                new String[]{borrower}, // select args
                null, // group
                null, // having
                null); // order

        // If we don't have an entry, insert one
        if (c.getCount() == 0) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(BorrowerTable.COLUMN_NAME, borrower);
            if (mSelectedContact == null) {
                contentValues.put(BorrowerTable.COLUMN_IS_CONTACT, false);
                contentValues.put(BorrowerTable.COLUMN_LOOKUP_KEY, "");
            } else {
                contentValues.put(BorrowerTable.COLUMN_IS_CONTACT, true);
                contentValues.put(BorrowerTable.COLUMN_LOOKUP_KEY, mSelectedContact.LookupKey);
            }

            id = (int) mSQLiteDatabase.insert(BorrowerTable.BORROWER_TABLE, null, contentValues);
        } else {
            c.moveToFirst();
            id = c.getInt(c.getColumnIndex(BorrowerTable.COLUMN_ID));
        }
        c.close();

        return id;
    }

    private void loadDebt(int debtId) {
        Cursor debt = mSQLiteDatabase.rawQuery(
                "select * from " +
                        DebtTable.DEBT_TABLE +
                        " where " + DebtTable.COLUMN_ID + "=?",
                new String[] {String.valueOf(debtId)});

        if (debt.moveToFirst()) {
            // Load borrower
            int borrowerId = debt.getInt(debt.getColumnIndex(DebtTable.COLUMN_BORROWER_ID));
            loadBorrower(borrowerId);

            // load the rest of the data
            int direction = debt.getInt(debt.getColumnIndex(DebtTable.COLUMN_DIRECTION));
            int type = debt.getInt(debt.getColumnIndex(DebtTable.COLUMN_DEBT_TYPE));
            double amount = debt.getDouble(debt.getColumnIndex(DebtTable.COLUMN_AMOUNT));
            double repaid = debt.getDouble(debt.getColumnIndex(DebtTable.COLUMN_PAYED_BACK_AMOUNT));
            String description = debt.getString(debt.getColumnIndex(DebtTable.COLUMN_DESCRIPTION));
            String barcode = debt.getString(debt.getColumnIndex(DebtTable.COLUMN_BARCODE));
            String imageUri = debt.getString(debt.getColumnIndex(DebtTable.COLUMN_IMAGE_URI));
            mIsoLendDate = debt.getString(debt.getColumnIndex(DebtTable.COLUMN_LEND_DATE));
            mIsoDueDate = debt.getString(debt.getColumnIndex(DebtTable.COLUMN_DUE_DATE));
            mIsoRepaidDate = debt.getString(debt.getColumnIndex(DebtTable.COLUMN_PAYED_BACK_DATE));

            if (direction == DebtTable.I_OWE_THEM) {
                mRadioButtonIOweThem.setChecked(true);
                mRadioButtonTheyOweMe.setChecked(false);
            } else if (direction == DebtTable.THEY_OWE_ME) {
                mRadioButtonIOweThem.setChecked(false);
                mRadioButtonTheyOweMe.setChecked(true);
            }

            mDebtType.setSelection(type);
            mAmountPicker.setUseCurrency(0 == mDebtType.getSelectedItemPosition());
            setAmount(amount);
            setRepaid(repaid);
            mDescription.setText(description);
            if (!barcode.isEmpty()) {
                mScanBarcode.setText(barcode);
            }
            if (!imageUri.isEmpty()) {
                Uri uri = Uri.parse(imageUri);
                mTakePictiure.setImageURI(uri);
                mTakenBitmap = BitmapFactory.decodeFile(imageUri);
            }
            mLendDate.setText(mCalendarHelper.fromIsoToLocal(mIsoLendDate));
            mDueDate.setText(mCalendarHelper.fromIsoToLocal(mIsoDueDate));
            if (mIsoRepaidDate.isEmpty()) {
                mRepaidDate.setText("-");
            } else {
                mRepaidDate.setText(mCalendarHelper.fromIsoToLocal(mIsoRepaidDate));
            }
        }
        debt.close();
    }

    private void loadBorrower(int borrowerId) {
        Cursor borrower = mSQLiteDatabase.query(
                BorrowerTable.BORROWER_TABLE,
                new String[]{BorrowerTable.COLUMN_NAME, BorrowerTable.COLUMN_IS_CONTACT,
                        BorrowerTable.COLUMN_LOOKUP_KEY},
                BorrowerTable.COLUMN_ID + "=?",
                new String[]{String.valueOf(borrowerId)},
                null,
                null,
                null,
                null
        );

        if (borrower.moveToFirst()) {
            String name = borrower.getString(borrower.getColumnIndex(BorrowerTable.COLUMN_NAME));
            String lookupKey = borrower.getString(borrower.getColumnIndex(BorrowerTable.COLUMN_LOOKUP_KEY));
            String photoUri = PhotoLookupHelper.lookupPhotoThumbnail(getActivity(), lookupKey);
            mBorrower.setAdapter(null);
            mBorrower.setText(name);

            if (!photoUri.isEmpty()) {
                mImageBorrower.setImageURI(Uri.parse(photoUri));
            }

            // Hide the keyboard and set the adapter after the name was entered, so the autocomplete
            // won't open.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBorrower.setAdapter(mContactsAdapter);
                    hideKeyboard();
                }
            }, 250);
        }
        borrower.close();
    }

    public void doSaveAction() {
        if (isValid()) {
            saveDebt(false);
            mListener.onSaveComplete();
        }
    }

    public void doMakeEven() {
        if (isValid()) {
            saveDebt(true);
            mListener.onSaveComplete();
        }
    }

    public void deleteDebt() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setMessage(R.string.dialog_delete_debt);
        dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (mDebtId != -1) {
                    mSQLiteDatabase.delete(DebtTable.DEBT_TABLE,
                            DebtTable.COLUMN_ID + "=?",
                            new String[] {String.valueOf(mDebtId)} );
                }

                mListener.onSaveComplete();
            }
        });

        dialog.create().show();
    }

    public void onPictureScanned(Bitmap bitmap) {
        BitmapDrawable bd = new BitmapDrawable(getActivity().getResources(), bitmap);
        mTakePictiure.setImageDrawable(bd);

        mTakenBitmap = bitmap;
    }

    public void onBarcodeScanned(String barcode) {
        if (barcode.isEmpty()) {
            mScanBarcode.setText(R.string.debt_editor_tab_to_scan);
            mHasBarcode = false;
        } else {
            mScanBarcode.setText(barcode);
            mHasBarcode = true;
        }
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    /**
     * Populate the fragment with the given debt. Is used for updating debts
     *
     * @param id The id from DebtTable
     */
    public void setDebtId(int id) {
        mDebtId = id;
        loadDebt(id);
    }

    /**
     * Populate the borrower with a given name
     *
     * @param id The id from the borrower table.
     */
    public void setBorrowerId(int id) {
        mBorrowerId = id;
        loadBorrower(id);
    }

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            getActivity().startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void setAmount(double amount) {
        mSelectedAmount = amount;
        mAmount.setText(mAmountPicker.getDecimalFormat().format(amount));
    }

    private void setRepaid(double amount) {
        mSelectedRepaid = amount;
        mRepaid.setText(mAmountPicker.getDecimalFormat().format(amount));
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mBorrower.getWindowToken(), 0);
    }

    private void zoomImageFromThumb(final View thumbView) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        final int shortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        // Load the high-resolution "zoomed-in" image.
        mLargePicture.setImageBitmap(mTakenBitmap);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        mContainer.getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        mLargePicture.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        mLargePicture.setPivotX(0f);
        mLargePicture.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(mLargePicture, "x",
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(mLargePicture, "y",
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(mLargePicture, "scaleX",
                        startScale, 1f)).with(ObjectAnimator.ofFloat(mLargePicture,
                "scaleY", startScale, 1f));
        set.setDuration(shortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        mLargePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(mLargePicture, "x", startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(mLargePicture,
                                        "y", startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(mLargePicture,
                                        "scaleX", startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(mLargePicture,
                                        "scaleY", startScaleFinal));
                set.setDuration(shortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        mLargePicture.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        mLargePicture.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }
}
