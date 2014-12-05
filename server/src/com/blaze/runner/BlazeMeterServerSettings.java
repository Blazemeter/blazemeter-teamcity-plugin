package com.blaze.runner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import jetbrains.buildServer.serverSide.ServerPaths;


public class BlazeMeterServerSettings {
	public ServerPaths serverPaths;
	private String userKey = "";
	private String blazeMeterUrl = "";
	private String serverName = "";
	private String serverPort = "";
	private String username = "";
	private String password = "";
	
	public BlazeMeterServerSettings(ServerPaths serverPaths){
		this.serverPaths = serverPaths;
	}
	
	public void init(){
		loadProperties();
	}
	
	public void loadProperties() {
//		System.out.println("BlazeMeter load propertis from " + serverPaths.getConfigDir()+"/userKeyFile.properties");
		File keyFile = new File(serverPaths.getConfigDir()+"/userKeyFile.properties");
		if (!keyFile.exists()){
    		try {
				boolean created = keyFile.createNewFile();
				if (!created){
//					System.out.println("Cannot create BlazeMeter configuration file, file already exists!");
				}
			} catch (IOException e) {
				System.out.println("Cannot create BlazeMeter configuration file! Error is: "+e.getMessage());
				return ;
			}
		}
		FileReader inFile;
		try {
			inFile = new FileReader(keyFile);
			Properties prop = new Properties();
			prop.load(inFile);
			this.setUserKey(prop.getProperty("user_key"));
			this.setBlazeMeterUrl(prop.getProperty("blazeMeterUrl"));
			this.setServerName(prop.getProperty("serverName"));
			this.setServerPort(prop.getProperty("serverPort"));
			this.setUsername(prop.getProperty("username"));
			this.setPassword(prop.getProperty("password"));
			inFile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getUserKey() {
		return userKey;
	}

	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getServerPort() {
		return serverPort;
	}

	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

    public String getBlazeMeterUrl() {
        return blazeMeterUrl;
    }

    public void setBlazeMeterUrl(String blazeMeterUrl) {
        this.blazeMeterUrl = blazeMeterUrl;
    }
}
