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

package de.amuttsch.ioweu.app.ui.dialogs;

import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormat;

import de.amuttsch.ioweu.app.R;
import de.amuttsch.ioweu.app.utils.CurrencyHelper;
import de.amuttsch.ioweu.app.utils.Utilities;

public class AmountPicker extends DialogFragment {

    private final static String TAG = AmountPicker.class.getSimpleName();

    public interface OnAmountSelectedListener {
        public void onAmountSelected(double amount);
    }

    private enum Operators {NONE, PLUS, MINUS, MULTIPLY, DIVIDE}

    private Operators mOperator = Operators.NONE;

    private TextView mAmountText;

    private TextView mCalcAmountText;

    private int mDecimalPlaces;

    private boolean mUseCurrency;

    private CurrencyHelper mCurrencyHelper;

    private DecimalFormat mDecimalFormat;

    private double mAmount;

    private double mCalcValue;

    private OnAmountSelectedListener mOnAmountSelectedListener;

    private Button mBtnPlus, mBtnMinus, mBtnMul, mBtnDiv;

    private boolean mIntegrals;

    public AmountPicker() {
        initialize();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_amount_picker, container);
        getDialog().setTitle(R.string.amount_picker_title);

        mAmountText = (TextView) view.findViewById(R.id.txt_amount);
        mCalcAmountText = (TextView) view.findViewById(R.id.txt_calc_amount);

        view.findViewById(R.id.btn_one).setOnClickListener(onClickDigit);
        view.findViewById(R.id.btn_two).setOnClickListener(onClickDigit);
        view.findViewById(R.id.btn_three).setOnClickListener(onClickDigit);
        view.findViewById(R.id.btn_four).setOnClickListener(onClickDigit);
        view.findViewById(R.id.btn_five).setOnClickListener(onClickDigit);
        view.findViewById(R.id.btn_six).setOnClickListener(onClickDigit);
        view.findViewById(R.id.btn_seven).setOnClickListener(onClickDigit);
        view.findViewById(R.id.btn_eight).setOnClickListener(onClickDigit);
        view.findViewById(R.id.btn_nine).setOnClickListener(onClickDigit);
        view.findViewById(R.id.btn_zero).setOnClickListener(onClickDigit);
        view.findViewById(R.id.btn_zero_zero).setOnClickListener(onClickDigit);

        mBtnPlus = (Button) view.findViewById(R.id.btn_plus);
        mBtnMinus = (Button) view.findViewById(R.id.btn_minus);
        mBtnMul = (Button) view.findViewById(R.id.btn_mul);
        mBtnDiv = (Button) view.findViewById(R.id.btn_div);
        mBtnPlus.setOnClickListener(onClickOperator);
        mBtnMinus.setOnClickListener(onClickOperator);
        mBtnMul.setOnClickListener(onClickOperator);
        mBtnDiv.setOnClickListener(onClickOperator);
        mBtnPlus.setTag(Operators.PLUS);
        mBtnMinus.setTag(Operators.MINUS);
        mBtnMul.setTag(Operators.MULTIPLY);
        mBtnDiv.setTag(Operators.DIVIDE);

