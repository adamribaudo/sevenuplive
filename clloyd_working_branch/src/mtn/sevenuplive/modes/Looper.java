package mtn.sevenuplive.modes;

import java.util.List;

import mtn.sevenuplive.main.MonomeUp;

import org.jdom.Attribute;
import org.jdom.Element;

import promidi.MidiOut;
import promidi.Note;

public class Looper extends Mode {
	
	private Loop[] loops;
	
	private final static int OFFSET_START_CTRL = 96;
	
	private MidiOut midiOut;
	private Boolean gateLoopChokes = true;
	private boolean muteNotes = false;

	public boolean[] stopLoopsOnNextStep;
	
	public Looper(int _navRow, MidiOut _midiOut, mtn.sevenuplive.main.MonomeUp _m, int grid_width, int grid_height) {
		super(_navRow, grid_width, grid_height);
		
		stopLoopsOnNextStep = new boolean[7];
		
		loops = new Loop[7];
		for(int i=0;i<loops.length;i++)
			loops[i] = new Loop();
		
		midiOut = _midiOut;
	}
	
	public int getNumLoops()
	{
		return loops.length;
	}
	
	public Loop getLoop(int index)
	{
		return loops[index];
	}
	
	private void updateNavGrid()
	{
		clearNavGrid();
		navGrid[myNavRow] = DisplayGrid.SOLID;
	   //Iterate through the loops and set them all to their coordesponding values
 	   for(int i=0;i<loops.length;i++)
       {
     			 if(loops[i].isPlaying())
     				  navGrid[getYCoordFromSubMenu(i)] = DisplayGrid.FASTBLINK;
     			 else 
     				  navGrid[getYCoordFromSubMenu(i)] = DisplayGrid.OFF;
       }
	}
	
	public void updateDisplayGrid()
	{
		clearDisplayGrid();
		
		for(int i=0;i<loops.length;i++)
	    {
		      if(loops[i].isPlaying())
				   displayGrid[i][loops[i].getStep()] = DisplayGrid.SOLID;
	    }
	}
	
	public int[] getNavGrid()
	{
		updateNavGrid();
		return navGrid;
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
		//updateNavGrid(); // @TODO clloyd not needed, done in play and stop functions
	}
	
	private void pressNavCol(int y)
	{
		int loopIndex = getSubMenuFromYCoord(y);
		//Inverse the mode of the corresponding loop
		if(AllModes.getInstance().getLoopRecorder().isLoopSequencePlaying(loopIndex) || loops[loopIndex].isPlaying())
		{
			loops[loopIndex].stop();
			AllModes.getInstance().getLoopRecorder().stopLoopSequence(loopIndex);
			midiOut.sendNoteOff(new Note(MonomeUp.C3+loopIndex,127, 0));
		}
		else
		{
			playLoop(loopIndex, 0);
		}
		
		
	}
	
	public boolean isLoopPlaying(int loopNum)
	{
		return loops[loopNum].isPlaying();
	}
	
	public void stopLoop(int loopNum)
	{
		loops[loopNum].stop();
		updateNavGrid();
		AllModes.loopRecorder.updateNavGrid();
		if (loops[loopNum].getType() != Loop.HIT)
			midiOut.sendNoteOff(new Note(MonomeUp.C3+loopNum,127, 0));
	}
	
	public void setLoopStopOnNextStep(int loopNum)
	{
		stopLoopsOnNextStep[loopNum] = true;
	}
	
	public void playLoop(int loopNum, int step)
	{
		loops[loopNum].setTrigger(step, true);
		loops[loopNum].setStep(step);
		loops[loopNum].setPressedRow(step);
		updateNavGrid();
		AllModes.loopRecorder.updateNavGrid();
	}
	
	private void pressDisplay(int x, int y)
	{
			//Choke loops in the same choke group
			int curChokeGroup = loops[x].getChokeGroup();
			if(curChokeGroup > -1)
			{
				for(int i=0; i<7;i++)
					if(loops[i].getChokeGroup() == curChokeGroup && i != x)
					{
						if(gateLoopChokes)
							stopLoop(i);
						else
							stopLoopsOnNextStep[i] = true;
					}
			}
			
			stopLoopsOnNextStep[x] = false;
			playLoop(x, y);
			
			//System.out.println("Gate loops is " + gateLoopChokes);
	}
	
	// NOTE: Not used
	/*private int getSizeOfStoppedLoopsInChokeGroup(int chokeGroup)
	{
		int size = 0;
		for(int i=0;i<7;i++)
		{
			if(stopLoopsOnNextStep[i])
				if(loops[i].getChokeGroup() == chokeGroup)
					size++;
		}
		return size;
	}
	
	private int getChokeGroupSize(int chokeGroup)
	{
		int size = 0;
		for(int i=0; i<7;i++)
		{
			if(loops[i].getChokeGroup() == chokeGroup)
				size++;
		}
		return size;
	}*/
	
	public void release(int x, int y)
	{
		if (loops[x].isPlaying() && loops[x].getType() == Loop.MOMENTARY && loops[x].getLastTriggedStep() == y) {
			stopLoop(x);
		}
	}

