package edu.project.hoodwatch;

/*
 * used to handle api expection message passing when encountered.
 */
public class ApiException extends Exception {
	private static final long serialVersionUID = 1L;

	public ApiException(String msg) {
		super(msg);
	}

	public ApiException(String msg, Throwable thr) {
		super(msg, thr);
	}
}
