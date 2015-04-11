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

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class PickDateDialog extends DialogFragment {

    private DatePickerDialog.OnDateSetListener mListener;

    private GregorianCalendar mGregorianCalendar = new GregorianCalendar();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int year = mGregorianCalendar.get(Calendar.YEAR);
        int month = mGregorianCalendar.get(Calendar.MONTH);
        int day = mGregorianCalendar.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), mListener, year, month, day);
    }

    public void setListener(DatePickerDialog.OnDateSetListener listener) {
        mListener = listener;
    }

    public void setGregorianCalendar(GregorianCalendar gregorianCalendar) {
        mGregorianCalendar = gregorianCalendar;
    }
}
