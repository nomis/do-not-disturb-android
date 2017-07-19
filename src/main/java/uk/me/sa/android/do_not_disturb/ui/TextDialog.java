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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils.TruncateAt;
import android.text.TextWatcher;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import uk.me.sa.android.do_not_disturb.R;

public abstract class TextDialog {
	private final AlertDialog alertDialog;
	private final EditText editText;
	private final TextView errorMessage;

	public TextDialog(Context context, int titleId, String initialValue, int hintId) {
		final LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		layout.setPadding(R.dimen.dialogPadding, R.dimen.dialogPadding, R.dimen.dialogPadding, R.dimen.dialogPadding);

		editText = new EditText(context);
		MarginLayoutParams editTextMargins = new MarginLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		editTextMargins.bottomMargin = R.dimen.innerPadding;
		editText.setLayoutParams(editTextMargins);
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
				final String text = editText.getText().toString();
				new Handler().post(new Runnable() {
					public void run() {
						onSuccess(text);
					}
				});
			}
		}).setNegativeButton(android.R.string.cancel, null).create();

		alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				final String text = editText.getText().toString();
				new Handler().post(new Runnable() {
					public void run() {
						onTextChanged(text);
					}
				});
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
				final String text = s.toString();
				new Handler().post(new Runnable() {
					public void run() {
						TextDialog.this.onTextChanged(text);
					}
				});
			}
		});

		alertDialog.setTitle(titleId);
		editText.setText(initialValue);
		editText.setHint(hintId);

		alertDialog.create();
		alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
		alertDialog.show();
	}

	protected void setValid(boolean valid) {
		alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(valid);
	}

	protected void setErrorMessage(CharSequence value) {
		errorMessage.setText(value);
	}

	abstract void onTextChanged(String value);

	abstract void onSuccess(String value);
}
