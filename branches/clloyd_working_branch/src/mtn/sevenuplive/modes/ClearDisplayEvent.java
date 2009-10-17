package mtn.sevenuplive.modes;


public class ClearDisplayEvent implements Event {

	public static final String CLEAR_DISPLAY_EVENT = "CLEAR_DISPLAY_EVENT";
		
	public ClearDisplayEvent() {
	}
	
	public String getType() {
		return CLEAR_DISPLAY_EVENT;
	}
}
