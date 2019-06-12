package br.com.mdr.uberclone.helper;

import android.content.Context;

public class Utils {
    public static int convertDpToPx(int dp, Context context) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dp * scale + 0.5f);
    }
}
