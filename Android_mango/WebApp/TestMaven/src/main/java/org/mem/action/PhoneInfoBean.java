package org.mem.action;

public class PhoneInfoBean {
	private String version;
	private String Brand;
	private String Model;
	private int interMem;
	private int sdCardMem;
	private String phoneNum;
	
	public String getPhoneNum() {
		return phoneNum;
	}
	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getBrand() {
		return Brand;
	}
	public void setBrand(String brand) {
		Brand = brand;
	}
	public String getModel() {
		return Model;
	}
	public void setModel(String model) {
		Model = model;
	}
	public int getInterMem() {
		return interMem;
	}
	public void setInterMem(int interMem) {
		this.interMem = interMem;
	}
	public int getSdCardMem() {
		return sdCardMem;
	}
	public void setSdCardMem(int sdCardMem) {
		this.sdCardMem = sdCardMem;
	}
}