        view.findViewById(R.id.btn_equals).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateAmount();
            }
        });

        view.findViewById(R.id.btn_remove_last_digit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAmount = Utilities.RoundDown(mAmount, mDecimalPlaces - 1);
                mAmount /= 10;

                updateAmount();
            }
        });

        view.findViewById(R.id.fab_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateAmount();
                if (mOnAmountSelectedListener != null) {
                    mOnAmountSelectedListener.onAmountSelected(mAmount);
                }
                getDialog().dismiss();
            }
        });

        updateAmount();

        return view;
    }

    private void initialize() {
        mDecimalFormat = new DecimalFormat();
        mCurrencyHelper = new CurrencyHelper();
        mDecimalPlaces = mCurrencyHelper.getCurrency().getDefaultFractionDigits();

        updatePattern();

        mAmount = 0;
        mCalcValue = 0;
        mOperator = Operators.NONE;
    }

    private void updatePattern() {
        StringBuilder pattern = new StringBuilder();
        pattern.append("#0");
        if (mDecimalPlaces > 0) {
            pattern.append(".");
            for (int i = 0; i<mDecimalPlaces;++i) {
                pattern.append("0");
            }
        }

        if (mUseCurrency) {
            mDecimalFormat.setCurrency(mCurrencyHelper.getCurrency());
            pattern.append(" ¤");
        }

        mDecimalFormat.applyPattern(pattern.toString());
    }

    private void updateAmount() {
        mAmountText.setText(mDecimalFormat.format(mAmount));
        if (mCalcValue > 0) {
            mCalcAmountText.setText(mDecimalFormat.format(mCalcValue));
        } else {
            mCalcAmountText.setText("");
        }
    }

    private View.OnClickListener onClickDigit = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button button = (Button) v;
            double value;

            try {
                value = Double.valueOf(button.getText().toString());
                if (mIntegrals) {
                    value *= Math.pow(10, mDecimalPlaces);
                    mIntegrals = false;
                }
            } catch (NumberFormatException e) {
                return;
            }

            if (button.getId() == R.id.btn_zero_zero) {
                mAmount *= 100;
            } else {
                mAmount *= 10;
            }
            mAmount += value * Math.pow(10, -mDecimalPlaces);

            updateAmount();
        }
    };

    private View.OnClickListener onClickOperator = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Button button = (Button) v;

            if (mCalcValue > 0) {
                calculateAmount();
            }

            // Reset the other colors and set the button to green
            resetOperatorButtonsColors();
            button.setTextColor(Color.GREEN);

            mOperator = (Operators) button.getTag();
            mIntegrals = mOperator.equals(Operators.MULTIPLY) || mOperator.equals(Operators.DIVIDE);

            // Move the amount to the calc amount
            mCalcValue = mAmount;
            mAmount = 0;

            updateAmount();
        }
    };

    private void calculateAmount() {
        double result = 0;
        switch (mOperator) {
            case NONE:
                result = mAmount;
                break;
            case PLUS:
                result = mCalcValue + mAmount;
                break;
            case MINUS:
                result = mCalcValue - mAmount;
                break;
            case MULTIPLY:
                result = mCalcValue * mAmount;
                break;
            case DIVIDE:
                result = mCalcValue / mAmount;
                break;
        }
        setAmount(result);
        mCalcValue = 0;
        mOperator = Operators.NONE;
        resetOperatorButtonsColors();
        updateAmount();
    }

    private void resetOperatorButtonsColors() {
        mBtnPlus.setTextColor(Color.BLACK);
        mBtnMinus.setTextColor(Color.BLACK);
        mBtnMul.setTextColor(Color.BLACK);
        mBtnDiv.setTextColor(Color.BLACK);
    }

    public void setDecimalPlaces(int decimalPlaces) {
        mDecimalPlaces = decimalPlaces;
        updatePattern();
    }

    /**
     * Show the curreny sign in the dialog?
     * @param useCurrency True for showing the currency sign. Decimal places will be updated to the
     *                    default for the currency. False will hide the currency sign and won't show
     *                    decimal places. This can be overriden by calling setDecimalPlaces after
     *                    this method call.
     */
    public void setUseCurrency(boolean useCurrency) {
        mUseCurrency = useCurrency;
        if (useCurrency) {
            mDecimalPlaces = mCurrencyHelper.getCurrency().getDefaultFractionDigits();
        } else {
            mDecimalPlaces = 0;
        }
        updatePattern();
    }

    public double getAmount() {
        return mAmount;
    }

    public void setAmount(double amount) {
        mAmount = amount;
    }

    public DecimalFormat getDecimalFormat() {
        return mDecimalFormat;
    }

    public void setOnAmountSelectedListener(
            OnAmountSelectedListener onAmountSelectedListener) {
        mOnAmountSelectedListener = onAmountSelectedListener;
    }
}
