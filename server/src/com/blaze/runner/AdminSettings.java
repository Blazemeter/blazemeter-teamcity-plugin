package com.blaze.runner;

import java.io.*;
import java.util.Properties;

import jetbrains.buildServer.serverSide.ServerPaths;


public class AdminSettings {
	public ServerPaths serverPaths;
	private String userKey = "";
	private String blazeMeterUrl = "";

	public AdminSettings(ServerPaths serverPaths){
		this.serverPaths = serverPaths;
	}
	
	public void init(){
		loadProperties();
	}

	public void saveProperties(){
		File keyFile=propFile();
		FileWriter outFile=null;
		try {
			Properties prop=new Properties();
			outFile = new FileWriter(keyFile);
			prop.put("user_key",this.userKey);
			prop.put("blazeMeterUrl",this.blazeMeterUrl);
		    prop.store(outFile,null);
//			outFile.close();
		} catch (FileNotFoundException e) {
			System.out.println("Cannot save configuration: "+e.getMessage());
		} catch (IOException e) {
			System.out.println("Cannot save configuration: "+e.getMessage());
		} catch (Exception e) {
			System.out.println("Cannot save configuration: "+e.getMessage());
		}finally {
			try {
				outFile.close();
			} catch (IOException e) {
				System.out.println("Cannot close "+keyFile.getAbsolutePath()+": "+e.getMessage());
			}

		}
	}

	public void loadProperties() {
		File keyFile = propFile();
		FileReader inFile=null;
		try {
			inFile = new FileReader(keyFile);

			Properties prop=new Properties();
			prop.load(inFile);
			this.userKey=prop.getProperty("user_key");
			this.blazeMeterUrl=prop.getProperty("blazeMeterUrl");
			inFile.close();
		} catch (FileNotFoundException e) {
			System.out.println("Cannot load configuration: "+e.getMessage());
		} catch (IOException e) {
			System.out.println("Cannot load configuration: "+e.getMessage());
		}finally {
			try {
				inFile.close();
			} catch (IOException e) {
				System.out.println("Cannot close "+keyFile.getAbsolutePath()+": "+e.getMessage());
			}

		}
	}

	private File propFile(){
		File keyFile = new File(serverPaths.getConfigDir()+Constants.BZM_PROPERTIES_FILE);
		if (!keyFile.exists()){
			try {
				boolean created = keyFile.createNewFile();
				if (!created){
					System.out.println("Cannot create configuration file, file already exists!");
				}
			} catch (IOException e) {
				System.out.println("Cannot create configuration file! Error is: "+e.getMessage());
				return null;
			}
		}
		return keyFile;
	}


	public String getUserKey() {
		return userKey;
	}

	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}


    public String getBlazeMeterUrl() {
        return blazeMeterUrl;
    }

    public void setBlazeMeterUrl(String blazeMeterUrl) {
        this.blazeMeterUrl = blazeMeterUrl;
    }
}
