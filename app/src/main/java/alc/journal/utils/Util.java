package alc.journal.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.CursorLoader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.googlecode.mp4parser.h264.Debug;

import java.io.IOException;
import java.util.Calendar;

import alc.journal.R;

import static android.content.Context.MODE_PRIVATE;

public class Util {

    public static final String USER_UID = "userUid";
    public static final String DATA_BASE_NAME = "journal";
    public static final String JOURNAL_ENTRY_KEY = "journalEntryKey";
    public static final String APP_PREFERENCES = "appPreferences";
    public static final String DISPLAY_ITEM_BAR_PREF = "displayItemPref";
    public static final String EDIT_JOURNAL = "editJournal";
    public static final String APP_NAME = "My journal";
    public static final int STORAGE_PERMISSION_CODE=1001;
    public static final  int RC_SIGN_IN =1;
    public static final String[] GALLERY_PERMISSION={
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void showMessageBoxSimple(Context context, String msgTitle, String msgText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(msgTitle);
        builder.setMessage(msgText);
        builder.setPositiveButton("OK", null);
        builder.show();

    }

    public static String getTimNow() {
        return java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
    }

    public static String getDateInt(Context context) {
        Calendar cal = Calendar.getInstance();
        return String.format(context.getResources().getString(R.string.format_string04), cal.get(Calendar.YEAR)) +
                String.format(context.getResources().getString(R.string.format_string02), cal.get(Calendar.MONTH) + 1) +
                String.format(context.getResources().getString(R.string.format_string02), cal.get(Calendar.DAY_OF_MONTH));
    }

    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API19(Context context, Uri uri) {
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = {MediaStore.Images.Media.DATA};

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{id}, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }


    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        String result = null;


        CursorLoader cursorLoader = new CursorLoader(
                context,
                contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        if (cursor != null) {
            int column_index =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
        }
        return result;
    }

    public static String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index
                = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public static boolean isPermissionGranted(Context context,String permission) {
        //Getting the permission status
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED ;


    }

    public static boolean getBooleanPreference(Context context ,String key){
        SharedPreferences mPreference = context.getSharedPreferences(APP_PREFERENCES,MODE_PRIVATE);
        return mPreference.getBoolean(key, TextUtils.equals(key,DISPLAY_ITEM_BAR_PREF));
    }

    public static void setBooleanPreference(Context context,String key,boolean value){
        SharedPreferences mPreference = context.getSharedPreferences(APP_PREFERENCES,MODE_PRIVATE);
        SharedPreferences.Editor editor =  mPreference.edit();
        editor.putBoolean(key, value).apply();
    }

    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null) return false;

        switch (activeNetwork.getType()) {
            case ConnectivityManager.TYPE_WIFI:
                if ((activeNetwork.getState() == NetworkInfo.State.CONNECTED ||
                        activeNetwork.getState() == NetworkInfo.State.CONNECTING)
                       )
                    return true;
                break;
            case ConnectivityManager.TYPE_MOBILE:
                if ((activeNetwork.getState() == NetworkInfo.State.CONNECTED ||
                        activeNetwork.getState() == NetworkInfo.State.CONNECTING)
                       )
                    return true;
                break;
            default:
                return false;
        }
        return false;
    }





}
