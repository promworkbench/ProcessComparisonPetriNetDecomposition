package org.processmining.processcomparisonpetrinetdecomposition.measurements;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.model.XTrace;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPST;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPSTNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public class IndependentReachedMeasurement extends Measurement {
	private final String NAME = "Conditional subprocess activition (occurrence of parent)";

	public IntMeasurementValue evaluate(PetriNetRPSTNode fragment, SyncReplayResult alignment, XTrace trace, PetriNetRPST rpst, TransEvClassMapping mapping) {
		// get parent fragment
		PetriNetRPSTNode parent = null;
		if (rpst.getTree().getPredecessorCount(fragment) > 0) {
			parent = rpst.getTree().getPredecessors(fragment).iterator().next();
		}
		
		Set<Transition> fragment_trans = fragment.getTrans();
		
		Set<Transition> parent_trans;
		if (parent != null) {
			parent_trans = parent.getTrans();
		}
		else {
			parent_trans = new HashSet<>();
		}
		
		List<StepTypes> step_types = alignment.getStepTypes();
		List<Object> model_steps = alignment.getNodeInstance();
		
		boolean entered_parent = false;
		
		for (int i = 0; i < model_steps.size(); i++) {
			if (step_types.get(i).equals(SYNC_MV) || step_types.get(i).equals(INVISIBLE_MV)) {
				Transition cur_trans = (Transition)model_steps.get(i);
				if (parent_trans.contains(cur_trans)) {
					entered_parent = true;
					if (fragment_trans.contains(cur_trans)) {//fragment_trans_labels.contains(cur_trans.getLabel())) {
						return new IntMeasurementValue(1);
					}
				}
			}
		}
		if (entered_parent || parent == null) {
			return new IntMeasurementValue(0);
		}
		else {
			return null;
		}
	}
	
	public String getName() {
		return NAME;
	}
}
