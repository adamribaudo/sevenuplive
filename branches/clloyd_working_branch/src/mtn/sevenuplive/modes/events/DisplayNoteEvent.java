package mtn.sevenuplive.modes.events;

public class DisplayNoteEvent implements Event {

	public static final String DISPLAY_NOTE_EVENT = "DISPLAY_NOTE_EVENT";

	private int pitch;
	private int displayGridState;
	private int slot;
	
	/**
	 * Event to influence the display grid
	 * @param slot slot which we are affecting the display state of
	 * @param pitch pitch to draw
	 * @param displayGridState DisplayGrid.SOLID for example
	 */
	public DisplayNoteEvent(int slot, int pitch, int displayGridState) {
		this.pitch = pitch;
		this.displayGridState = displayGridState;
		this.slot = slot;
	}
	
	public String getType() {
		return DISPLAY_NOTE_EVENT;
	}
	
	public int getPitch() {
		return pitch;
	}

	public int getDisplayGridState() {
		return displayGridState;
	}
	
	public int getSlot() {
		return slot;
	}

}
