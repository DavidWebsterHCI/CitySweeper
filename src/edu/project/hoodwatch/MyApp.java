package edu.project.hoodwatch;

/*
 * application entry point of sorts: contains global variables address formatting
 */
import java.net.URL;

import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.location.Address;
import android.os.AsyncTask;

public class MyApp extends Application {
	
	public static final String MY_ISSUES = "my_issues";
	public static final String FOLLOWED_ISSUES = "followed_issues";
	public static final String SETTINGS = "settings";
	public static final String HOME = "home";
	public static final String NEW_REPORT = "new_report";
	
	
	public static final String PREFS_NAME = "edu.project.hoodwatch.prefs";
	public static final String API_KEY = "api_key";
	public static final String LARGE_IMAGE_FILENAME = "large_image.png";
	public static final String USER_IMAGE_FILENAME = "user_image.png";
	public static AsyncTask<URL, Integer, Bitmap> imageTask;


	
	//                        * USER INFO *
	///////////////////////////////////////////////////////////////////
	public static final String USER_ID_KEY = "user_id";
	public static final String USER_TOKEN_KEY = "token";
	public static final String USER_DISPLAYNAME_KEY = "displayname";
	public static final String USER_EMAIL_KEY = "email";
	public static final String USER_PASSWORD_KEY = "password"; 
	///////////////////////////////////////////////////////////////////

	public static String formatAddress(Address address) {
		boolean first = true;
		StringBuilder sbAddress = new StringBuilder();
		int lastAddressLineIndex = address.getMaxAddressLineIndex();
		for (int i = 0; i < lastAddressLineIndex; i++) {
			if (first) {
				first = false;
				sbAddress.append(address.getAddressLine(i));
			} else {
				sbAddress.append(", ");
				sbAddress.append(address.getAddressLine(i));
			}
		}
		return sbAddress.toString();
	}
	
	
}
