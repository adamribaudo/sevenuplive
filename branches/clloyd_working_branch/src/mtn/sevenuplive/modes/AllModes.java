package mtn.sevenuplive.modes;

public class AllModes {
	
	// A bit of cheating but allow direct reference for classes in this package
	// as the getters are too verbose here
	static PatternizerModel patternizerModel;
	static MelodizerModel melody1Model;
	static MelodizerModel melody2Model;
	static Controller controller;
	static Sequencer sequencer;
	static Looper looper;
	static LoopRecorder loopRecorder;
	static Masterizer masterizer;
	static StartupMode startup;
	static PatternizerView patternizerViews[];
	static MelodizerView melodizer1Views[];
	static MelodizerView melodizer2Views[];

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
	 * @param startup 
	 */
	public AllModes(PatternizerModel patternizerModel, PatternizerView[] patternizerViews, Controller controller,
			Sequencer sequencer, MelodizerModel melody1Model, MelodizerView[] melodizer1Views, MelodizerModel melody2Model, MelodizerView[] melodizer2Views,
			Looper looper, LoopRecorder loopRecorder, Masterizer masterizer, StartupMode startup) {
		super();
		AllModes.patternizerModel = patternizerModel;
		AllModes.patternizerViews = patternizerViews;
		AllModes.controller = controller;
		AllModes.sequencer = sequencer;
		AllModes.melody1Model = melody1Model;
		AllModes.melody2Model = melody2Model;
		AllModes.melodizer1Views = melodizer1Views;
		AllModes.melodizer2Views = melodizer2Views;
		AllModes.looper = looper;
		AllModes.loopRecorder = loopRecorder;
		AllModes.masterizer = masterizer;
		AllModes.startup = startup;
		
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

	public MelodizerView getMelodizer1View(int i) {
		return melodizer1Views[i];
	}

	public MelodizerView getMelodizer2View(int i) {
		return melodizer2Views[i];
	}
	
	public MelodizerModel getMelodizer1Model() {
		return melody1Model;
	}

	public MelodizerModel getMelodizer2Model() {
		return melody2Model;
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
	
	public StartupMode getStartupMode() {
		return startup;
	}
	
	/**
	 * Hardcoded subscription list for now
	 * @param event
	 */
	public void sendMenuFocusChangeEvent(Mode.MenuFocusEvent event) {
		controller.onMenuFocusChange(event);
		sequencer.onMenuFocusChange(event);
		looper.onMenuFocusChange(event);
		loopRecorder.onMenuFocusChange(event);
		masterizer.onMenuFocusChange(event);
		startup.onMenuFocusChange(event);
		for (int i = 0; i < patternizerViews.length; i++) {
			patternizerViews[i].onMenuFocusChange(event);
			melodizer1Views[i].onMenuFocusChange(event);
			melodizer2Views[i].onMenuFocusChange(event);
		}
	}
	
}
