package mtn.sevenuplive.modes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import mtn.sevenuplive.main.MonomeUp;
import mtn.sevenuplive.scales.Scale;
import mtn.sevenuplive.scales.ScaleName;

import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;

import promidi.MidiOut;
import promidi.Note;

/**
 * @author Adam Ribaudo
 * Melodizer Class.
 * 
 * Contains the logic involved in recording and playing back sequences of melodies for a MonomeUp.
 */
public class Melodizer extends Mode implements PlayContext {

	/** 
	 * This is used to record the key state when a recorded pattern
	 * is triggered. And use it in transpose mode to compute the key offset  
	 */
	public int startingKey[];

	/** This is currently active key for each slot */
	public int key[];

	/** This is the scale offset/position in degrees for each slot */
	public int offset[];

	/** 
	 * This is used to record the offset state when a recorded pattern
	 * is triggered. And use it in transpose mode to compute the offset  
	 */
	public int startingOffset[];

	public Hashtable <Integer, NoteSequence> sequences;
	private int cuedIndex;

	private boolean isRecording;

	// Should changing the key or offset transpose the sequence that is playing?
	private boolean transpose = false;

	// Different display modes for Melodizer
	public enum eMelodizerMode {KEYBOARD, CLIP, NONE, POSITION};
	private eMelodizerMode currentMode = eMelodizerMode.KEYBOARD;
	private eMelodizerMode altMode = eMelodizerMode.KEYBOARD;

	private int curSeqBank;
	private int displayNote[]; //Array of ints holding [pitch] of notes being played back in a sequence
	private int clipNotes[][]; //When clipMode=true.  Array of ints holding [channel][pitch] of clips being launched/stopped
	private boolean heldNote[];
	private boolean newHeldNote[];
	private MidiOut midiMelodyOut[];
	private Scale melodyScale;

	private int recMode = ModeConstants.MEL_ON_BUTTON_PRESS;

	public Melodizer(int _navRow, MidiOut _midiMelodyOut[], int grid_width, int grid_height) {
		super(_navRow, grid_width, grid_height);
		midiMelodyOut = _midiMelodyOut;
		key = new int[7];
		offset = new int[7];
		startingKey = new int[7];
		startingOffset = new int[7];
		cuedIndex = -1;
		sequences = new Hashtable<Integer, NoteSequence>();
		curSeqBank = 0;
		heldNote = new boolean[128];
		displayNote = new int[128];
		clipNotes = new int[7][128];
		newHeldNote = new boolean[128];
		melodyScale = new Scale(ScaleName.Major);

		updateNavGrid();
		updateDisplayGrid();
	}

