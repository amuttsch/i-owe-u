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

package de.amuttsch.ioweu.app.ui.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.EditText;

public class FixEditText extends EditText {

    private String mPrefix;

    private String mSuffix;

    private ColorStateList mPrefixColor;

    private ColorStateList mSuffixColor;

    public FixEditText(Context context) {
        super(context);
    }

    public FixEditText(Context context, AttributeSet attrs) {
        super(context, attrs, android.R.attr.editTextStyle);
    }

    public FixEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setPrefix(String prefix) {
        mPrefix = prefix;
    }

    public void setSuffix(String suffix) {
        mSuffix = suffix;
    }

    public void setPrefixColor(int color) {
        mPrefixColor = ColorStateList.valueOf(color);
    }

    public void setSuffixColor(int color) {
        mSuffixColor = ColorStateList.valueOf(color);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
