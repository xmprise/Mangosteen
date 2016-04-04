package org.mem.action;

public class PerFolderBean {
	private String folderName;
	private int fileOfNum;
	private String thumbPath;
	private String type;
	private String folderPath;
	
	public String getFolderPath() {
		return folderPath;
	}
	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getFolderName() {
		return folderName;
	}
	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}
	public int getFileOfNum() {
		return fileOfNum;
	}
	public void setFileOfNum(int fileOfNum) {
		this.fileOfNum = fileOfNum;
	}
	public String getThumbPath() {
		return thumbPath;
	}
	public void setThumbPath(String thumbPath) {
		this.thumbPath = thumbPath;
	}
	
}