	public void updateDisplayGrid()
	{
		//System.out.println("Update melodizer display grid");
		clearDisplayGrid();

		if(currentMode == eMelodizerMode.KEYBOARD)
		{
			//Show keys
			displayGrid[0][7] = DisplayGrid.FASTBLINK; // C
			displayGrid[1][6] = DisplayGrid.FASTBLINK; // C#
			displayGrid[1][7] = DisplayGrid.FASTBLINK; // D
			displayGrid[2][6] = DisplayGrid.FASTBLINK; // D#
			displayGrid[2][7] = DisplayGrid.FASTBLINK; // E
			displayGrid[3][7] = DisplayGrid.FASTBLINK; // F
			displayGrid[4][6] = DisplayGrid.FASTBLINK; // F#
			displayGrid[4][7] = DisplayGrid.FASTBLINK; // G
			displayGrid[5][6] = DisplayGrid.FASTBLINK; // G#
			displayGrid[5][7] = DisplayGrid.FASTBLINK; // A
			displayGrid[6][6] = DisplayGrid.FASTBLINK; // A#
			displayGrid[6][7] = DisplayGrid.FASTBLINK; // B	

			//Set current key
			switch(key[curSeqBank])
			{
			case 0: displayGrid[0][7] = DisplayGrid.SOLID; // C
			break;
			case 1: displayGrid[1][6] = DisplayGrid.SOLID; // C#
			break;
			case 2: displayGrid[1][7] = DisplayGrid.SOLID; // D
			break;
			case 3: displayGrid[2][6] = DisplayGrid.SOLID; // D#
			break;
			case 4: displayGrid[2][7] = DisplayGrid.SOLID; // E
			break;
			case 5: displayGrid[3][7] = DisplayGrid.SOLID; // F
			break;
			case 6: displayGrid[4][6] = DisplayGrid.SOLID; // F#
			break;
			case 7: displayGrid[4][7] = DisplayGrid.SOLID; // G
			break;
			case 8: displayGrid[5][6] = DisplayGrid.SOLID; // G#
			break;
			case 9: displayGrid[5][7] = DisplayGrid.SOLID; // A
			break;
			case 10: displayGrid[6][6] = DisplayGrid.SOLID; // A#
			break;
			case 11: displayGrid[6][7] = DisplayGrid.SOLID; // B	
			break;
			}

		} else if (currentMode == eMelodizerMode.POSITION) {

			//Show keys
			displayGrid[0][7] = DisplayGrid.FASTBLINK; 
			displayGrid[1][7] = DisplayGrid.FASTBLINK; 
			displayGrid[2][7] = DisplayGrid.FASTBLINK; 
			displayGrid[3][7] = DisplayGrid.FASTBLINK; 
			displayGrid[4][7] = DisplayGrid.FASTBLINK; 
			displayGrid[5][7] = DisplayGrid.FASTBLINK; 
			displayGrid[6][7] = DisplayGrid.FASTBLINK; 	

			// Set current position
			switch(offset[curSeqBank])
			{
			case 0: displayGrid[0][7] = DisplayGrid.SOLID; 
			break;
			case 1: displayGrid[1][7] = DisplayGrid.SOLID; 
			break;
			case 2: displayGrid[2][7] = DisplayGrid.SOLID; 
			break;
			case 3: displayGrid[3][7] = DisplayGrid.SOLID; 
			break;
			case 4: displayGrid[4][7] = DisplayGrid.SOLID; 
			break;
			case 5: displayGrid[5][7] = DisplayGrid.SOLID;
			break;
			case 6: displayGrid[6][7] = DisplayGrid.SOLID; 
			break;
			case 7: displayGrid[7][7] = DisplayGrid.SOLID; 
			break;
			}
		}

		if(currentMode != eMelodizerMode.CLIP)
		{
			//Light this note if not in clipMode
			//Loop through entire grid.  Find each note value. Light it if it's held
			int noteStatus;
			for(int j=0;j<7;j++)
			{
				for(int k=0;k<8;k++)
				{
					noteStatus = displayNote[convertGridPositionToNote(j, k)];

					if (currentMode == eMelodizerMode.KEYBOARD && k < 7) {
						if(noteStatus != DisplayGrid.OFF)
							displayGrid[j][k] = noteStatus;
					} else if (currentMode == eMelodizerMode.POSITION && k < 8) {
						if(noteStatus != DisplayGrid.OFF)
							displayGrid[j][k] = noteStatus;
					}
				}
			}	
		}
		else 
		{
			//Display clip status based on incoming MIDI notes from Live
			int noteStatus;
			for(int j=0;j<7;j++)
			{
				for(int k=0;k<8;k++)
				{
					noteStatus = clipNotes[curSeqBank][convertGridPositionToNote(j, k)];
					if(noteStatus != DisplayGrid.OFF)
						displayGrid[j][k] = noteStatus;
				}
			}	
		}
	}

