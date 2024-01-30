package org.processmining.processcomparisonpetrinetdecomposition.measurements;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPST;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPSTNode;
import org.processmining.processcomparisonpetrinetdecomposition.parameters.DecompositionParameters;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public abstract class Measurement {
	public static final StepTypes LOG_MV = StepTypes.L; 
	public static final StepTypes MODEL_MV = StepTypes.MREAL;
	public static final StepTypes INVISIBLE_MV = StepTypes.MINVI;
	public static final StepTypes SYNC_MV = StepTypes.LMGOOD;
	
	/**
	 * Evaluates the measurement value of the given fragment and aligned trace.
	 * @param fragment
	 * @param alignment
	 * @param trace
	 * @param rpst
	 * @return MeasurementValue assigned by the measurement
	 */
	public abstract MeasurementValue evaluate(PetriNetRPSTNode fragment, SyncReplayResult alignment, XTrace trace, PetriNetRPST rpst, TransEvClassMapping mapping);
	
	
	/**
	 * 
	 * @param fragment
	 * @param alignments
	 * @param log
	 * @param rpst
	 * @param parameters
	 * @return
	 */
	public List<MeasurementValue> getFragmentMeasurements(PetriNetRPSTNode fragment, PNRepResult alignments, XLog log, PetriNetRPST rpst, DecompositionParameters parameters, TransEvClassMapping mapping) {
		ArrayList<MeasurementValue> scores = new ArrayList<MeasurementValue>(log.size());
		// iterate over all alignments; for each alignment iterate over all traces of this variant and calculate their scores
		for (SyncReplayResult alignment : alignments) {
			for (int trace_idx : alignment.getTraceIndex()) {
				MeasurementValue score = evaluate(fragment, alignment, log.get(trace_idx), rpst, mapping);
				if (score != null) {
					scores.add(score);
				}
			}
		}
		return scores;
	} 
	
	
	/**
	 * Returns the name of the measurement for use in the UI
	 * @return String name
	 */
	public abstract String getName();
}
