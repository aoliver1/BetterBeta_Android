package info.betterbeta.xml;

import info.betterbeta.model.Area;
import info.betterbeta.model.IdType;
import info.betterbeta.provider.BetaProvider;

import java.text.ParseException;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class AreaXmlHandler extends DefaultHandler {

	private ArrayList<Area> areas;
	private Area area;
	private String tempString;
	
	public AreaXmlHandler() {
		super();
	}
	
	
	@Override
	public void startDocument() throws SAXException {
		areas = new ArrayList<Area>();
	
		super.startDocument();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		if("area".equals(qName)){
			area = new Area();
		}
		
		tempString = "";
		
		super.startElement(uri, localName, qName, attributes);
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		try {
			
		if("area".equals(localName))
			areas.add(area);
		else if ("name".equals(localName))
			area.setName(tempString);
		else if ("details".equals(localName))
			area.setDetails(tempString);
		else if ("latitude".equals(localName))
			area.setLatitude(tempString);
		else if ("longitude".equals(localName))
			area.setLongitude(tempString);
		else if ("date_added".equals(localName))
			area.setDateAdded(BetaProvider.mysqlDateFormater.parse(tempString));
		else if ("date_modified".equals(localName))
			area.setDateModified(BetaProvider.mysqlDateFormater.parse(tempString));
		else if ("parent_id".equals(localName)){
			area.setParent(Long.valueOf(tempString));
			area.setIdType(IdType.MASTER);
		}
		else if ("id".equals(localName))
			area.setMasterId(Long.valueOf(tempString));

		else if ("permission".equals(localName))
			area.setPermission(Integer.valueOf(tempString));
		tempString = "";
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.endElement(uri, localName, qName);
	}

	public ArrayList<Area> getAreas() {
		return this.areas;
	}


	public void setAreas(ArrayList<Area> areas) {
		this.areas = areas;
	}


	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		// TODO Auto-generated method stub
		for(int i = start; i < start + length; i++)
			tempString = tempString + ch[i];
		
		super.characters(ch, start, length);
	}
	

}