	/**
	 * Calculate the note under a pad in the grid
	 * Taking into account the scale and the degree 
	 * offset within the scale.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private int convertGridPositionToNote(int x, int y) {
		int note = (((8-y) * 12 - 12) + (Math.abs((x + offset[curSeqBank]) / 7) * 12) + melodyScale.Degrees[((x + offset[curSeqBank]) % 7)] + key[curSeqBank]);
		//System.out.println("Position to Note->Grid x:" + Integer.toString(x) + " y:" + Integer.toString(y) + " note:" + Integer.toString(note));
		return note;
	}

	/**
	 * Calculate the note under a pad in the grid
	 * Taking into account the scale only 
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private int convertGridPositionToNoteNoOffset(int x, int y) {
		int note = (((8-y) * 12 - 12) + melodyScale.Degrees[x % 7] + key[curSeqBank]);
		return note;
	}
	
	/**
	 * Calculate the note under a pad in the grid
	 * Taking into account the scale and the degree 
	 * offset within the scale.
	 * 
	 * @param note 0-127
	 * @return First grid position, higher coordinate top/left if duplicates, null if not found
	 */
	private GridPosition convertNoteToGridPosition(int note) {
		int gridNote;

		for(int j=0;j<7;j++)
		{
			for(int k=0;k<8;k++)
			{
				gridNote = convertGridPositionToNote(j, k);
				if (gridNote == note) {
					//System.out.println("Note to position-> Note:" + Integer.toString(note) + "Grid x:" + Integer.toString(j) + " y:" + Integer.toString(k));
					return new GridPosition(j, k);
				}
			}
		}	
		return null;
	}

	/**
	 * Calculate the note under a pad in the grid
	 * Taking into account the scale only 
	 * 
	 * @param note 0-127
	 * @return First grid position, higher coordinate top/left if duplicates, null if not found
	 */
	private GridPosition convertNoteToGridPositionNoOffset(int note) {
		int gridNote;

		for(int j=0;j<7;j++)
		{
			for(int k=0;k<8;k++)
			{
				gridNote = convertGridPositionToNoteNoOffset(j, k);
				if (gridNote == note) {
					//System.out.println("Note to position-> Note:" + Integer.toString(note) + "Grid x:" + Integer.toString(j) + " y:" + Integer.toString(k));
					return new GridPosition(j, k);
				}
			}
		}	
		return null;
	}

	public void updateNavGrid()
	{
		clearNavGrid();

		navGrid[myNavRow] = DisplayGrid.SOLID;

		if(isRecording)
			navGrid[getYCoordFromSubMenu(curSeqBank)] = DisplayGrid.SLOWBLINK;
		else
			navGrid[getYCoordFromSubMenu(curSeqBank)] = DisplayGrid.FASTBLINK;
	}

	public void press(int x, int y)
	{

		if(x == DisplayGrid.NAVCOL)
		{
			pressNavCol(y);
			updateNavGrid();
		}
		else
			pressDisplay(x,y);

		updateDisplayGrid();
	}

	public void release(int x, int y)
	{
		int melodyPitch;
		melodyPitch = convertGridPositionToNote(x, y);
		heldNote[melodyPitch] = false;
		if(currentMode != eMelodizerMode.CLIP)
			displayNote[melodyPitch] = DisplayGrid.OFF;
		Note melodyRelease;
		melodyRelease = new Note(melodyPitch,0, 0);
		midiMelodyOut[curSeqBank].sendNoteOff(melodyRelease);
		addEvent(melodyRelease);
		updateDisplayGrid();
	}

	private void pressNavCol(int y)
	{
		int pressedSeq = getSubMenuFromYCoord(y);

		//If they press the current bank and it is not already recording, begin cuing in that bank
		if (pressedSeq == curSeqBank && isRecording == false)
		{
			if(getSeqStatus(pressedSeq) == MonomeUp.PLAYING)
			{
				ArrayList<Note> noteList;
				Note note;
				noteList = sequences.get(pressedSeq).getHeldNotes();
				//Loop through heldnotes and send note off for each
				for(int i=0;i<noteList.size();i++)
				{
					note = noteList.get(i);
					midiMelodyOut[pressedSeq].sendNoteOff(note);
					if(pressedSeq == curSeqBank)
						displayNote[note.getPitch()] = DisplayGrid.OFF;
				}
			}
			//Reset displayNote container
			displayNote = new int[128];
			isRecording = true;

			beginCue(pressedSeq);
		}
		//Stop recording
		else if(pressedSeq == curSeqBank && isRecording == true)
		{
			isRecording = false;
			endRecording();
			//immediately begin playing the recorded sequence
			playSeq(pressedSeq);
		}
		else if(pressedSeq != curSeqBank)
		{
			curSeqBank = pressedSeq;
			//Reset displayNote container
			displayNote = new int[128];
		}

		updateNavGrid();
	}

