package io.github.emmrida.chat4ussetup.util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
	private static final String BUNDLE_NAME = Messages.class.getPackageName() + ".messages"; //$NON-NLS-1$

	private Messages() {
	}

	public static String getString(String key) {
		try {
			//return RESOURCE_BUNDLE.getString(key);
			return ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault()).getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
