package com.temula.upload;

import java.util.List;

import org.apache.log4j.Logger;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

import junit.framework.TestCase;

public class TestGoogleSpreadsheet extends TestCase {
	static final Logger logger = Logger.getLogger(GoogleSpreadsheet.class);

	public void testRead(){
		GoogleSpreadsheet gu = new GoogleSpreadsheet();
		try{
			List<List<Object>>objects = gu.read();
			assertNotNull(objects);
			assertTrue(objects.size()>0);
			for(List<Object>list:objects){
				ClientResponse.Status  status = gu.post(list);
				assertNotNull(status);
				assertTrue(status.equals(Status.OK));
			}
		}
		catch(Exception e){
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}
	
	
}