	/**
	 * Determines if the row represents a note row or not
	 * @param y
	 * @return 
	 */
	public boolean isNote(int y) {
		return (y < 6 && currentMode == eMelodizerMode.KEYBOARD) || 
		currentMode == eMelodizerMode.NONE || 
		(y < 7 && currentMode == eMelodizerMode.POSITION);
	}

	private void pressDisplay(int x, int y)
	{
		//User is pressing a note button (as opposed to changing key)
		if (isNote(y))
		{
			//Calculate the pitch by the octave (y) and the transposed scale
			int melodyPitch;
			melodyPitch = convertGridPositionToNote(x, y);

			Note melodySend;
			melodySend = new Note(melodyPitch,100, 0);
			heldNote[melodyPitch] = true;
			if(currentMode != eMelodizerMode.CLIP)
				displayNote[melodyPitch] = DisplayGrid.SOLID;
			midiMelodyOut[curSeqBank].sendNoteOn(melodySend);
			addEvent(melodySend);
		}
		//User is pressing a button in the key area
		else if (currentMode == eMelodizerMode.KEYBOARD)
		{
			//User is changing keys
			if(key[curSeqBank] != getKeyFromCoords(x, y))
			{
				for(int i=0; i<128;i++)
				{
					// If transposing we want the old pitch here
					if (transpose) 
						displayNote[i] = DisplayGrid.OFF;
					
				}

				int curKeyValue = key[curSeqBank];

				//Set new key
				key[curSeqBank] = getKeyFromCoords( x, y);
				int keyDif = curKeyValue - key[curSeqBank];

				//Release notes from old key
				for(int i=0; i<128;i++)
				{ 
					if(heldNote[i])
					{
						Note melodyRelease;
						melodyRelease = new Note(i,0, 0);
						midiMelodyOut[curSeqBank].sendNoteOff(melodyRelease);
						addEvent(melodyRelease);
						heldNote[i] = false;
						
						// If !transposing we want the new pitch here
						if (!transpose) 
							displayNote[i] = DisplayGrid.OFF;
						
						if ((i-keyDif) >= 0)
							newHeldNote[i-keyDif] = true;

						//System.out.println("  Killing " + i);
					}
				}

				//Send notes for new key
				for(int i=0; i<128;i++)
				{ 
					if(newHeldNote[i])
					{		
						Note melodySend;
						melodySend = new Note(i,100, 0);
						midiMelodyOut[curSeqBank].sendNoteOn(melodySend);
						addEvent(melodySend);
						heldNote[i]=true;
						displayNote[i] = DisplayGrid.SOLID;
						newHeldNote[i] = false;
						//System.out.println("Sending " + i);
					}
				}
			}

		} else if (currentMode == eMelodizerMode.POSITION) {

			if (offset[curSeqBank] != x)
			{
				GridPosition[] oldPositions = new GridPosition[128]; 

				// Mark all old positions
				for(int i=0; i<128;i++)
				{
					oldPositions[i] = convertNoteToGridPosition(i);
					
					// If transposing we want the old pitch here
					if (transpose) 
						displayNote[i] = DisplayGrid.OFF;
					
				}

				int curOffsetValue = offset[curSeqBank];

				//Set new offset
				offset[curSeqBank] = x;
				int offsetDif = curOffsetValue - offset[curSeqBank];

				//Release notes from old offset
				for(int i=0; i<128;i++)
				{ 
					if(heldNote[i])
					{
						Note melodyRelease;
						melodyRelease = new Note(i,0, 0);
						midiMelodyOut[curSeqBank].sendNoteOff(melodyRelease);
						addEvent(melodyRelease);
						heldNote[i] = false;
						
						// If !transposing we want the new pitch here
						if (!transpose)
							displayNote[i] = DisplayGrid.OFF;
						
						// Can't compute if we don't know the original position
						if (oldPositions[i] != null) { 
							// Because offset has changed, new note will be a different note from same grid position
							int newNote = convertGridPositionToNote(oldPositions[i].x, oldPositions[i].y);
							newHeldNote[newNote] = true;
						}
					}
				}

				//Send notes for new key
				for(int i=0; i<128;i++)
				{ 
					if(newHeldNote[i])
					{		
						Note melodySend;
						melodySend = new Note(i,100, 0);
						midiMelodyOut[curSeqBank].sendNoteOn(melodySend);
						addEvent(melodySend);
						heldNote[i]=true;
						displayNote[i] = DisplayGrid.SOLID;
						newHeldNote[i]=false;
					}
				}
			}
		}
		
	}

