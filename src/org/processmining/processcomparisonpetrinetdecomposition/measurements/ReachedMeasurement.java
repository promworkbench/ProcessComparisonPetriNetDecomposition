package org.processmining.processcomparisonpetrinetdecomposition.measurements;

import java.util.List;
import java.util.Set;

import org.deckfour.xes.model.XTrace;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPST;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPSTNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public class ReachedMeasurement extends Measurement {
	private final String NAME = "Subprocess activated";

	public IntMeasurementValue evaluate(PetriNetRPSTNode fragment, SyncReplayResult alignment, XTrace trace, PetriNetRPST rpst, TransEvClassMapping mapping) {
		// compare labels of the transitions
		//HashSet<String> fragment_trans_labels = new HashSet<String>();
		Set<Transition> fragment_trans = fragment.getTrans();
		//for (Transition trans : fragment.getTrans()) {
		//	fragment_trans_labels.add(trans.getLabel());
		//}
		
		List<StepTypes> step_types = alignment.getStepTypes();
		List<Object> model_steps = alignment.getNodeInstance();
		
		for (int i = 0; i < model_steps.size(); i++) {
			if (step_types.get(i).equals(SYNC_MV) || step_types.get(i).equals(INVISIBLE_MV)) {
				Transition cur_trans = (Transition)model_steps.get(i);
				if (fragment_trans.contains(cur_trans)) {//fragment_trans_labels.contains(cur_trans.getLabel())) {
					return new IntMeasurementValue(1);
				}
			}
		}
		return new IntMeasurementValue(0);	
	}
	
	public String getName() {
		return NAME;
	}
}
