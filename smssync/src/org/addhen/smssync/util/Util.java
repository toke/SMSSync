/*****************************************************************************
 ** Copyright (c) 2010 - 2012 Ushahidi Inc
 ** All rights reserved
 ** Contact: team@ushahidi.com
 ** Website: http://www.ushahidi.com
 **
 ** GNU Lesser General Public License Usage
 ** This file may be used under the terms of the GNU Lesser
 ** General Public License version 3 as published by the Free Software
 ** Foundation and appearing in the file LICENSE.LGPL included in the
 ** packaging of this file. Please review the following information to
 ** ensure the GNU Lesser General Public License version 3 requirements
 ** will be met: http://www.gnu.org/licenses/lgpl.html.
 **
 **
 ** If you have questions regarding the use of this file, please contact
 ** Ushahidi developers at team@ushahidi.com.
 **
 *****************************************************************************/

package org.addhen.smssync.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.addhen.smssync.BuildConfig;
import org.addhen.smssync.Prefs;
import org.addhen.smssync.R;
import org.addhen.smssync.activities.MessagesTabActivity;
import org.addhen.smssync.receivers.ConnectivityChangedReceiver;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

/**
 * This class habours common util methods that are available for other classes
 * to use.
 * 
 * @author eyedol
 */
public class Util {

    public static final int NOTIFICATION_ALERT = 1337;

    public static final int READ_THREAD = 1;

    public static HashMap<String, String> smsMap = new HashMap<String, String>();

    private static NetworkInfo networkInfo;

    private static JSONObject jsonObject;

    private static Pattern pattern;

    private static Matcher matcher;

    private static final String TIME_FORMAT_12_HOUR = "h:mm a";

    private static final String TIME_FORMAT_24_HOUR = "H:mm";

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private static final String URL_PATTERN = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

    private static final int NOTIFY_RUNNING = 100;

    private static final String CLASS_TAG = Util.class.getSimpleName();

    /**
     * Joins two strings together.
     * 
     * @param String first - The first String to be joined to a second string.
     * @param String second - The second String to join to the first string.
     * @return String
     */
    public static String joinString(String first, String second) {
        return first.concat(second);
    }

    /**
     * Converts a string into an int value.
     * 
     * @param String value - The string to be converted into int value.
     * @return int
     */
    public static int toInt(String value) {
        return Integer.parseInt(value);
    }

