package com.nilhcem.frcndict;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.nilhcem.frcndict.ApplicationController;
import com.nilhcem.frcndict.R;
import com.nilhcem.frcndict.about.AboutDialog;
import com.nilhcem.frcndict.core.AbstractDictActivity;
import com.nilhcem.frcndict.search.SearchActivity;
import com.nilhcem.frcndict.search.SearchService;
import com.nilhcem.frcndict.settings.OnPreferencesChangedListener;
import com.nilhcem.frcndict.settings.SettingsActivity;
import com.nilhcem.frcndict.starred.StarredActivity;

public final class DashboardActivity extends AbstractDictActivity {
	
	private ImageButton mDictButton;
	
	public static final int BACK_TO_EXIT_TIMER = 4000;
	private long mLastBackPressTime = 0l;
	private Toast mPressBackTwiceToast = null;
	private Dialog mAboutDialog = null;

	//@Override
	protected int getLayoutResId() {
		return R.layout.dashboard;
	}


	//@Override
	protected Context getPackageContext() {
		return DashboardActivity.this;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutResId());
		initDictionaryButton();
		if (savedInstanceState != null && (savedInstanceState.getBoolean("about-displayed", false))) {
			createAboutDialog().show();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		mPressBackTwiceToast = null;
		setLastBackPressTime(0l);

		OnPreferencesChangedListener listener = ((ApplicationController) getApplication())
				.getOnPreferencesChangedListener();
		// If theme was changed, restart activity.
		if (listener.hasThemeChanged()) {
			listener.setThemeHasChanged(false);
			Intent intent = getIntent();
			finish();
			startActivity(intent);
		} else if (listener.shouldResultListBeUpdated()) {
			// refresh views after back from settings
			listener.setResultListShouldBeUpdated(false);
		}
	}

	@Override
	protected void onPause() {
		cancelToastIfNotNull(mPressBackTwiceToast);
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		if (isBackBtnPressedForTheFirstTime()) {
			mPressBackTwiceToast = Toast.makeText(this, R.string.search_press_back_twice_exit, BACK_TO_EXIT_TIMER);
			mPressBackTwiceToast.show();
			setLastBackPressTime(System.currentTimeMillis());
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// No call for super(). Bug on API Level > 11
		if (mAboutDialog != null && mAboutDialog.isShowing()) {
			outState.putBoolean("about-displayed", true);
			mAboutDialog.dismiss();
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean stopProcessing = true;

		if (item.getItemId() == R.id.main_menu_about) {
			createAboutDialog().show();
		} else if (item.getItemId() == R.id.main_menu_settings) {
			startActivity(new Intent(this, SettingsActivity.class));
		} else if (item.getItemId() == R.id.main_menu_starred) {
			startActivity(new Intent(this, StarredActivity.class));
		} else {
			stopProcessing = super.onOptionsItemSelected(item);
		}
		return stopProcessing;
	}

	private void initDictionaryButton() {
		mDictButton = (ImageButton) findViewById(R.id.dashboard_dict);
		mDictButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				Intent intent = new Intent(DashboardActivity.this, SearchActivity.class);
				
				startActivity(intent);
			}
		});
	}

	private Dialog createAboutDialog() {
		mAboutDialog = new AboutDialog(this, R.style.AboutDialog);
		return mAboutDialog;
	}

	private void cancelToastIfNotNull(Toast toast) {
		if (toast != null) {
			toast.cancel();
		}
	}
	
	private void setLastBackPressTime(long val)
	{
		mLastBackPressTime = val;
	}
	
	public boolean isBackBtnPressedForTheFirstTime() {
		return (mLastBackPressTime < System.currentTimeMillis() - SearchService.BACK_TO_EXIT_TIMER);
	}
}
