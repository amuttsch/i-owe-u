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

package de.amuttsch.ioweu.app.utils;

import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import java.text.DecimalFormat;
import java.util.Currency;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import de.amuttsch.ioweu.app.App;
import de.amuttsch.ioweu.app.SettingsKeys;

public class CurrencyHelper {
    private Currency mCurrency;

    private SharedPreferences mSharedPreferences;

    public CurrencyHelper() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getApp());

        updateCurrentCurrency();
    }

    public void updateCurrentCurrency() {
        Currency localeCurrency = Currency.getInstance(Locale.getDefault());
        String currencyCode = mSharedPreferences.getString(SettingsKeys.CURRENCY, localeCurrency.getCurrencyCode());

        mCurrency = Currency.getInstance(currencyCode);
    }

    public Set<Currency> getAvailableCurrencies() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return Currency.getAvailableCurrencies();
        } else {
            Set<Currency> currencies = new HashSet<>();
            for (Locale l : Locale.getAvailableLocales()) {
                currencies.add(Currency.getInstance(l));
            }
            return currencies;
        }
    }

    public Currency getCurrency() {
        return mCurrency;
    }

    public int getDefaultFractionDigits() {
        return mCurrency.getDefaultFractionDigits();
    }

    public DecimalFormat getDecimalFormat() {
        DecimalFormat decimalFormat = new DecimalFormat();
        StringBuilder pattern = new StringBuilder();
        pattern.append("#0");
        if (getDefaultFractionDigits() > 0) {
            pattern.append(".");
            for (int i = 0; i<getDefaultFractionDigits();++i) {
                pattern.append("0");
            }
        }

        decimalFormat.setCurrency(getCurrency());
        pattern.append(" ¤");

        decimalFormat.applyPattern(pattern.toString());
        return decimalFormat;
    }
}
