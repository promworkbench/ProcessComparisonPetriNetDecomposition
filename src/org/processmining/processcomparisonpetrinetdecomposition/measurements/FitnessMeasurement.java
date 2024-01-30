package org.processmining.processcomparisonpetrinetdecomposition.measurements;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPST;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPSTNode;
import org.processmining.processcomparisonpetrinetdecomposition.parameters.DecompositionParameters;
import org.processmining.processcomparisonpetrinetdecomposition.util.AlignmentReplayer;
import org.processmining.processcomparisonpetrinetdecomposition.util.RPSTHelper;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public class FitnessMeasurement extends Measurement {
	private final String NAME = "Fragment execution fitness";
	private double shortest_path_through_frag;

	public MeasurementValue evaluate(PetriNetRPSTNode fragment, SyncReplayResult alignment, XTrace trace, PetriNetRPST rpst, TransEvClassMapping mapping) {
		// initialize necessary trace and fragment information
		Set<Transition> fragment_trans = fragment.getTrans();
		List<String> fragment_ev_ids = new ArrayList<>();
		for (Transition trans : fragment_trans) {
			fragment_ev_ids.add(mapping.get(trans).getId());
		}
		XEventClassifier ev_classifier = mapping.getEventClassifier();
		// iterate over alignment, for each fragment transition/ activity check fitness
		double cost = 0; // counter for the alignment cost
		int projected_trace_length = 0; // counter for the length of the trace projected on the fragment activities
				
		AlignmentReplayer replayer = new AlignmentReplayer(rpst.getNet().getNet(), trace, alignment, fragment);
		while (replayer.hasNext()) {
			replayer.next();
			if (replayer.curStepType().equals(SYNC_MV) && fragment_trans.contains(replayer.curModelStep())) { // cost: 0, increases projected trace length
				projected_trace_length += 1;
			}
			else if (replayer.curStepType().equals(MODEL_MV) && fragment_trans.contains(replayer.curModelStep())) { // cost: 1, does not change projected trace length
				cost += 1;
			}
			else if (replayer.curStepType().equals(LOG_MV) && fragment_ev_ids.contains(ev_classifier.getClassIdentity(replayer.curLogStep()))) { // cost: 1, increases projected trace length
				cost += 1;
				projected_trace_length += 1;
			} 
		}
		
		if (projected_trace_length == 0) return null; // trace does not enter fragment, i.e. irrelevant
		double fitness = 1 - (cost/(projected_trace_length+shortest_path_through_frag));
		return new DoubleMeasurementValue(fitness);	
	}
	
	@Override
	public List<MeasurementValue> getFragmentMeasurements(PetriNetRPSTNode fragment, PNRepResult alignments, XLog log, PetriNetRPST rpst, DecompositionParameters parameters, TransEvClassMapping mapping) {
		// compute the shortest path through the fragment once for the given fragment
		shortest_path_through_frag = RPSTHelper.shortestPathThroughModelLength(fragment);
		
		ArrayList<MeasurementValue> scores = new ArrayList<MeasurementValue>(log.size());
		
		// test if all transitions are silent
		boolean all_invisible = true;
		for (Transition trans : fragment.getTrans()) {
			if (!trans.isInvisible()) {
				all_invisible = false;
				break;
			}
		}
		if (all_invisible) {
			for (int i = 0; i < log.size(); i++) {
				scores.add(new DoubleMeasurementValue(1.0));
			}
			return scores;
		}
		
		// iterate over all alignments; for each alignment iterate over all traces of this variant and calculate their scores
		for (SyncReplayResult alignment : alignments) {
			for (int trace_idx : alignment.getTraceIndex()) {
				MeasurementValue score = evaluate(fragment, alignment, log.get(trace_idx), rpst, mapping);
				if (score != null) {
					scores.add(score);
				}
			}
		}
		if (scores.isEmpty()) {
			for (int i = 0; i < log.size(); i++) {
				scores.add(new DoubleMeasurementValue(1.0));
			}
		}
		return scores;
	} 
	
	public String getName() {
		return NAME;
	}
}
