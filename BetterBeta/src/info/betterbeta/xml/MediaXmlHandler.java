package info.betterbeta.xml;

import info.betterbeta.model.IdType;
import info.betterbeta.model.Media;
import info.betterbeta.provider.BetaProvider;

import java.text.ParseException;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MediaXmlHandler extends DefaultHandler {

	private ArrayList<Media> medias;
	private Media media;
	private String tempString;
	
	public MediaXmlHandler() {
		super();
	}

	@Override
	public void startDocument() throws SAXException {
		medias = new ArrayList<Media>();
	
		super.startDocument();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		if("media".equals(qName)){
			media = new Media();
		}
		tempString = "";
		
		super.startElement(uri, localName, qName, attributes);
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		try{
		if("media".equals(localName))
			medias.add(media);
		else if ("name".equals(localName))
			media.setName(tempString);
		else if ("details".equals(localName))
			media.setDetails(tempString);
		else if ("latitude".equals(localName))
			media.setLatitude(tempString);
		else if ("longitude".equals(localName))
			media.setLongitude(tempString);
		else if ("path".equals(localName))
			media.setPath(tempString);
		else if ("type".equals(localName))
			media.setType(Integer.valueOf(tempString));
		else if ("date_added".equals(localName))
			media.setDateAdded(BetaProvider.mysqlDateFormater.parse(tempString));
		else if ("date_modified".equals(localName))
			media.setDateModified(BetaProvider.mysqlDateFormater.parse(tempString));
		else if ("area_id".equals(localName)){
			media.setArea(Long.valueOf(tempString));
			media.setIdType(IdType.MASTER);
		}
		else if ("problem_id".equals(localName)){
			media.setProblem(Long.valueOf(tempString));
			media.setIdType(IdType.MASTER);
		}
		else if ("id".equals(localName))
			media.setMasterId(Long.valueOf(tempString));
		else if ("permission".equals(localName))
			media.setPermission(Integer.valueOf(tempString));
		tempString = "";
	} catch (ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		super.endElement(uri, localName, qName);
	}

	public ArrayList<Media> getMedia() {
		return this.medias;
	}


	public void setMedia(ArrayList<Media> medias) {
		this.medias = medias;
	}


	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		// TODO Auto-generated method stub
		for(int i = start; i < start + length; i++)
			tempString = tempString + ch[i];
		
		super.characters(ch, start, length);
	}
	
	
}
