package com.blaze.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import com.blaze.api.BlazemeterApi;

public class APITest {
	private static String USER_KEY = "5a8da32f36036f8c29fe";
	public APITest(){
		
	}
	
	public void retrieveTest(){
		BlazemeterApi bAPI = new BlazemeterApi(null, -1, null, null);
		try {
			HashMap<String, String> tests = bAPI.getTestList(USER_KEY);
			
			Iterator<String> keySet = tests.keySet().iterator();
			while (keySet.hasNext()){
				String key = keySet.next();
				String value = tests.get(key);
				
				System.out.println("Key:"+key+" Value:"+value);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public static void main(String[] args){
		APITest test = new APITest();
		test.retrieveTest();
	}
}
