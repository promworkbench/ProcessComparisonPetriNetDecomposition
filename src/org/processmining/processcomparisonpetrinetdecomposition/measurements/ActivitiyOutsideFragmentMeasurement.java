package org.processmining.processcomparisonpetrinetdecomposition.measurements;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XTrace;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPST;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPSTNode;
import org.processmining.processcomparisonpetrinetdecomposition.util.AlignmentReplayer;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public class ActivitiyOutsideFragmentMeasurement extends Measurement{
	private final String NAME = "Activity occurred outside of fragment";
	
	public MeasurementValue evaluate(PetriNetRPSTNode fragment, SyncReplayResult alignment, XTrace trace,
			PetriNetRPST rpst, TransEvClassMapping mapping) { 
		List<String> fragment_ev_ids = new ArrayList<>();
		for (Transition trans : fragment.getTrans()) {
			fragment_ev_ids.add(mapping.get(trans).getId());
		}
		XEventClassifier ev_classifier = mapping.getEventClassifier();
		
		// use alignment replay to count the tokens currently in the fragment and determine if the current activity belongs to a fragment execution
		AlignmentReplayer replayer = new AlignmentReplayer(rpst.getNet().getNet(), trace, alignment, fragment);
		int counter = 0; // counts how many fragment activities occur outside of executions
		boolean was_executed = replayer.isInFragment();  // remove if not normalized by fragment occurrences only
		
		while (replayer.hasNext()) {
			replayer.next();
			if (replayer.curStepType().equals(LOG_MV) && !replayer.isInFragment() && fragment_ev_ids.contains(ev_classifier.getClassIdentity(replayer.curLogStep()))) {
				counter += 1;
			}
			if (replayer.isInFragment()) {
				was_executed = true;  // remove if not normalized by fragment occurrences only
			}
		}
		if (!was_executed && counter == 0) {  
			return null;  // remove if not normalized by fragment occurrences only
		}
		return new IntMeasurementValue(counter);
	}

	public String getName() {
		return NAME;
	}
}
