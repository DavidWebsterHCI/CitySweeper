package edu.project.hoodwatch;

/*
 * an interface for communication between client and server
 */

public interface SubmitReportTaskDelegate {

	public void reportWillSubmit();
	
	public void reportDidSubmit();
}