	private int getKeyFromCoords(int x, int y)
	{
		//Set new key
		if(x == 0 && y == 7)return 0;
		else if(x == 1 && y == 6)return 1;
		else if(x == 1 && y == 7)return 2;
		else if(x == 2 && y == 6)return 3;
		else if(x == 2 && y == 7)return 4;
		else if(x == 3 && y == 7)return 5;
		else if(x == 4 && y == 6)return 6;
		else if(x == 4 && y == 7)return 7;
		else if(x == 5 && y == 6)return 8;
		else if(x == 5 && y == 7)return 9;
		else if(x == 6 && y == 6)return 10;
		else if(x == 6 && y == 7)return 11;
		else return 0;
	}

	public void heartbeat()
	{
		ArrayList<Note> noteList;
		NoteSequence s;
		Hashtable <Integer, ArrayList<Note>> notePackage = new Hashtable<Integer, ArrayList<Note>>();
		Integer index;

		//loop through sequences and perform heartbeat on each.  packaging up each note that is returned
		for(Enumeration<Integer> els = sequences.keys();els.hasMoreElements();)
		{
			index = Integer.class.cast(els.nextElement());
			s = sequences.get(index);

			noteList = s.heartbeat();

			// If there are transpositions then the notelist returned will be a clone with the transpositions
			noteList = transpose(noteList, index);

			if(noteList != null)
			{
				//package the note
				notePackage.put(index, noteList);
			}
		}

		//Old monomeup heartbeat code
		Note note;

		//Loop through and send all notes in the notePackage
		for(Enumeration<Integer> els = notePackage.keys();els.hasMoreElements();)
		{
			index = Integer.class.cast(els.nextElement());
			noteList = notePackage.get(index);

			for(int i=0;i<noteList.size();i++)
			{
				note = noteList.get(i);
				if(note.getVelocity() > 0)
				{
					//System.out.println("Playing note " + note.getPitch());
					midiMelodyOut[index].sendNoteOn(note);
					if(index == curSeqBank)
						displayNote[note.getPitch()] = DisplayGrid.SOLID;
				}
				else
				{
					//System.out.println("Releasing note " + note.getPitch());
					midiMelodyOut[index].sendNoteOff(note);
					if(index == curSeqBank)
						displayNote[note.getPitch()] = DisplayGrid.OFF;
				}
				if(index == curSeqBank)
					updateDisplayGrid();
			}
		}
	}

	/**
	 * Beginning a cue creates a new sequence if one does not exist at the specified index.  Otherwise, it begins cueing on the existing sequence.
	 * 
	 * @param seqIndex
	 */
	public void beginCue(int seqIndex)
	{
		if(!sequences.containsKey(seqIndex))
		{
			sequences.put(seqIndex, new NoteSequence(seqIndex, this));
			setMelRecMode(recMode);
		}

		sequences.get(seqIndex).beginCue();

		cuedIndex = seqIndex;
	}

