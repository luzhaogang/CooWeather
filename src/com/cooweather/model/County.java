package com.cooweather.model;

public class County {
	private int id;
	private String CountyName;
	private String CountyCode;
	private int cityId;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCountyName() {
		return CountyName;
	}

	public void setCountyName(String countyName) {
		CountyName = countyName;
	}

	public String getCountyCode() {
		return CountyCode;
	}

	public void setCountyCode(String countyCode) {
		CountyCode = countyCode;
	}

	public int getCityId() {
		return cityId;
	}

	public void setCityId(int cityId) {
		this.cityId = cityId;
	}
}
