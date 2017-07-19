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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;

public class DialogUtil {
	private DialogUtil() {
	}

	public interface InputTextListener {
		public void onInputText(String value);
	}

	public static void inputText(Context context, int titleId, String initialValue, int hintId, final InputTextListener listener) {
		final LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		final EditText editText = new EditText(context);
		editText.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		layout.addView(editText);
		final AlertDialog alertDialog = new AlertDialog.Builder(context).setTitle(titleId).setView(layout)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (listener != null) {
							String text = editText.getText().toString();

							if (validateText(text)) {
								listener.onInputText(text);
							}
						}
					}
				}).setNegativeButton(android.R.string.cancel, null).create();

		alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(validateText(editText.getText()));
			}
		});

		editText.setSingleLine(true);
		editText.setText(initialValue);
		editText.setHint(hintId);
		editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(validateText(s));
			}
		});

		alertDialog.show();
	}

	private static boolean validateText(CharSequence value) {
		return TextUtils.getTrimmedLength(value) > 0;
	}
}