    /**
     * Capitalize any String given to it.
     * 
     * @param String text - The string to be capitalized.
     * @return String
     */
    public static String capitalizeString(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    /**
     * Create CSV from a given string in a Vector object.
     * 
     * @param Vector <String> text - The Vector object containing the Strings
     * @return String
     */
    public static String implode(Vector<String> text) {

        String implodedStr = "";

        int i = 0;

        for (String value : text) {
            implodedStr += i == text.size() - 1 ? value : value + ",";
            i++;
        }

        return implodedStr;
    }

    /**
     * Checks if there is Internet connection or data connection on the device.
     * 
     * @param Context context - The activity calling this method.
     * @return boolean
     */
    public static boolean isConnected(Context context) {

        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        networkInfo = connectivity.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;

    }

    /**
     * Limit a string to a defined length.
     * 
     * @param int limit - the total length.
     * @param string limited - the limited string.
     * @return String
     */
    public static String limitString(String value, int length) {
        StringBuilder buf = new StringBuilder(value);
        if (buf.length() > length) {
            buf.setLength(length);
            buf.append(" ...");
        }
        return buf.toString();
    }

    /**
     * Format date into more human readable format.
     * 
     * @param date - The date to be formatted.
     * @return String
     */
    public static String formatDate(String dateFormat, String date,
            String toFormat) {

        String formatted = "";

        DateFormat formatter = new SimpleDateFormat(dateFormat);
        try {
            Date dateStr = formatter.parse(date);
            formatted = formatter.format(dateStr);
            Date formatDate = formatter.parse(formatted);
            formatter = new SimpleDateFormat(toFormat);
            formatted = formatter.format(formatDate);

        } catch (ParseException e) {

            e.printStackTrace();
        }
        return formatted;
    }

    /**
     * Get true/false status of JSON payload "success"
     * 
     * @param json_data - The JSON string.
     * @return boolean - value of "success" JSON parameter
     */
    public static boolean getJsonSuccessStatus(String json_data) {
        Log.i(CLASS_TAG, "getJsonSuccessStatus(): Extracting payload JSON data"
                + json_data);
        try {

            jsonObject = new JSONObject(json_data);
            return jsonObject.getJSONObject("payload").getBoolean("success");

        } catch (JSONException e) {
            Log.e(CLASS_TAG, "JSONException: " + e.getMessage());
            return false;
        }

    }

    /**
     * Get JSON payload "error" string
     * 
     * @param json_data - The JSON string.
     * @return string - value of "error" JSON parameter
     */
    public static String getJsonError(String json_data) {
        Log.i(CLASS_TAG, "getJsonError(): Extracting payload JSON data"
                + json_data);
        try {

            jsonObject = new JSONObject(json_data);
            return jsonObject.getJSONObject("payload").getString("error");

        } catch (JSONException e) {
            // Could not find "error" in JSON response
            Log.e(CLASS_TAG, "JSONException: " + e.getMessage());
            return "";
        }

    }

    /**
     * Show toast (int version)
     * 
     * @param context - the application's context
     * @param resId - ID of string resource
     * @return void
     */
    public static void showToast(Context context, int resId) {
        int duration = Toast.LENGTH_LONG;
        Toast.makeText(context, resId, duration).show();
    }

    /**
     * Show toast (string version)
     * 
     * @param context - the application's context
     * @param text - message to display in toast pop-up
     */
    public static void showToast(Context context, String text) {
        int duration = Toast.LENGTH_LONG;
        Toast.makeText(context, text, duration).show();
    }

    /**
     * Show notification
     */
    public static void showNotification(Context context) {

        Intent baseIntent = new Intent(context, MessagesTabActivity.class);
        baseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                baseIntent, 0);

        buildNotification(context, R.drawable.icon,
                context.getString(R.string.notification_summary),
                context.getString(R.string.app_name), pendingIntent, true);

    }

    /**
     * Show a notification
     * 
     * @param String message to display
     * @param String notification title
     */
    public static void showFailNotification(Context context, String message,
            String notificationTitle) {

        Intent baseIntent = new Intent(context, MessagesTabActivity.class);
        baseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                baseIntent, 0);

