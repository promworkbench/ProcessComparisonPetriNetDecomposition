package org.processmining.processcomparisonpetrinetdecomposition.measurements;

import java.time.Duration;
import java.util.Date;

import org.deckfour.xes.model.XTrace;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPST;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPSTNode;
import org.processmining.processcomparisonpetrinetdecomposition.util.AlignmentReplayer;
import org.processmining.processcomparisonpetrinetdecomposition.util.RPSTHelper;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public class ElapsedTimeMeasurement extends Measurement {
	private final String NAME = "Elapsed time since case start";

	public DurationMeasurementValue evaluate(PetriNetRPSTNode fragment, SyncReplayResult alignment, XTrace trace, PetriNetRPST rpst, TransEvClassMapping mapping) {
		AlignmentReplayer replayer = new AlignmentReplayer(rpst.getNet().getNet(), trace, alignment, fragment);
		
		while (replayer.hasNext()) {
			replayer.next();
			if (replayer.curStepType() == SYNC_MV && fragment.getTrans().contains(replayer.curModelStep())) {
				Date start = RPSTHelper.get_event_timestamp(trace.get(0));
				Date end = RPSTHelper.get_event_timestamp(replayer.curLogStep());
				return new DurationMeasurementValue(Duration.between(start.toInstant(), end.toInstant()));
			}
		}
		
		return null;
	}
	
	public String getName() {
		return NAME;
	}
}
