package org.processmining.processcomparisonpetrinetdecomposition.measurements;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPST;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPSTNode;
import org.processmining.processcomparisonpetrinetdecomposition.parameters.DecompositionParameters;
import org.processmining.processcomparisonpetrinetdecomposition.util.AlignmentReplayer;
import org.processmining.processcomparisonpetrinetdecomposition.util.RPSTHelper;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public class FragmentActivityDurationMeasurement extends Measurement {

	private final String NAME = "Fragment execution duration (intra-fragment activities)";
	
		@Override
	public ArrayList<MeasurementValue> getFragmentMeasurements(PetriNetRPSTNode fragment, PNRepResult alignments,
			XLog log, PetriNetRPST rpst, DecompositionParameters parameters, TransEvClassMapping mapping) {
		ArrayList<MeasurementValue> scores = new ArrayList<MeasurementValue>(log.size());

		for (SyncReplayResult alignment : alignments) {
			// For each alignment, we can re-use the event POSITIONS for each trace
			List<Pair<Integer, Integer>> execEventBoundaries = getMeasurementEventIndices(fragment, alignment,
					log.get(alignment.getTraceIndex().first()), rpst);
			// Extract measurements based on indices
			for (int traceIdx : alignment.getTraceIndex()) {
				MeasurementValue score = extractMeasurementFromTrace(execEventBoundaries, log.get(traceIdx));
				if (score != null) {
					scores.add(score);
				}
			}
		}
		return scores;
	}

	@Override
	public MeasurementValue evaluate(PetriNetRPSTNode fragment, SyncReplayResult alignment, XTrace trace,
			PetriNetRPST rpst, TransEvClassMapping mapping) {
		return null;
	}
	
	public List<Pair<Integer, Integer>> getMeasurementEventIndices(PetriNetRPSTNode fragment,
			SyncReplayResult alignment, XTrace trace, PetriNetRPST rpst) {
		AlignmentReplayer replayer = new AlignmentReplayer(rpst.getNet().getNet(), trace, alignment, fragment);

		List<Pair<Integer, Integer>> fragmentMeasurements = new LinkedList<>();
		
		// Event index of first synchronous move in fragment
		int firstIntraFragSyncEvent = -1;

		// Event index of latest synchronous move in fragment
		int lastIntraFragSyncEvent = -1;

		while (replayer.hasNext()) {
			replayer.next();

			// Fragment exited, handle before entries
			// In case the same step exits and enters the fragment
			if (replayer.hasExited() && replayer.didLastExitedExecActivateFragTrans()) {
				// If exiting move is synchronous and part of fragment
				// Update the event indices of last and possibly first synchronous move
				if (replayer.curStepType() == SYNC_MV && fragment.getTrans().contains(replayer.curModelStep()))  {
					int curLogStep = replayer.curLogIndex();
					if (firstIntraFragSyncEvent == -1) {
						firstIntraFragSyncEvent = curLogStep;
					}
					lastIntraFragSyncEvent = curLogStep;

				}
				// Save execution (even if zero synchronous events)
				fragmentMeasurements.add(Pair.of(firstIntraFragSyncEvent, lastIntraFragSyncEvent));
				firstIntraFragSyncEvent = lastIntraFragSyncEvent = -1;
			}
			
			// Update intra-fragment synchronous event indices
			if (replayer.isInFragment()) {
				if (replayer.curStepType() == SYNC_MV && fragment.getTrans().contains(replayer.curModelStep()))  {
					int curLogStep = replayer.curLogIndex();
					if (firstIntraFragSyncEvent == -1) {
						firstIntraFragSyncEvent = curLogStep;
					}
					lastIntraFragSyncEvent = curLogStep;
				}
			}
		}
		
		return fragmentMeasurements;
	}

	private DurationMeasurementValue extractMeasurementFromTrace(List<Pair<Integer, Integer>> fragExecEventBoundaries,
			XTrace trace) {
		if (fragExecEventBoundaries.size() == 0) {
			return null;
		} else if (fragExecEventBoundaries.size() == 1) {
			Pair<Integer, Integer> fragExec = fragExecEventBoundaries.get(0);
			// Execution without related events
			if (fragExec.getLeft() == -1 && fragExec.getRight() == -1) {
				return new DurationMeasurementValue(Duration.ZERO);
			}
			else if (fragExec.getLeft() != -1 && fragExec.getRight() != -1) {
				Date start = RPSTHelper.get_event_timestamp(trace.get(fragExec.getLeft()));
				Date end = RPSTHelper.get_event_timestamp(trace.get(fragExec.getRight()));
				return new DurationMeasurementValue(Duration.between(start.toInstant(), end.toInstant()));
			}
			else {
				System.out.println("Problem - underspecified measurement");
				return new DurationMeasurementValue(Duration.ZERO);
			}
		} else {
			return (new DurationMeasurementValue()).getAverage(
					fragExecEventBoundaries.stream()
						.map(fragExec -> {
							if (fragExec.getLeft() == -1 && fragExec.getRight() == -1) {
								return new DurationMeasurementValue(Duration.ZERO);
							}
							else if (fragExec.getLeft() != -1 && fragExec.getRight() != -1) {
								Date start = RPSTHelper.get_event_timestamp(trace.get(fragExec.getLeft()));
								Date end = RPSTHelper.get_event_timestamp(trace.get(fragExec.getRight()));
								return new DurationMeasurementValue(Duration.between(start.toInstant(), end.toInstant()));
							}
							else {
								System.out.println("Problem - underspecified measurement");
								return new DurationMeasurementValue(Duration.ZERO);
							}
						})
						.collect(Collectors.toList()));
		}
	}

	public String getName() {
		return NAME;
	}

}
