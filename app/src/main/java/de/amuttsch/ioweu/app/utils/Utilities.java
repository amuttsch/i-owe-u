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
import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class Utilities {
    private final static String TAG = Utilities.class.getSimpleName();

    public static String GetRandomString(int len) {
        Random random = new Random();
        char[] chars = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
        StringBuilder sb = new StringBuilder();

        for (int i=0;i<len;++i) {
            sb.append(chars[random.nextInt(chars.length)]);
        }

        return sb.toString();
    }

    public static boolean SaveBitmapToExternalStorage(Context context, Bitmap bitmap, File dir,
            String filename) {
        boolean ret = false;
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, fos);
            fos.close();
            ret = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

    /**
     * Round down a number.
     * @param d The number to round down.
     * @param n The number of decimal places it should be rounded to.
     * @return The round down number.
     */
    public static double RoundDown(double d, int n) {
        double round = Math.pow(10, n);
        return Math.floor(d * round) / round;
    }
}