	public void step()
	{
		int pressedRow;
		int resCounter;
		int step;
		
		updateDisplayGrid();
		
		for(int i=0;i<7;i++)
		{
			if(stopLoopsOnNextStep[i])
			{
				stopLoop(i);
				stopLoopsOnNextStep[i] = false;
			}
		}
		
		for(int i = 0;i<loops.length;i++)
        {
			
			if(loops[i].isPlaying())
        	{
        		pressedRow = loops[i].getPressedRow();
        		resCounter = loops[i].getResCounter();
        		step = loops[i].getStep();
        		
        		//In buzz you have to send the controller AFTER the note is played
        		int loopCtrlValue = (loops[i].getStep() * 16);
        		
        		
        		// Only send the controller if we are changing position. This allows the sample to play smoothly and linearly.
        		if (pressedRow > -1) {
        			switch (loops[i].getType()) {
        				case Loop.HIT: // Hits we let it run to the end of the sample and don't send a noteOff on release
        					if (loops[i].getTrigger(step) == true) {
        						midiOut.sendController(new promidi.Controller(OFFSET_START_CTRL+i, loopCtrlValue));
        						if(!muteNotes)
        							midiOut.sendNoteOn(new Note(MonomeUp.C3+i,127, 0));
        						loops[i].setTrigger(step, false);
        					} else {
        						stopLoop(i);
        						pressedRow = -1;
        	    				continue;
        					}
        					break;
        				case Loop.MOMENTARY:
        				case Loop.SLICE:
        					if (resCounter == 0 || loops[i].getTrigger(step)) {
        						midiOut.sendController(new promidi.Controller(OFFSET_START_CTRL+i, loopCtrlValue));
        						if(!muteNotes)
        							midiOut.sendNoteOn(new Note(MonomeUp.C3+i,127, 0));
        						loops[i].setTrigger(step, false);
        					}
        					// If it's a one shot loop, then we stop after the first iteration
        	        		if (loops[i].getType() == Loop.SLICE && loops[i].isLastResInStep()) {
        	    				stopLoop(i);
        	    				pressedRow = -1;
        	    				continue;
        	    			}
        				// Don't break here, flow into SHOT	
        				case Loop.SHOT:
        					// If it's a one shot loop, then we stop after the first iteration
        	        		if (loops[i].getType() == Loop.SHOT && loops[i].isLastResStep()) {
        	    				stopLoop(i);
        	    				pressedRow = -1;
        	    				continue;
        	    			}
        	        	// Don't break flow into LOOP	
        				case Loop.LOOP:
        				default:
        					if (resCounter == 0) 
        						midiOut.sendController(new promidi.Controller(OFFSET_START_CTRL+i, loopCtrlValue));
        				
        					//Send note every time looprow is 0 or at it's offset
        	        		if((resCounter == 0) && (step == 0 || pressedRow > -1))
        	        		{
        	        			if (!muteNotes) {
        	        				// We only want to retrigger when necessary to avoid additional microfades or minor timing issues.
	        	        			if(loops[i].getTrigger(step) == true || (step == 0 && resCounter == 0 && loops[i].getIteration() > 0)) {
	        	        				midiOut.sendNoteOn(new Note(MonomeUp.C3+i,127, 0));
	        	        				loops[i].setTrigger(step, false); 
	        	        			}
        	        			}
        	        			pressedRow = -1;
        	        				
        	        		}
        					break;
        					
        			};
        		}	
        		
        		loops[i].nextResCount();
        	}
        }
	}
	
	public Element toJDOMXMLElement()
	{
		Element xmlLooper = new Element("looper");
		
		xmlLooper.setAttribute(new Attribute("gateLoopChokes", gateLoopChokes.toString()));
		
		Element xmlLoop;

		for(Integer i=0;i<loops.length;i++)
		{
			xmlLoop = loops[i].toJDOMXMLElement();
			xmlLoop.setAttribute(new Attribute("index", i.toString()));
			xmlLooper.addContent(xmlLoop);
		}
		
		return xmlLooper;
	}
	
	@SuppressWarnings("unchecked")
	public void loadJDOMXMLElement(Element xmlLooper)
	{	
		List<Element> xmlLoops;
		Integer loopIndex;
		
		gateLoopChokes = xmlLooper.getAttributeValue("gateLoopChokes") == null ? gateLoopChokes : Boolean.parseBoolean(xmlLooper.getAttributeValue("gateLoopChokes"));
		
		xmlLoops = xmlLooper.getChildren();
		
		for (Element xmlLoop : xmlLoops)
		{
			loopIndex = xmlLoop.getAttributeValue("index") == null ? NOT_SET : Integer.parseInt(xmlLoop.getAttributeValue("index"));
			if (loopIndex != NOT_SET)
				loops[loopIndex].loadJDOMXMLElement(xmlLoop);		
		}
	}
	
	public void setGateLoopChokes(boolean _gateLoopChokes)
	{
		gateLoopChokes = _gateLoopChokes;
	}
	
	public boolean getGateLoopChokes()
	{
		return gateLoopChokes;
	}

	public void reset() {
		for(int i=0;i<7;i++)
			stopLoop(i);
	}

	public void setLooperMute(boolean mute) {
		muteNotes = mute;
		
	}
}

