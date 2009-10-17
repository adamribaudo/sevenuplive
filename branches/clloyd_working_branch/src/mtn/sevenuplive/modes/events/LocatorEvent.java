package mtn.sevenuplive.modes.events;

public class LocatorEvent implements Event {

	public static final String LOCATOR_EVENT = "LOCATOR_EVENT";

	private int slot;
	
	public LocatorEvent() {}
	
	/**
	 * Event to set sequence location
	 * @param slot slot which we are affecting -1 to affect all
	 */
	public LocatorEvent(int slot) {
		this.slot = slot;
	}
	
	public String getType() {
		return LOCATOR_EVENT;
	}
	
	public int getSlot() {
		return slot;
	}

}