	public void endRecording()
	{
		markTransposeStart(cuedIndex);
		sequences.get(cuedIndex).endRecording();
		cuedIndex = -1;
	}

	/**
	 * Sends a note to the currently cued sequence.  If no sequence is cued, the note will be ignored.
	 * 
	 * @param note
	 */
	public void addEvent(Note note)
	{
		if(cuedIndex > -1)
		{
			sequences.get(cuedIndex).addEvent(note);
		}
	}

	/**
	 * Play to particular sequence
	 * @param seqIndex
	 * @return returns true if the sequence exists and has started playing and false otherwise
	 */
	public boolean playSeq(int seqIndex)
	{
		if(sequences.containsKey(seqIndex))
		{
			markTransposeStart(seqIndex);
			sequences.get(seqIndex).play();
			return true;
		}
		else return false;
	}

	public void stopSeq(int seqIndex)
	{
		if(sequences.containsKey(seqIndex))
		{
			sequences.get(seqIndex).stop();
		}
	}

	//Returns -1 if no such sequence exists or empty.  0 if stopped. 1 if playing.
	public int getSeqStatus(int index)
	{
		if(sequences.containsKey(index))
			return sequences.get(index).getStatus();
		else return -1;

	}

	public Element toXMLElement(String ElementName)
	{



		Element xmlMelodizer = new Element(ElementName);

		xmlMelodizer.setAttribute(new Attribute("scale", melodyScale.Name.toString()));
		// For backwards compatibility only
		xmlMelodizer.setAttribute(new Attribute("clipMode", currentMode == eMelodizerMode.CLIP ? "true" : "false"));
		xmlMelodizer.setAttribute(new Attribute("melodizerMode", currentMode.toString()));
		xmlMelodizer.setAttribute(new Attribute("altMode", altMode.toString()));
		xmlMelodizer.setAttribute(new Attribute("transpose", Boolean.toString(transpose)));
		String keyString = "";
		for(int i=0;i<key.length;i++)
		{
			keyString += key[i];
			if(i!=key.length-1)
				keyString+= ",";
		}

		xmlMelodizer.setAttribute(new Attribute("key", keyString));

		Element xmlSequence;

		int seqIndex;
		NoteSequence sequence;

		for(Enumeration<Integer> els = sequences.keys();els.hasMoreElements();)
		{
			seqIndex = Integer.class.cast(els.nextElement());
			sequence = sequences.get(seqIndex);
			xmlSequence = sequence.toJDOMXMLElement();
			xmlSequence.setAttribute(new Attribute("key", ((Integer)key[seqIndex]).toString()));
			xmlMelodizer.addContent(xmlSequence);
		}

		return xmlMelodizer;
	}

	@SuppressWarnings("unchecked")
	public void loadXMLElement(Element xmlMelodizer)
	{
		//Clear current info
		cuedIndex = -1;
		sequences = new Hashtable<Integer, NoteSequence>();

		//Load XML	
		melodyScale = new Scale(ScaleName.valueOf(xmlMelodizer.getAttribute("scale").getValue()));
		try {
			// This is optional and for backwards compatibility only
			if (xmlMelodizer.getAttribute("clipMode").getBooleanValue() == true) {
				currentMode = eMelodizerMode.CLIP;
			}
			if (xmlMelodizer.getAttribute("melodizerMode") != null) {
				currentMode = eMelodizerMode.valueOf(xmlMelodizer.getAttribute("melodizerMode").getValue());
			}
			if (xmlMelodizer.getAttribute("altMode") != null) {
				altMode = eMelodizerMode.valueOf(xmlMelodizer.getAttribute("altMode").getValue());
			}
			if (xmlMelodizer.getAttribute("transpose") != null) {
				transpose = Boolean.parseBoolean(xmlMelodizer.getAttribute("transpose").getValue());
			}
		} catch (DataConversionException e) {
			// Do nothing
		} catch (NullPointerException e) {
			// Do nothing
		}finally 
		{
			try {
				String keyString = xmlMelodizer.getAttribute("key").getValue();
				int i=0;
				for(String strKey : keyString.split(","))
				{
					key[i] = Integer.parseInt(strKey);
					i++;
				}
			} catch (Exception e) {
				System.out.println("Error: No key attribute found in XML file");
			} finally
			{
				List<Element> xmlSequences;
				NoteSequence sequence;
				Integer index;

				xmlSequences = xmlMelodizer.getChildren();

				for (Element xmlSequence : xmlSequences)
				{
					index = Integer.parseInt(xmlSequence.getAttributeValue("index"));	
					key[index] = Integer.parseInt(xmlSequence.getAttributeValue("key"));
					sequence = new NoteSequence(index, this);
					setMelRecMode(recMode);
					sequence.loadJDOMXMLElement(xmlSequence);
					//Set status to stopped if there is a sequence
					if(!sequence.isEmpty())sequence.setStatus(MonomeUp.STOPPED);
					sequences.put(index, sequence);
				}

				updateDisplayGrid();
			}
		}
	}

