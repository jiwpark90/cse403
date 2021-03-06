package com.example.budgetmanager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.budgetmanager.api.ApiCallback;
import com.example.budgetmanager.api.ApiInterface;

/**
 * Activity which displays a Register screen to the user, offering registration as
 * well.
 *
 * @author Chi Ho coldstar96
 */
public class RegisterActivity extends Activity {
	private final int MIN_PASS_LENGTH = 4;

	// Values for local storage
	private static final String PREFS_EMAIL = "email";
	private static final String PREFS_PASS = "password";

	// Values for email and password at the time of the register attempt.
	private String mEmail;
	private String mPassword;
	private String mPasswordCheck;

	// UI references.
	private EditText mEmailView;
	private EditText mPasswordView;
	private EditText mPasswordCheckView;
	private View mRegisterFormView;
	private View mRegisterStatusView;
	private TextView mRegisterStatusMessageView;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_register);

		// Set up the register form.
		mEmail = getIntent().getExtras().getString("email");
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(mEmail);

		mPassword = getIntent().getExtras().getString("password");
		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setText(mPassword);
		mPasswordView.setOnEditorActionListener(
				new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.register || id == EditorInfo.IME_NULL) {
							registerAttempt(textView);
							return true;
						}
						return false;
					}
				});

		mPasswordCheck = "";
		mPasswordCheckView = (EditText) findViewById(R.id.password2);
		mPasswordCheckView.setText(mPasswordCheck);
		mPasswordCheckView.setOnEditorActionListener(
				new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.register || id == EditorInfo.IME_NULL) {
							registerAttempt(textView);
							return true;
						}
						return false;
					}
				});

		mRegisterFormView = findViewById(R.id.register_form);
		mRegisterStatusView = findViewById(R.id.register_status);
		mRegisterStatusMessageView = (TextView) findViewById(R.id.register_status_message);

		// focuses to empty edit view
		if (mEmail.isEmpty()) {
			mEmailView.requestFocus();
		} else if (mPassword.isEmpty()) {
			mPasswordView.requestFocus();
		} else {
			mPasswordCheckView.requestFocus();
		}
	}

	/**
	 * Attempts to register and sign in the account specified by the register form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual register attempt is made.
	 */
	public void registerAttempt(View view) {
		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);
		mPasswordCheckView.setError(null);

		// Store values at the time of the register attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();
		mPasswordCheck = mPasswordCheckView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < MIN_PASS_LENGTH) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a password match.
		if (TextUtils.isEmpty(mPasswordCheck)) {
			mPasswordCheckView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (!mPassword.equals(mPasswordCheck)) {
			mPasswordCheckView.setError(getString(R.string.error_no_match_password));
			focusView = mPasswordCheckView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!mEmail.contains("@") || !mEmail.contains(".") || mEmail.contains(" ")) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt register and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user register attempt.
			mRegisterStatusMessageView.setText(R.string.register_progress_signing_in);
			getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			showProgress(true);
			ApiInterface.getInstance().createUser(mEmail, mPassword, new ApiCallback<Object>() {
				// Create popup dialog failure
				@Override
				public void onFailure(String errorMessage) {
					showProgress(false);
					Toast.makeText(RegisterActivity.this,
							errorMessage, Toast.LENGTH_LONG).show();
				}

				// Move to entry log activity
				@Override
				public void onSuccess(Object result) {
					Intent intent = new Intent(RegisterActivity.this, MainActivity.class);

					SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();

					editor.putString(PREFS_EMAIL, mEmail);
					editor.putString(PREFS_PASS, mPassword);
					editor.commit();

					setResult(2);

					showProgress(false);
					startActivity(intent);
					finish();
				}
			});
		}
	}

	/**
	 * Shows the progress UI and hides the register form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		InputMethodManager imm = (InputMethodManager) getSystemService(
				Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mEmailView.getWindowToken(), 0);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mRegisterStatusView.setVisibility(View.VISIBLE);
			mRegisterStatusView.animate().setDuration(shortAnimTime)
			.alpha(show ? 1 : 0)
			.setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mRegisterStatusView.setVisibility(show ? View.VISIBLE
							: View.GONE);
				}
			});

			mRegisterFormView.setVisibility(View.VISIBLE);
			mRegisterFormView.animate().setDuration(shortAnimTime)
			.alpha(show ? 0 : 1)
			.setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mRegisterFormView.setVisibility(show ? View.GONE
							: View.VISIBLE);
				}
			});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mRegisterStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
}
