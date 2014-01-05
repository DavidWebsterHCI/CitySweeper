package edu.project.hoodwatch;

/*
 * interface for webservice communications
 */
import org.apache.http.HttpResponse;

public interface WebTaskDelegate { 
	
	public void taskWillExecute();
	
	public void taskDidExecute(HttpResponse response);

}