	public void reset() {
		for(int i=0;i<7;i++)
			stopSeq(i);
	}

	public void clipLaunch(int pitch, int vel, int channel) {
		if(vel == 0 ||vel == 64) //STOP or OFF
		{
			clipNotes[channel][pitch] = DisplayGrid.OFF;
			//System.out.println("Setting " + pitch + " OFF for channel " + channel);
		}
		else if(vel == 126)//CUE
		{
			clipNotes[channel][pitch] = DisplayGrid.SLOWBLINK;
			//System.out.println("Setting " + pitch + " SLOWBLINK");
		}	
		else if(vel == 127 || vel == 1)//PLAY or continue
		{
			//Turn on the note that actually plays
			clipNotes[channel][pitch] = DisplayGrid.SOLID;
		}
		if(channel == curSeqBank)
			updateDisplayGrid();
	}

	public Scale getScale()
	{
		return melodyScale;
	}

	public void setScale(Scale newScale)
	{
		melodyScale = newScale;
	}

	public void locatorEvent() {
		if(sequences.containsKey(curSeqBank))
		{
			sequences.get(curSeqBank).locatorEvent();
		}
	}

	public void setMelRecMode(int _recMode) {
		//Set all sequences to the new rec Mode
		recMode = _recMode;
		Integer sequenceIndex;
		for(Enumeration<Integer> els = sequences.keys();els.hasMoreElements();)
		{
			sequenceIndex = Integer.class.cast(els.nextElement());
			sequences.get(sequenceIndex).setMelRecMode(recMode);
		}


	}

	public void extNoteOn(Note note, int channel) {

		//System.out.println("Ext note received channel " + channel);
		//Convert the external instrument's channel to the melodizer's channel
		int melodizerChannel = channel - 8;
		//144 = noteOn
		if(note.getVelocity() > 0 && note.getStatus() == 144)
		{
			heldNote[note.getPitch()] = true;
			midiMelodyOut[melodizerChannel].sendNoteOn(note);
			if(melodizerChannel == curSeqBank)
				displayNote[note.getPitch()] = DisplayGrid.SOLID;
			addEvent(note);
		}
		else
		{
			Note releaseNote = new Note(note.getPitch(),0, 0);
			midiMelodyOut[melodizerChannel].sendNoteOff(releaseNote);
			heldNote[note.getPitch()] = false;
			if(melodizerChannel == curSeqBank)
				displayNote[note.getPitch()] = DisplayGrid.OFF;
			addEvent(releaseNote);
		}

		updateDisplayGrid();
	}

	public eMelodizerMode getCurrentMode() {
		return currentMode;
	}

	public void setCurrentMode(eMelodizerMode currentMode) {
		this.currentMode = currentMode;
		updateNavGrid();
		updateDisplayGrid();
	}

