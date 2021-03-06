package com.nilhcem.frcndict.about;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;

import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.database.DatabaseHelper;
import com.nilhcem.frcndict.settings.SettingsActivity;
import com.nilhcem.frcndict.utils.Log;

/**
 * Note: if some changes are made to this class, please modify proguard.cfg
 */
/* package-private */
final class JavascriptInterface {
	private static final String VERSION_SEPARATOR = "-";
	private static final String THEME_DEFAULT = "./res/theme-default.css";
	private static final String THEME_DARK = "./res/theme-dark.css";

	private final Context mParentContext;
	private final Dialog mDialog;

	JavascriptInterface(Context parent, Dialog dialog) {
		mParentContext = parent;
		mDialog = dialog;
	}

	public String getAppName() {
		return mParentContext.getString(R.string.app_name);
	}

	public String getAppVersion() {
		String version;
		try {
			PackageInfo pInfo = mParentContext.getPackageManager().getPackageInfo(mParentContext.getPackageName(), 0);
			version = pInfo.versionName;
		} catch (NameNotFoundException ex) {
			Log.e(JavascriptInterface.class.getSimpleName(), ex, "Failed to get version");
			version = "";
		}
		return version;
	}

	public String getDbVersion() {
		DatabaseHelper db = DatabaseHelper.getInstance();
		String version = null;

		if (db.open()) {
			version = db.getDbVersion();
			db.close();
		}
		return convertDbVersionToFormattedDateVersion(version);
	}

	public void closeDialog() {
		mDialog.dismiss();
	}

	public String getTheme() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mParentContext);
		return (prefs.getBoolean(SettingsActivity.KEY_DARK_THEME, false))
			? JavascriptInterface.THEME_DARK : JavascriptInterface.THEME_DEFAULT;
	}

	private String convertDbVersionToFormattedDateVersion(String version) {
		return (version != null && version.length() > 8)
			? new StringBuilder()
			.append(version.substring(0, 4))
			.append(JavascriptInterface.VERSION_SEPARATOR)
			.append(version.substring(4, 6))
			.append(JavascriptInterface.VERSION_SEPARATOR)
			.append(version.substring(6, 8))
			.toString()
			: "";
	}
}
