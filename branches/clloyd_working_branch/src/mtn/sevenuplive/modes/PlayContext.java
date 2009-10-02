package mtn.sevenuplive.modes;

import java.util.ArrayList;

import promidi.Note;

public interface PlayContext {
	
	/**
	 * Performs any tranposition of pitch on notes.
	 * Note that if notes are modified, a new Array with new notes should be returned
	 * @param notes
	 */
	public ArrayList<Note> transpose(ArrayList<Note> notes, int transpositionIndex);
	
	/**
	 * Are we transposing
	 * @return
	 */
	public boolean getTranspose();
	
}
