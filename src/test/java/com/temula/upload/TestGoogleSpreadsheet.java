package com.temula.upload;

import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import com.sun.jersey.api.client.ClientResponse;
import com.temula.location.Space;
import com.temula.location.SpaceParser;

public class TestGoogleSpreadsheet extends TestCase {
	static final Logger logger = Logger.getLogger(GoogleSpreadsheet.class);

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
