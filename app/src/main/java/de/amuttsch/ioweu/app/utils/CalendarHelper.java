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

import android.content.Context;
import android.util.Log;

import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class CalendarHelper {
    private final static String TAG = CalendarHelper.class.getSimpleName();

    private final String ISO_FORMAT = "yyyy-MM-dd";

    private String mLocalDatePattern;

    private DateFormat mDateFormatIso;

    private DateFormat mDateFormatLocal;

    public CalendarHelper(Context context) {
        Format dateFormat = android.text.format.DateFormat.getDateFormat(context);
        mLocalDatePattern = ((SimpleDateFormat) dateFormat).toLocalizedPattern();

        Locale locale = context.getResources().getConfiguration().locale;

        mDateFormatIso = new SimpleDateFormat(ISO_FORMAT, locale);
        mDateFormatLocal = new SimpleDateFormat(mLocalDatePattern, locale);
    }

    public String nowIso() {
        return fromCalendarToIso(new GregorianCalendar());
    }

    public String nowLocale() {
        return fromCalendarToLocal(new GregorianCalendar());
    }

    public String fromCalendarToIso(final GregorianCalendar calendar) {
        return mDateFormatIso.format(calendar.getTime());
    }

    public String fromCalendarToLocal(final GregorianCalendar calendar) {
        return mDateFormatLocal.format(calendar.getTime());
    }

    public String fromIsoToLocal(String iso8601date) {
        return fromCalendarToLocal(fromStringToCalendarIso(iso8601date));
    }

    public GregorianCalendar fromStringToCalendarIso(final String iso8601date) {
        GregorianCalendar calendar = new GregorianCalendar();

        try {
            Date date = mDateFormatIso.parse(iso8601date);
            calendar.setTime(date);
        } catch (ParseException e) {
            Log.d(TAG, "Could not parse " + iso8601date);
        }
        return calendar;
    }
}
