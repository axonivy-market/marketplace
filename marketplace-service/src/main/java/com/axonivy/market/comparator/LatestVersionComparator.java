package com.axonivy.market.comparator;

import java.util.Comparator;

public class LatestVersionComparator implements Comparator<String> {

	@Override
	public int compare(String v1, String v2) {
		// Split by "."
		String[] parts1 = v1.split("\\.");
		String[] parts2 = v2.split("\\.");

		// Compare up to the shorter length
		int length = Math.min(parts1.length, parts2.length);
		for (int i = 0; i < length; i++) {
			try {
				int num1 = Integer.parseInt(parts1[i]);
				int num2 = Integer.parseInt(parts2[i]);
				// Return difference for numeric parts
				if (num1 != num2) {
					return num2 - num1;
				}
				// Handle non-numeric parts (e.g., "m229")
			} catch (NumberFormatException e) {
				return parts2[i].replaceAll("\\D", "").compareTo(parts1[i].replaceAll("\\D", ""));
			}
		}

		// Versions with more parts are considered larger
		return parts2.length - parts1.length;
	}
}
