package edu.project.hoodwatch;

/*
 *The three classes "ListOfIssuesTaskxxxx.java" all work in coordination in order to allow for a list of
 *issues to be displayed to the user in the two cases list views are implemented.
 */
public interface ListOfIssuesTaskDelegate {
	
	public void taskWillExecute();
	
	public void taskDidExecute(ListOfIssuesTaskResponse data);

}
