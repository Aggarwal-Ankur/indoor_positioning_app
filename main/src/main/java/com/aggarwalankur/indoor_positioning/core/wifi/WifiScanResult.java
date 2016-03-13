package com.aggarwalankur.indoor_positioning.core.wifi;

public class WifiScanResult {
	public String bssid;
	public String ssid;
	public int frequency;
	public int level;
	public String capabilities;
	
	public WifiScanResult(String bssid, String ssid, int frequency, int level , String capabilities){
		this.bssid = bssid;
		this.ssid = ssid;
		this.frequency = frequency;
		this.level = level;
		this.capabilities = capabilities;
	}

	public WifiScanResult() {
	}
}
