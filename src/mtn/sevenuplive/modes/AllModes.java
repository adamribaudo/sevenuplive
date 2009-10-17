package mtn.sevenuplive.modes;

import mtn.sevenuplive.modes.events.Event;
import mtn.sevenuplive.modes.events.EventDispatcher;
import mtn.sevenuplive.modes.events.EventListener;

public class AllModes implements EventDispatcher {
	
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
	 * Delegate to event dispatchers in the different modes
	 * @param event
	 */
	public void sendEvent(Event event) {
		controller.sendEvent(event);
		sequencer.sendEvent(event);
		looper.sendEvent(event);
		loopRecorder.sendEvent(event);
		masterizer.sendEvent(event);
		startup.sendEvent(event);
		patternizerModel.sendEvent(event);
		melody1Model.sendEvent(event);
		melody2Model.sendEvent(event);
	}

	public void subscribe(Event event, EventListener target) {
		controller.subscribe(event ,target);
		sequencer.subscribe(event, target);
		looper.subscribe(event, target);
		loopRecorder.subscribe(event, target);
		masterizer.subscribe(event, target);
		startup.subscribe(event, target);
		patternizerModel.subscribe(event, target);
		melody1Model.subscribe(event, target);
		melody2Model.subscribe(event, target);
	}

	public void unsubscribe(Event event, EventListener target) {
		controller.unsubscribe(event ,target);
		sequencer.unsubscribe(event, target);
		looper.unsubscribe(event, target);
		loopRecorder.unsubscribe(event, target);
		masterizer.unsubscribe(event, target);
		startup.unsubscribe(event, target);
		patternizerModel.unsubscribe(event, target);
		melody1Model.unsubscribe(event, target);
		melody2Model.unsubscribe(event, target);
		
	}
	
}
