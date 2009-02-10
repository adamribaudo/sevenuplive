/**
 * SevenUp for Monome's 40h
 * @author Adam Ribaudo
 * arribaud@gmail.com
 * 12/09/2007
 * Copyright 2007 - All rights reserved
 * 
 * SevenUpApplet Class - initializes the SevenUpApplet that extends processing's core PApplet class.  Sends COM port to MonomeUp and opens the Midi input device.
 */

package mtn.sevenuplive.main;
import mtn.sevenuplive.modes.AllModes;
import mtn.sevenuplive.scales.Scale;
import mtn.sevenuplive.scales.ScaleName;

import org.jdom.Document;

import oscP5.OscMessage;
import promidi.MidiIO;
import promidi.Note;
import proxml.XMLInOut;

public class SevenUpApplet extends processing.core.PApplet
{
	private static final long serialVersionUID = 1L;
	private MonomeUp m;
	private XMLInOut xmlIO;
	private Scale monomeScale;
	private MidiIO midiIO;
	private ConnectionSettings sevenUpConnections;
	private SevenUpPanel parentPanel;
	
	public SevenUpApplet(ConnectionSettings _sevenUpConnections, SevenUpPanel _parentPanel)
	{
		sevenUpConnections = _sevenUpConnections;
		parentPanel = _parentPanel;
		init();
	}
	
	public void setup()
	{
	   
	   frameRate(40);
	   size(300, 200);
	   
	   xmlIO = new XMLInOut(this);
	   midiIO = MidiIO.getInstance(this);
	   midiIO.printDevices();
	   monomeScale = new Scale(ScaleName.Major);
	   m = new MonomeUp(this, sevenUpConnections, monomeScale, midiIO, parentPanel);
	   m.lightsOff();
	   this.online = false;
	   
	   //Set up input devices for channels 1-8
	   //MIDI Clock is set to channel 8 as to not interfere with Melodizer Clip launching
	   midiIO.plug(this, "noteOn", sevenUpConnections.midiInputDeviceIndex, 7);
	   
	   //Channels 1-7 used to listen to Clip Launch events.
	   midiIO.plug(this, "noteOnCh1", sevenUpConnections.midiInputDeviceIndex, 0);
	   midiIO.plug(this, "noteOnCh2", sevenUpConnections.midiInputDeviceIndex, 1);
	   midiIO.plug(this, "noteOnCh3", sevenUpConnections.midiInputDeviceIndex, 2);
	   midiIO.plug(this, "noteOnCh4", sevenUpConnections.midiInputDeviceIndex, 3);
	   midiIO.plug(this, "noteOnCh5", sevenUpConnections.midiInputDeviceIndex, 4);
	   midiIO.plug(this, "noteOnCh6", sevenUpConnections.midiInputDeviceIndex, 5);
	   midiIO.plug(this, "noteOnCh7", sevenUpConnections.midiInputDeviceIndex, 6);
	   
	   //Channels 9-15 used to listen to external instruments for melodizer
	   midiIO.plug(this, "noteOnCh9", sevenUpConnections.midiInputDeviceIndex, 8);
	   midiIO.plug(this, "noteOnCh10", sevenUpConnections.midiInputDeviceIndex, 9);
	   midiIO.plug(this, "noteOnCh11", sevenUpConnections.midiInputDeviceIndex, 10);
	   midiIO.plug(this, "noteOnCh12", sevenUpConnections.midiInputDeviceIndex, 11);
	   midiIO.plug(this, "noteOnCh13", sevenUpConnections.midiInputDeviceIndex, 12);
	   midiIO.plug(this, "noteOnCh14", sevenUpConnections.midiInputDeviceIndex, 13);
	   midiIO.plug(this, "noteOnCh15", sevenUpConnections.midiInputDeviceIndex, 14);
	}
	
	private Scale getScaleFromString(String scaleName)
	{
		Scale newScale;
		
		if(scaleName.equals("Billian"))
			newScale = new Scale(ScaleName.Billian);
		else if(scaleName.equals("Blues"))
			newScale = new Scale(ScaleName.Blues);
		else if(scaleName.equals("Blues (Minor)"))
			newScale = new Scale(ScaleName.MinorBlues);
		else if(scaleName.equals("Dorian"))
			newScale = new Scale(ScaleName.Dorian);
		else if(scaleName.equals("Major"))
			newScale = new Scale(ScaleName.Major);
		else if(scaleName.equals("Minor"))
			newScale = new Scale(ScaleName.Minor);
		else if(scaleName.equals("Minor Seven"))
			newScale = new Scale(ScaleName.MinorSeven);
		else if(scaleName.equals("Pentatonic"))
			newScale = new Scale(ScaleName.Pentatonic);
		else if(scaleName.equals("Pentatonic (Minor)"))
			newScale = new Scale(ScaleName.MinorPentatonic);
		else if(scaleName.equals("Ultra Locrian"))
			newScale = new Scale(ScaleName.UltraLocrian);
		else if(scaleName.equals("Mullnixian"))
			newScale = new Scale(ScaleName.Mullnixian);
		else
			newScale = new Scale(ScaleName.Chromatic);
		
		return newScale;
	}
	
	public void setMelody1Scale(String scaleName)
	{
		m.setMelody1Scale(getScaleFromString(scaleName));
	}
	
	public void setMelody2Scale(String scaleName)
	{
		m.setMelody2Scale(getScaleFromString(scaleName));
	}
	
