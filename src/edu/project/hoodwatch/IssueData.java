package edu.project.hoodwatch;
/*
 * This class contains getters/setters for webservices to interact with the backend server.
 */
import java.io.Serializable;
import java.net.URL;

public class IssueData implements Serializable {
	private static final long serialVersionUID = 1L;
	// var names here need to correspond to var names provided by the web service. 
	private String id;
	private String description;
	private String short_description;
	private String street_address;
	private String latitude;
	private String longitude;
	private String category;
	private String time_ago;
	private String timestamp;
	private String thumb_image_url;
	private String large_image_url;
	private String reported_by;
	
	// fields in dispute:
//	private String num_follows;
//	private String num_comments;

	public IssueData(String serviceName, String expiration, String briefDescription) {
		this.setCategory(serviceName);
		this.setDate(expiration);
		this.setfDescription(briefDescription);
	}

	// ---------------------------------------------------
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	// ---------------------------------------------------
	public String getDate() {
		return time_ago;
	}

	public void setDate(String time) {
		this.time_ago = time;
	}
	
	// ---------------------------------------------------
	public String getTimeStamp() {
		return timestamp;
	}

	public void setTimeStamp(String tstamp) {
		this.timestamp = tstamp;
	}

	// ---------------------------------------------------
	public String getCategory() {
		return category;
	}

	public void setCategory(String serviceName) {
		this.category = serviceName;
	}

	// ---------------------------------------------------
	public String getReportedBy() {
		return reported_by;
	}

	public void setReportedBy(String reported_by) {
		this.reported_by = reported_by;
	}

	// ---------------------------------------------------
	public String getAddress() {
		return street_address;
	}

	public void setAddress(String address) {
		this.street_address = address;
	}

	// ---------------------------------------------------
	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	// ---------------------------------------------------
	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	// ---------------------------------------------------
	public String getDescription() {
		return description;
	}

	public void setfDescription(String description) {
		this.description = description;
	}
	
	// ---------------------------------------------------
	public String getShortDescription() {
		return short_description;
	}

	public void setShortfDescription(String short_description) {
		this.short_description = short_description;
	}

	// ---------------------------------------------------
	public String getThumbURL() {
		return thumb_image_url;
	}

	public void setThumbURL(String thumb_url) {
		this.thumb_image_url = thumb_url;
	}

	// ---------------------------------------------------
	public String getImage_url() {
		return large_image_url;
	}

	public void setImage_url(String image_url) {
		this.large_image_url = image_url;
	}

//	// ---------------------------------------------------
//	public String getNum_follows() {
//		return num_follows;
//	}
//
//	public void setNum_follows(String num_follows) {
//		this.num_follows = num_follows;
//	}
//
//	// ---------------------------------------------------
//	public String getNum_comments() {
//		return num_comments;
//	}
//
//	public void setNum_comments(String num_comments) {
//		this.num_comments = num_comments;
//	}
}
