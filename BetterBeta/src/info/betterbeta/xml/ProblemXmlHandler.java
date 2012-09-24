package info.betterbeta.xml;

import info.betterbeta.model.IdType;
import info.betterbeta.model.Problem;
import info.betterbeta.provider.BetaProvider;

import java.text.ParseException;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ProblemXmlHandler extends DefaultHandler {

	private ArrayList<Problem> problems;
	private Problem problem;
	private String tempString;
	
	public ProblemXmlHandler() {
		super();
	}
	
	
	@Override
	public void startDocument() throws SAXException {
		problems = new ArrayList<Problem>();
	
		super.startDocument();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		if("problem".equals(qName)){
			problem = new Problem();
		}
		
		tempString = "";
		
		super.startElement(uri, localName, qName, attributes);
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		try{
		if("problem".equals(localName))
			problems.add(problem);
		else if ("name".equals(localName))
			problem.setName(tempString);
		else if ("details".equals(localName))
			problem.setDetails(tempString);
		else if ("latitude".equals(localName))
			problem.setLatitude(tempString);
		else if ("longitude".equals(localName))
			problem.setLongitude(tempString);
		else if ("date_added".equals(localName))
			problem.setDateAdded(BetaProvider.mysqlDateFormater.parse(tempString));
		else if ("date_modified".equals(localName))
			problem.setDateModified(BetaProvider.mysqlDateFormater.parse(tempString));
		else if ("area_id".equals(localName)){
			problem.setArea(Long.valueOf(tempString));
			problem.setIdType(IdType.MASTER);
		}
		else if ("id".equals(localName))
			problem.setMasterId(Long.valueOf(tempString));
		else if ("permission".equals(localName))
			problem.setPermission(Integer.valueOf(tempString));
		tempString = "";
	} catch (ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		super.endElement(uri, localName, qName);
	}

	public ArrayList<Problem> getProblems() {
		return this.problems;
	}


	public void setProblems(ArrayList<Problem> problems) {
		this.problems = problems;
	}


	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		// TODO Auto-generated method stub
		for(int i = start; i < start + length; i++)
			tempString = tempString + ch[i];
		
		super.characters(ch, start, length);
	}
	

}
