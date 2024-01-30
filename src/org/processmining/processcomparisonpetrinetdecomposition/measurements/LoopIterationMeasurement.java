package org.processmining.processcomparisonpetrinetdecomposition.measurements;

import org.deckfour.xes.model.XTrace;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPST;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPSTNode;
import org.processmining.processcomparisonpetrinetdecomposition.util.AlignmentReplayer;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public class LoopIterationMeasurement extends Measurement {
	private final String NAME = "Loop iteration";

	/**
	 * Count the number of synchronous executions. Return the amount if it is greater than zero and null else
	 */
	public IntMeasurementValue evaluate(PetriNetRPSTNode fragment, SyncReplayResult alignment, XTrace trace, PetriNetRPST rpst, TransEvClassMapping mapping) {
		int num_iterations = 0;
		
		AlignmentReplayer replayer = new AlignmentReplayer(rpst.getNet().getNet(), trace, alignment, fragment);
		while (replayer.hasNext()) {
			replayer.next();
			if (replayer.hasExited() && replayer.exitedSyncExecution()) {
				num_iterations += 1;
			}
		}
		
		return num_iterations == 0 ? null : new IntMeasurementValue(num_iterations);
	}
	
	public String getName() {
		return NAME;
	}
}
