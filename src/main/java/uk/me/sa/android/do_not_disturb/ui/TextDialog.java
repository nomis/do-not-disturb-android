/*
	do-not-disturb-android - Android Do not Disturb Service

	Copyright 2017  Simon Arlott

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.me.sa.android.do_not_disturb.ui;

import uk.me.sa.android.do_not_disturb.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextUtils.TruncateAt;
import android.text.TextWatcher;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class TextDialog {
	private final AlertDialog alertDialog;
	private final EditText editText;
	private final TextView errorMessage;

	public TextDialog(Context context, int titleId, String initialValue, int hintId) {
		final LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		int dialogPadding = context.getResources().getDimensionPixelSize(R.dimen.dialogPadding);
		layout.setPadding(dialogPadding, layout.getPaddingTop(), dialogPadding, layout.getPaddingBottom());

		editText = new EditText(context);
		editText.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		editText.setSingleLine(true);
		layout.addView(editText);

		errorMessage = new TextView(context);
		errorMessage.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		errorMessage.setSingleLine(true);
		errorMessage.setEllipsize(TruncateAt.MARQUEE);
		errorMessage.setTextColor(context.getColor(android.R.color.holo_red_light));
		layout.addView(errorMessage);

		alertDialog = new AlertDialog.Builder(context).setView(layout).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				new AsyncTask<String, Void, Boolean>() {
					@Override
					protected Boolean doInBackground(String... params) {
						return saveText(params[0]);
					}

					@Override
					protected void onPostExecute(Boolean result) {
						if (result) {
							onSuccess();
						}
					}
				}.execute(editText.getText().toString());
			}
		}).setNegativeButton(android.R.string.cancel, null).create();

		alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				onTextChanged(editText.getText());
			}
		});

		editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				TextDialog.this.onTextChanged(s);
			}
		});

		alertDialog.setTitle(titleId);
		editText.setText(initialValue);
		editText.setHint(hintId);

		alertDialog.create();
		alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
		alertDialog.show();
	}

	private void onTextChanged(Editable text) {
		new AsyncTask<String, Void, Integer>() {
			@Override
			protected Integer doInBackground(String... params) {
				return checkText(params[0]);
			}

			@Override
			protected void onPostExecute(Integer result) {
				if (result == null) {
					setValid();
				} else {
					setInvalid(result);
				}
			}
		}.execute(text.toString());
	}

	private void setValid() {
		alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
		errorMessage.setText(null);
	}

	private void setInvalid(int errorText) {
		alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
		errorMessage.setText(errorText);
	}

	/**
	 * Executed on a background thread to check the text.
	 * 
	 * @param value
	 *            text to be checked
	 * @return string resource to display if the text is invalid
	 */
	abstract Integer checkText(String value);

	/**
	 * Executed on a background thread to save the text
	 * 
	 * @param value
	 *            text to be saved
	 * 
	 * @return true if the text is valid and has been saved
	 */
	abstract boolean saveText(String value);

	/**
	 * Executed on the UI thread if the text has been saved
	 */
	abstract void onSuccess();
}
