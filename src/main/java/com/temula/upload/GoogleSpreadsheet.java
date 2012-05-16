package com.temula.upload;




import java.lang.reflect.Field;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.temula.StringTemplateProcessor;
public class GoogleSpreadsheet {
	static final Logger logger = Logger.getLogger(GoogleSpreadsheet.class.getName());
	final static String SPREADSHEET_NAME="temula_spaces";
	ResourceBundle resourceBundle = ResourceBundle.getBundle("temulaupload");
	ResourceBundle resourceBundleColumnMap = ResourceBundle.getBundle("googleSpreadsheetMapping");

	public List<List<Object>> read() throws Exception {
		String USERNAME = "temulaupload@gmail.com";
		String PASSWORD = "vivekraj";
		List<List<Object>> objects = new ArrayList<List<Object>>();
		for(int i=0;i<5;i++){

			try{

				SpreadsheetService service = new SpreadsheetService("MySpreadsheetIntegration-v1");
				service.setUserCredentials(USERNAME, PASSWORD);

				// Define the URL to request.  This should never change.
				URL SPREADSHEET_FEED_URL = new URL(
				"https://spreadsheets.google.com/feeds/spreadsheets/private/full");

				// Make a request to the API and get all spreadsheets.
				SpreadsheetFeed feed = service.getFeed(SPREADSHEET_FEED_URL, SpreadsheetFeed.class);
				List<SpreadsheetEntry> spreadsheets = feed.getEntries();

				// Iterate through all of the spreadsheets returned
				SpreadsheetEntry ourSpreadSheet=null;
				for (SpreadsheetEntry spreadsheet : spreadsheets) {
					if(spreadsheet.getTitle().getPlainText().equals(SPREADSHEET_NAME)){
						ourSpreadSheet=spreadsheet;
						break;
					}
				}

				if(ourSpreadSheet==null)throw new Exception("could not find spreadsheet "+SPREADSHEET_NAME);


				for(WorksheetEntry sheet: ourSpreadSheet.getWorksheets()){
					URL cellFeedUrl =  sheet.getCellFeedUrl();

					CellFeed cellFeed = service.getFeed(cellFeedUrl, CellFeed.class);
					String[][]data = new String[cellFeed.getRowCount()][cellFeed.getColCount()];

					for (CellEntry cell : cellFeed.getEntries()) {
						int row = cell.getCell().getRow();
						int col = cell.getCell().getCol();
						int rowIdx = row-1;
						int colIdx = col-1;
						data[rowIdx][colIdx]=cell.getPlainTextContent();
					}
					objects.add(processData(sheet.getTitle().getPlainText(),data));
				}
				break;
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		return objects;
	}

	private List<Object> processData(String sheetName,String[][]data)throws Exception{
		if(sheetName==null)throw new Exception("null sheet name");
		if(sheetName.trim().length()==0)throw new Exception("Sheet name empty string");
		if(data==null)throw new Exception("data null");
		if(data.length==0)throw new Exception("data has no rows");
		if(data[0].length==0)throw new Exception("data has no cols");

		sheetName=sheetName.trim();

		Field[]fields = Class.forName(sheetName).getDeclaredFields();
		Method[]methods = Class.forName(sheetName).getMethods();

		
		List<Object> objects = new ArrayList<Object>();
		int rows = data.length;
		int cols = data[0].length;
		List<Field> fieldList = new ArrayList<Field>(cols);

		List<Method> methodList = new ArrayList<Method>(cols);

		logger.info(""+cols+" columns");
		
		//for each field
		for(Field field:fields){
			String fieldNameLC = field.getName().toLowerCase();

			//for each column header in the spreadsheet.  
			for(int col=0;col<cols;col++){
				String colNameGS = data[0][col];
				if(colNameGS==null || colNameGS.trim().length()==0)continue;

				//map the column header to the field 
				String colName = this.resourceBundleColumnMap.getString(colNameGS);
				if(colName==null || colName.trim().length()==0)continue;
				
				
				String colNameLC = colName.toLowerCase();
				if(colNameLC.equals(fieldNameLC)){
					logger.info(colName+":"+colNameGS+":"+col);

					//save the field when we find a match
					fieldList.add(col, field);

					//save the method to set the field when we find a match
					for(int i=0;i<methods.length;i++){
						String methodName = methods[i].getName();
						String methodNameLC = methodName.toLowerCase();
						if(methodNameLC.equals("set"+fieldNameLC)){
							methodList.add(col, methods[i]);
						}
					}
				}
			}
		}

		for(int row=1;row<rows;row++){
			Object obj = Class.forName(sheetName).newInstance();
			int numAdded=0;
			for(int col=0;col<cols;col++){

				String datum = data[row][col];
				if(datum==null)continue;
				if(datum.trim().length()==0)continue;
				datum=datum.trim();

				Method method = methodList.get(col);
				if(method==null)throw new Exception("data found but could not find a set method for column header "+data[0][col]);

				String className = fieldList.get(col).getType().getName().toLowerCase();
				if(className.equals("boolean")){
					String datumLC = datum.toLowerCase();
					boolean value= ( datumLC.equals("t") || datumLC.equals("y") || datumLC.equals("true") )?true:false;
					Boolean b = new Boolean(value);
					method.invoke(obj, b);
				}
				else if(className.equals("int")){
					Integer itg;
					try{
						itg = new Integer(datum);
					}
					catch(Exception e){
						itg=-1;
					}
					method.invoke(obj, itg);
				}
				else if(className.equals("double")){
					 Double doub = new Double(datum);
					 method.invoke(obj, doub);
				}
				else{
					method.invoke(obj, datum);
				}
				numAdded++;
			}
			if(numAdded>0)objects.add(obj);
		}	
		return objects;
	}

	public ClientResponse.Status  post(List<Object> objects)throws Exception{
		if(objects==null)throw new Exception("null list");
		if(objects.size()==0)throw new Exception("empty list");


		logger.warning("posting "+objects.size()+" objects");

		String className = objects.get(0).getClass().getName();
		String classNameLC = className.toLowerCase();

		//DANGER - ASSUMING com. ARE THE FIRST FOUR CHARACTERS 
		String path = classNameLC.replace('.', '/');
		String templatePath = "/templates/"+path+".stg";

		char TEMPLATE_START_CHAR='^';
		char TEMPLATE_END_CHAR='$';


		String path2File = this.getClass().getResource(templatePath).getPath();
		STGroup g = new STGroupFile(path2File,TEMPLATE_START_CHAR,TEMPLATE_END_CHAR);
		ST st = g.getInstanceOf("list");
		StringTemplateProcessor stp = new StringTemplateProcessor();
		String ret = stp.bind(objects, st, "list");
		try{
			//in case I get a socket exception...total hack for now...
			logger.info("generating the client ");
			Client c  = Client.create();
			logger.info("getting resource ");
			WebResource r=c.resource(getBaseURI());
			c.setConnectTimeout(10*1000);
			logger.info("posting..."+System.currentTimeMillis());
			System.out.println("uri posting to:"+r.path("location/space/").getURI().toString());
			ClientResponse response = r.path("location/space/").type(MediaType.TEXT_HTML).post(ClientResponse.class,ret );
			logger.info("getting status...");

			ClientResponse.Status status = response.getClientResponseStatus();
			logger.info("status="+status);
			return status;
		}
		catch(Exception e){
			logger.warning(""+System.currentTimeMillis()+e.getMessage());
		}
		return null;
	}

	private  URI getBaseURI(){
		return UriBuilder.fromUri(resourceBundle.getString("baseURI")).port(Integer.parseInt(resourceBundle.getString("baseURIPort"))).build();
	}
	
	                                       

}
