package info.betterbeta.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class PutResponseXmlHandler extends DefaultHandler {

	private boolean success;
	private String reason;
	private String id;
	
	private String tempString;
	public PutResponseXmlHandler() {
		super();
	}
	
	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		tempString = "";
		super.startElement(uri, localName, qName, attributes);
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if("result".equals(localName)){
			if ("success".equals(tempString))
				success = true;
			else
				success = false;
		}
		else if ("reason".equals(localName))
			reason = tempString;
		else if ("id".equals(localName))
			id = tempString;

		tempString = "";
		super.endElement(uri, localName, qName);
	}

	

	public boolean isSuccess() {
		return this.success;
	}


	public void setSuccess(boolean success) {
		this.success = success;
	}


	public String getReason() {
		return this.reason;
	}


	public void setReason(String reason) {
		this.reason = reason;
	}


	public String getId() {
		return this.id;
	}


	public void setId(String id) {
		this.id = id;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		// TODO Auto-generated method stub
		for(int i = start; i < start + length; i++)
			tempString = tempString + ch[i];
		
		super.characters(ch, start, length);
	}
	
}
