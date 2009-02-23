package mtn.sevenuplive.modes;

public class AllModes {
	
	// A bit of cheating but allow direct reference for classes in this package
	// as the getters are too verbose here
	static PatternizerModel patternizerModel;
	static Controller controller;
	static Sequencer sequencer;
	static Melodizer melodizer1;
	static Melodizer melodizer2;
	static Looper looper;
	static LoopRecorder loopRecorder;
	static Masterizer masterizer;
	static PatternizerView patternizerViews[];

	private static AllModes theinstance;
	
	public static AllModes getInstance() {
		return theinstance;
	}
	
	/**
	 * Singleton pattern for allmodes
	 * 
	 * Singleton because we should never have more than one of each in the system, even 
	 * with multiple grids
	 * @param patternizer
	 * @param controller
	 * @param sequencer
	 * @param melodizer1
	 * @param melodizer2
	 * @param looper
	 * @param loopRecorder
	 * @param masterizer
	 */
	public AllModes(PatternizerModel patternizerModel, PatternizerView[] patternizerViews, Controller controller,
			Sequencer sequencer, Melodizer melodizer1, Melodizer melodizer2,
			Looper looper, LoopRecorder loopRecorder, Masterizer masterizer) {
		super();
		AllModes.patternizerModel = patternizerModel;
		AllModes.patternizerViews = patternizerViews;
		AllModes.controller = controller;
		AllModes.sequencer = sequencer;
		AllModes.melodizer1 = melodizer1;
		AllModes.melodizer2 = melodizer2;
		AllModes.looper = looper;
		AllModes.loopRecorder = loopRecorder;
		AllModes.masterizer = masterizer;
		
		theinstance=this;
	}
	
	public PatternizerView getPatternizerView(int i) {
		return patternizerViews[i];
	}
	
	public PatternizerModel getPatternizerModel(){
		return patternizerModel;
	}

	public Controller getController() {
		return controller;
	}

	public Sequencer getSequencer() {
		return sequencer;
	}

	public Melodizer getMelodizer1() {
		return melodizer1;
	}

	public Melodizer getMelodizer2() {
		return melodizer2;
	}

	public Looper getLooper() {
		return looper;
	}

	public LoopRecorder getLoopRecorder() {
		return loopRecorder;
	}

	public Masterizer getMasterizer() {
		return masterizer;
	}

}