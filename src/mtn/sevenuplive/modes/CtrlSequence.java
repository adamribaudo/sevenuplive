package mtn.sevenuplive.modes;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import mtn.sevenuplive.main.MonomeUp;
import org.jdom.Attribute;
import org.jdom.Element;

public class CtrlSequence {
	
	private int counter;
	private int length;
	private int index;
	private int status = -1;
	
	//Hashtable of keys (metronome count) and Integer loop positions
	private Hashtable<Integer, ArrayList<ControlValue>> events;

	CtrlSequence(int index){
		initialize();
		this.index = index;
	}
	
	public int getIndex()
	{
		return index;
	}
	
	public boolean hasEvents()
	{
		return !events.isEmpty();
	}
	
	private void initialize()
	{
		status = MonomeUp.STOPPED;
		events = new Hashtable<Integer, ArrayList<ControlValue>>();	
	}
	
	public void beginCue()
	{
		initialize();
		status = MonomeUp.CUED;
	}
	
	public void endRecording()
	{
		length = counter - 1;
		status = MonomeUp.STOPPED;
		//System.out.println("Sequence - " + index + " length = " + length);
	}
	
	public ArrayList<ControlValue> heartbeat()
	{
		//account for recording never stopping
		if(counter == Integer.MAX_VALUE - 1)
		{
			endRecording();
			return null;
		}
		
		//Advance counter if sequence is playing or recording
		if(status == MonomeUp.PLAYING || status == MonomeUp.RECORDING)
		{
			counter++;
			//System.out.println("Sequence - " + _index + ": count = " + counter);
		}
		
		//Send ControlValue if isPlaying and there is an event at the current count
		if(status == MonomeUp.PLAYING)
		{
			//Reset counter to beginning if it reaches the length
			if(counter > length)
			{
				counter = 1;
			}

			if(events.containsKey(counter))
			{
				//System.out.println("Sequence - " + index + " sending ctrl " + events.get(counter) + " at " + counter);
				return events.get(counter);
			}
			else return null;
		}
		
		return null;
	}
	
	/**
	 * Accepts an arraylist of integers that represent a bund of ctrlValues to fire at the event time
	 * @param ctrlValue
	 */
	public void addEvent(Integer ctrlId, Integer ctrlValue)
	{
		ArrayList<ControlValue> ctrlValues = new ArrayList<ControlValue>();
		ctrlValues.add(new ControlValue(ctrlId, ctrlValue));
		
		//Sequence is cued, just add the note
		if(status == MonomeUp.CUED)
		{
			status = MonomeUp.RECORDING;
			counter = 1;
			events.put(counter, ctrlValues);
		}
		//Sequence is recording, check to see if any notes are at this event and add accordingly
		else if(status == MonomeUp.RECORDING)
		{
			if(events.containsKey(counter))
				events.get(counter).add(new ControlValue(ctrlId, ctrlValue));
			else
				events.put(counter, ctrlValues);
		}
	}
	
	public void play()
	{
		status = MonomeUp.PLAYING;
		counter = 0;
	}
	
	public void stop()
	{
		status = MonomeUp.STOPPED;
	}
	
	/**
	 * Searches the event hashtable for all ctrl ids that are used.  Can be used to stop any loops tied to this event list.
	 * @return ArrayList of integer ids that are contained within the event list
	 */
	public ArrayList<Integer> getUniqueCtrlIds()
	{
		ArrayList<Integer> uniqueCtrlIds = new ArrayList<Integer>();
		
		Enumeration<Integer> eventEnum = events.keys();
		
		while(eventEnum.hasMoreElements())
		{
			Integer curEvent = eventEnum.nextElement();
			ArrayList<ControlValue> controlValues = events.get(curEvent);
			for(ControlValue controlValue : controlValues)
			{
				if(!uniqueCtrlIds.contains(new Integer(controlValue.getId())))
				{
					uniqueCtrlIds.add(new Integer(controlValue.getId()));
				}
			}
		}
		
		return uniqueCtrlIds;
	}
	
	public int getStatus()
	{
		return status;
	}
	

	public Element toJDOMXMLElement()
	{
		Element xmlSequence = new Element("ctrlSequence");
		
		Element xmlEvent;
		Element xmlControlValue;
		
		Integer eventIndex;
		ArrayList<ControlValue> ctrlValue;

		xmlSequence.setAttribute(new Attribute("length", ((Integer)length).toString()));
		xmlSequence.setAttribute(new Attribute("index", ((Integer)index).toString()));

		for(Enumeration<Integer> els = events.keys();els.hasMoreElements();)
		{
			xmlEvent = new Element("event");
			eventIndex = Integer.class.cast(els.nextElement());
			xmlEvent.setAttribute(new Attribute("index", eventIndex.toString()));
			
			ctrlValue = events.get(eventIndex);
			
			for(ControlValue curControlValue : ctrlValue)
			{
				xmlControlValue = new Element("controlValue");
				xmlControlValue.setAttribute(new Attribute("value", Integer.toString(curControlValue.getValue())));
				xmlControlValue.setAttribute(new Attribute("id", Integer.toString(curControlValue.getId())));
				xmlEvent.addContent(xmlControlValue);
			}
			
			xmlSequence.addContent(xmlEvent);
		}
		
		return xmlSequence;
	}
	
	@SuppressWarnings("unchecked")
	public void loadJDOMXMLElement(Element xmlSequence)
	{
		initialize();
		
		length = xmlSequence.getAttributeValue("length") == null ? ModeConstants.NOT_SET : Integer.parseInt(xmlSequence.getAttributeValue("length"));
		
		List<Element> xmlEvents;
		List<Element> xmlControlValues;
		ArrayList<ControlValue> controlValues;
		Integer ctrlValue;
		Integer id;
		Integer index;
		
		xmlEvents = xmlSequence.getChildren();
		
		for (Element xmlEvent :  xmlEvents)
		{
			controlValues = new ArrayList<ControlValue>();
			index = xmlEvent.getAttributeValue("index") == null ? ModeConstants.NOT_SET : Integer.parseInt(xmlEvent.getAttributeValue("index"));
			xmlControlValues = xmlEvent.getChildren();
			for (Element xmlControlValue : xmlControlValues)
			{
				ctrlValue = xmlControlValue.getAttributeValue("value") == null ? ModeConstants.NOT_SET : Integer.parseInt(xmlControlValue.getAttributeValue("value"));
				id = xmlControlValue.getAttributeValue("id") == null ? ModeConstants.NOT_SET : Integer.parseInt(xmlControlValue.getAttributeValue("id"));
				controlValues.add(new ControlValue(id, ctrlValue));
			}
			events.put(index, controlValues);
		}
	}
	
	
}
