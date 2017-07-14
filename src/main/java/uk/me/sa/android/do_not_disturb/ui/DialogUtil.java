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
import android.widget.EditText;

public class DialogUtil {
	private DialogUtil() {
	}

	public interface InputTextListener {
		public void onInputText(String value);
	}

	public static void inputText(Context context, int titleId, String initialValue, int hintId, final InputTextListener listener) {
		final EditText editText = new EditText(context);
		final AlertDialog dialog = new AlertDialog.Builder(context).setTitle(titleId).setView(editText)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (listener != null) {
							String text = editText.getText().toString();

							if (TextUtils.getTrimmedLength(text) > 0) {
								listener.onInputText(text);
							}
						}
					}
				}).setNegativeButton(android.R.string.cancel, null).create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
			}
		});

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
				dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(TextUtils.getTrimmedLength(s) > 0);
			}
		});

		dialog.show();
	}
}
