package com.temula.upload;

import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import com.sun.jersey.api.client.ClientResponse;

public class TestGoogleSpreadsheet extends TestCase {
	static final Logger logger = Logger.getLogger(GoogleSpreadsheet.class);

	
	public void testPropertyMapping(){
		
		ResourceBundle resourceBundle = ResourceBundle.getBundle("googleSpreadsheetMapping");
		Enumeration<String>enum_ = resourceBundle.getKeys();
		
		while(enum_.hasMoreElements()){
			String key = enum_.nextElement();
			String value = resourceBundle.getString(key);
			System.out.println(key+":"+value);
		}
	
	}
	
	public void testRead(){
		GoogleSpreadsheet gu = new GoogleSpreadsheet();
		try{
			List<List<Object>>objects = gu.read();
			assertNotNull(objects);
			assertTrue(objects.size()>0);
			for(int h=0;h<objects.size();h++){
				ClientResponse.Status status = gu.post(objects.get(h));
				assert(status.equals(ClientResponse.Status.OK));
				
			}
		}
		catch(Exception e){
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}
	
	
}