        buildNotification(context, R.drawable.icon, message, notificationTitle,
                pendingIntent, false);

    }

    /**
     * Build notification info
     * 
     * @param context The calling activity
     * @param drawable The notification icon
     * @param message The message
     * @param title The title for the notification
     * @param intent The pending intent
     * @param ongoing True if you don't want the user to clear the notification
     */
    public static void buildNotification(Context context, int drawable,
            String message, String title, PendingIntent intent, boolean ongoing) {

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context);
        builder.setContentTitle(title);
        builder.setContentText(message);
        builder.setSmallIcon(drawable);
        builder.setContentIntent(intent);

        if (ongoing) {
            builder.setOngoing(ongoing);
        }

        notificationManager.notify(NOTIFY_RUNNING, builder.getNotification());
    }

    /**
     * Validates an email address Credits:
     * http://www.mkyong.com/regular-expressions
     * /how-to-validate-email-address-with-regular-expression/
     * 
     * @param String - email address to be validated
     * @return boolean
     */
    public static boolean validateEmail(String emailAddress) {
        Log.i(CLASS_TAG, "validateEmail(): Validate Email address");
        if (!emailAddress.equals("")) {
            pattern = Pattern.compile(EMAIL_PATTERN);
            matcher = pattern.matcher(emailAddress);
            return matcher.matches();
        }
        return false;
    }

    /**
     * Clear the standard notification alert.
     * 
     * @param Context context - The context of the calling activity.
     * @return void
     */
    public static void clear(Context context) {
        clearAll(context);
    }

    /**
     * Clear all notifications shown to the user.
     * 
     * @param Context context - The context of the calling activity.
     * @return void.
     */
    public static void clearAll(Context context) {
        NotificationManager myNM = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        myNM.cancelAll();
    }

    /**
     * Clear a running notification.
     * 
     * @param Context context - The context of the calling activity.
     * @return void
     */
    public static void clearNotify(Context context) {
        NotificationManager myNM = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        myNM.cancel(NOTIFY_RUNNING);
    }

    /**
     * Makes an attempt to connect to a data network.
     */
    public static void connectToDataNetwork(Context context) {
        // Enable the Connectivity Changed Receiver to listen for
        // connection to a network so we can send pending messages.
        PackageManager pm = context.getPackageManager();
        ComponentName connectivityReceiver = new ComponentName(context,
                ConnectivityChangedReceiver.class);
        pm.setComponentEnabledSetting(connectivityReceiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    /**
     * Format an Unix timestamp to a string suitable for display to the user
     * according to their system settings (12 or 24 hour time).
     * 
     * @param Context context - The context of the calling activity.
     * @param long timestamp - The human unfriendly timestamp.
     * @return String
     */
    public static String formatTimestamp(Context context, long timestamp) {
        String HOURS_24 = "24";
        String hours = "24";

        SimpleDateFormat mSDF = new SimpleDateFormat();
        if (HOURS_24.equals(hours)) {
            mSDF.applyLocalizedPattern(TIME_FORMAT_24_HOUR);
        } else {
            mSDF.applyLocalizedPattern(TIME_FORMAT_12_HOUR);
        }
        return mSDF.format(new Date(timestamp));
    }

    /**
     * Validate the callback URL
     * 
     * @param String callbackURL - The callback URL to be validated.
     * @return int - 0 = well formed URL, 1 = no configured url, 2 = Malformed
     *         URL - 3 = can't make connection to it.
     */
    public static int validateCallbackUrl(String callbackUrl) {

        if (TextUtils.isEmpty(callbackUrl)) {
            return 1;
        }

        pattern = Pattern.compile(URL_PATTERN);
        matcher = pattern.matcher(callbackUrl);
        if (matcher.matches()) {
            return 0;
        }
        return 1;

    }

    /**
     * For debugging purposes. Append content of a string to a file
     * 
     * @param text
     */
    public static void appendLog(String text) {
        File logFile = new File(Environment.getExternalStorageDirectory(),
                "smssync.txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            // BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
                    true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String getPhoneNumber(Context context) {

        if (!TextUtils.isEmpty(Prefs.uniqueId)) {
            return Prefs.uniqueId;
        }
        return "";

    }

    public static String formatDateTime(long milliseconds, String dateTimeFormat) {
        final Date date = new Date(milliseconds);
        try {
            if (date != null) {
                SimpleDateFormat submitFormat = new SimpleDateFormat(
                        dateTimeFormat);
                return submitFormat.format(date);
            }
        } catch (IllegalArgumentException e) {
            new Util().log("IllegalArgumentException", e);
        }
        return null;
    }

    /**
     * Setup strict mode for threading when app is debug built. This is good for
     * debugging features that use heavy threading.
     */
    @TargetApi(11)
    @SuppressWarnings({
            "ConstantConditions", "PointlessBooleanExpression"
    })
    public static void setupStrictMode() {
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= 11) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyFlashScreen()
                    .build());
        }
    }

    public void log(String message) {
        Logger.log(getClass().getName(), message);
    }

    public void log(String format, Object... args) {

        Logger.log(getClass().getName(), String.format(format, args));
    }

    public void log(String message, Exception ex) {

        Logger.log(getClass().getName(), message, ex);
    }
}
