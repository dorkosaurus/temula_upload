package com.temula;


import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.StringRenderer;


public class StringTemplateProcessor {
	/** these characters denote the start end end of a variable interpolation.  
	 * to make them maximally intuitive, i'm using common regular expression 
	 * syntax to denote start and end*/
	private static final char TEMPLATE_START_CHAR='^';
	private static final char TEMPLATE_END_CHAR='$';
	
	StringRenderer renderer = new StringRenderer();
	public StringTemplateProcessor(){
		
	}

	/**
	 * General binding code.  used to generalize this documented StringTemplate code
	 * <pre>
	    ST st = new ST("<b>$u.id$</b>: $u.name$", '$', '$');
	    st.add("u", new User(999, "parrt"));
	    String result = st.render(); // "<b>999</b>: parrt"
	   </pre>
	   We pass in Object obj instead of User, the ST class to run the comparison with and the object identifer (the 'u' in '$u.id$' and '$u.name$').
	 * 
	 * @param obj
	 * @param template
	 * @param objectIdentifier
	 * @return
	 */
	public String bind(Object obj,ST template,String objectIdentifier){
		template.add(objectIdentifier,obj);
		return template.render();
	}
	
}