	public void setLoopChokeGroup(int loopNum, int chokeGroup)
	{
		m.setLoopChoke(loopNum, chokeGroup);
	}
	
	public void setGateLoopChokes(boolean gateLoops)
	{
		m.setGateLoopChokes(gateLoops);
	}
	
	public boolean getGateLoopChokes()
	{
		return m.getGateLoopChokes();
	}
	
	public boolean openSevenUpPatch(String patchPath)
	{
		try
		{
			xmlIO.loadElement(patchPath);
			return true;
		}catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public void draw()
	{
		m.draw();
	}
	
	public void monomePressed(int x, int y)
	{
		m.monomePressed(x, y);
	}
	
	public void monomeReleased(int x, int y)
	{
		m.monomeReleased(x, y); 
	}
	
	public void finalize()
	{
		m.lightsOff();
		m = null;
	}
	
	/***
	 * This event was originally in the JKlabs MonomeOSC class.  However, the event was not firing so I moved it here and it seems to work.
	 * @param oscIn
	 */
	void   oscEvent(OscMessage oscIn) {
		if (oscIn.checkAddrPattern("/" + sevenUpConnections.oscPrefix + "/press")) {
			
			if (oscIn.checkTypetag("iii")) {
				int x = oscIn.get(0).intValue();
				int y = oscIn.get(1).intValue();
				int value = oscIn.get(2).intValue();
				if(value == 1)
				{
					monomePressed(x, y);
				}
				else
					monomeReleased(x, y);
				
				}
		}
	}

	public void noteOn(Note note){
		//144 = noteOn
		if (note.getStatus() == 144)
			m.noteOn(note.getPitch());
	}
	
	public void noteOnCh1(Note note){
		m.clipLaunch(note.getPitch(), note.getVelocity(), 0);
	}
	public void noteOnCh2(Note note){
		m.clipLaunch(note.getPitch(), note.getVelocity(), 1);
	}
	public void noteOnCh3(Note note){
		m.clipLaunch(note.getPitch(), note.getVelocity(), 2);
	}
	public void noteOnCh4(Note note){
		m.clipLaunch(note.getPitch(), note.getVelocity(), 3);
	}
	public void noteOnCh5(Note note){
		m.clipLaunch(note.getPitch(), note.getVelocity(), 4);
	}
	public void noteOnCh6(Note note){
		m.clipLaunch(note.getPitch(), note.getVelocity(), 5);
	}
	public void noteOnCh7(Note note){
		m.clipLaunch(note.getPitch(), note.getVelocity(), 6);
	}
	
	public void noteOnCh9(Note note){
		if(note.getPitch() != 0)
			m.extNoteOn(note, 8);
	}
	
	public void noteOnCh10(Note note){
		if(note.getPitch() != 0)
			m.extNoteOn(note, 9);
	}

	public void noteOnCh11(Note note){
		if(note.getPitch() != 0)
			m.extNoteOn(note, 10);
	}

	public void noteOnCh12(Note note){
		if(note.getPitch() != 0)
			m.extNoteOn(note, 11);
	}
	
	public void noteOnCh13(Note note){
		if(note.getPitch() != 0)
			m.extNoteOn(note, 12);
	}

	public void noteOnCh14(Note note){
		if(note.getPitch() != 0)
			m.extNoteOn(note, 13);
	}
	
	public void noteOnCh15(Note note){
		if(note.getPitch() != 0)
			m.extNoteOn(note, 14);
	}
	
	public Scale getMelody1Scale()
	{
		return m.getMelody1Scale();
	}
	
	public Scale getMelody2Scale()
	{
		return m.getMelody2Scale();
	}
	
	public Document toJDOMXMLDocument(String fileName)
	{
		return m.toXMLDocument(fileName);
	}
	
	public boolean loadJDOMXMLDocument(Document XMLDoc)
	{
		return m.loadXML(XMLDoc);
	}
	
	public int getLoopChokeGroup(int loopNum)
	{
		return m.getLoopChokeGroup(loopNum);
	}
	
	public void quit()
	{
		//Redundant
		midiIO.closeInput(sevenUpConnections.midiInputDeviceIndex);
		midiIO.closeOutputs();
	}

	public void close() {
		midiIO.closeInput(sevenUpConnections.midiInputDeviceIndex);
		midiIO.closeOutputs();
	}

	public int loadPrevPatch() {
		return m.loadPrevPatch();
	}

	public int loadNextPatch() {
		return m.loadNextPatch();
	}
	
	public int getPatchPackSize()
	{
		return m.getPatchPackSize();
	}

	public String getPatchTitle() {
		return m.getPatchTitle();
	}

	public void setMelody1ClipMode(boolean b) {
		AllModes.getInstance().getMelodizer1().clipMode = b;
	}
	public void setMelody2ClipMode(boolean b) {
		AllModes.getInstance().getMelodizer2().clipMode = b;
	}
	public boolean getMelody1ClipMode()
	{
		return AllModes.getInstance().getMelodizer1().clipMode;
	}
	public boolean getMelody2ClipMode()
	{
		return AllModes.getInstance().getMelodizer2().clipMode;
	}

	public void setLooperMute(boolean mute) {
		m.setLooperMute(mute);
		
	}

	public void setMelRecMode(int melRecMode) {
		m.setMelRecMode(melRecMode);
	}
	
	public void setLoopMultiplier(int loopNum, int multiplier) {
		m.setLoopMultiplier(loopNum, multiplier);
	}
	
	
}
		