	public void setAltMode(eMelodizerMode altCurrentMode) {
		this.altMode = altCurrentMode;
	}

	public eMelodizerMode getAltMode() {
		return altMode;
	}

	public void swapModes() {
		eMelodizerMode oldCurrentMode = currentMode;
		setCurrentMode(this.altMode);
		setAltMode(oldCurrentMode);
	}

	public boolean getTranspose() {
		return transpose;
	}

	public void setTranspose(boolean transpose) {
		this.transpose = transpose;
	}

	/**
	 * Mark the beginning of a transpose session on a sequence
	 * If transpose flag is off, nothing is done.
	 * @param seqIndex
	 */
	private void markTransposeStart(int seqIndex) {
		/** 
		 * If transposing then record this as the starting offset 
		 * and key 
		 */
		if (transpose) {
			startingOffset[seqIndex] = offset[seqIndex];
			startingKey[seqIndex] = key[seqIndex];
		}
	}


	/**
	 * Called when NavMenu change is being cued, aborted or committed
	 */
	@Override
	public void onMenuFocusChange(MenuFocusEvent event) {
		super.onMenuFocusChange(event);

		System.out.println(event);

		// We are interested in this case
		if (event.oldIndex == myNavRow) {
			// When we toggle current mode button we switch between default modes
			if (event.type == Mode.MenuFocusEvent.eMenuFocusEvent.MENU_FOCUS_CHANGE_ABORTED) {
				swapModes();
			}
		}
	}

	/**
	 * Perform pitch transformation if
	 * requested on running note sequences
	 * 
	 */
	public ArrayList<Note> transpose(ArrayList<Note> notes, int transpositionIndex) {
		if (!transpose || notes == null)
			return notes;

		ArrayList<Note> newNotes = new ArrayList<Note>();

		int localOffset = offset[transpositionIndex] - startingOffset[transpositionIndex];
		int localKeyOffset = key[transpositionIndex] - startingKey[transpositionIndex];

		for (Note note : notes) {
			int pitch = note.getPitch();

			GridPosition pos = convertNoteToGridPositionNoOffset(pitch + localKeyOffset);

			// Drop notes that fall off the grid
			if (pos != null) {
				GridPosition newpos = pos.offsetX(localOffset);
				pitch = convertGridPositionToNoteNoOffset(newpos.x, newpos.y);
				newNotes.add(new Note(pitch, note.getVelocity(), note.getLength()));
			}	
		}
		return newNotes; 
	}

	// Grid Test
	public static void main(String args[]) {
		Melodizer.GridPosition grid = new Melodizer.GridPosition(5,4);

		System.out.println(grid);
		System.out.println("offset x by 5");
		grid = grid.offsetX(5);

		System.out.println(grid);
		System.out.println("offset x by -8");
		grid = grid.offsetX(-8);

		System.out.println(grid);

		System.out.println("offset x by -12");
		grid = grid.offsetX(-12);

		System.out.println(grid);
	}

	public static class GridPosition {
		private static final int DIM_X = 7;
		private static final int DIM_Y = 8;

		public int x;
		public int y;

		public GridPosition(int x, int y) {
			this.x = x;
			this.y = y;
		}

		/** 
		 * Add x to the grid position
		 * if x 
		 * Return null if off the grid
		 */
		public GridPosition offsetX(int offset_x) {
			int new_x;
			int new_y;
			if ((offset_x + x) >= 0) {
				new_x = (offset_x + x) % DIM_X;
				new_y = y - Math.abs((offset_x + x) / DIM_X);
			} else {
				new_x = DIM_X - (Math.abs(offset_x + x) % DIM_X);
				new_y = y - (Math.abs((offset_x + x) / DIM_X) + 1) ;
			} 

			if (new_x < DIM_X && new_y < DIM_Y && 
					new_x >= 0 && new_y >= 0)
				return new GridPosition(new_x, new_y);
			else
				return null;
		}

		public String toString() {
			return Integer.toString(x) + "," + Integer.toString(y);
		}


	}

}
