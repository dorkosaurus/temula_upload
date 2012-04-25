package com.temula.upload;


import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.jersey.api.client.ClientResponse;

public class GoogleSpreadSheetServlet extends HttpServlet {
	static final Logger logger = Logger.getLogger(GoogleSpreadsheet.class.getName());

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {
		logger.info("entering POST method");
		GoogleSpreadsheet gu = new GoogleSpreadsheet();
		
		try{
			
			List<List<Object>>objects = gu.read();
			
			for(int h=0;h<objects.size();h++){
				ClientResponse.Status status = gu.post(objects.get(h));

				
				if(status!=null && status.equals(ClientResponse.Status.OK)){
					String uri="http://templereservation.appspot.com/location/space";
					printSuccess(response.getOutputStream());
				}
				else{
					printError(response.getOutputStream(),new Exception("Server responded with response code "+status));
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
			printError(response.getOutputStream(),e);
		}
	}
	private void printSuccess(OutputStream out) throws IOException{
		String msg =
			"<html><head>" +
			"<title>Successful upload</title>" +
			"</head>" +
			"<body><a href='http://templereservation.appspot.com/location/space'>Click here to view the upload</a></body></html>";
		out.write(msg.getBytes());
		out.flush();
	}
	
	
	private void printError(OutputStream out,Exception e) throws IOException{
		StringBuffer error=new StringBuffer(e.getMessage()+"\n");
		for(StackTraceElement elem:e.getStackTrace()){
			error.append(elem.getClassName()+":"+elem.getMethodName()+":"+elem.getLineNumber()+"\n");
		}

		
		String msg =
			"<html><head>" +
			"<title>Error loading google spreadsheet</title>" +
			"</head>" +
			"<body><pre>"+error.toString()+"</pre></body></html>";
		out.write(msg.getBytes());
		out.flush();
	}
}
