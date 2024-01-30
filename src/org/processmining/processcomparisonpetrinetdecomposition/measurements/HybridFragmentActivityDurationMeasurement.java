package org.processmining.processcomparisonpetrinetdecomposition.measurements;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
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

public class HybridFragmentActivityDurationMeasurement extends Measurement {

	private final String NAME = "Hybrid Intra-fragment Activity Execution Duration";

	/**
	 * Set of transitions that a causal predecessors of this fragment
	 */
	private Set<Transition> fragNCausalPredTrans;;
	
	@Override
	public ArrayList<MeasurementValue> getFragmentMeasurements(PetriNetRPSTNode fragment, PNRepResult alignments,
			XLog log, PetriNetRPST rpst, DecompositionParameters parameters, TransEvClassMapping mapping) {
		ArrayList<MeasurementValue> scores = new ArrayList<MeasurementValue>(log.size());

		boolean isVisibleEdgeFragment = false;
		if (fragment.getArcs().size() == 1) {
			if (fragment.getExit() instanceof Transition) {
				Transition t = (Transition) fragment.getExit();
				if (!t.isInvisible()) {
					fragNCausalPredTrans = TimeInFragmentMeasurement.searchCausalPredSuccTransitions(fragment, 
							rpst.getNet().getNet(), true);
					isVisibleEdgeFragment = true;
				}
			}
		}

		for (SyncReplayResult alignment : alignments) {
			// For each alignment, we can re-use the event POSITIONS for each trace
			List<Pair<Integer, Integer>> execEventBoundaries;
			if (isVisibleEdgeFragment) {
				execEventBoundaries = getMeasurementEventIndicesEdgeVisible(fragment, alignment,
					log.get(alignment.getTraceIndex().first()), rpst);
			}
			else {
				execEventBoundaries = getMeasurementEventIndices(fragment, alignment,
						log.get(alignment.getTraceIndex().first()), rpst);
			}
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
				// If there was at least one synchronous move, save the duration
				if (firstIntraFragSyncEvent != -1 && lastIntraFragSyncEvent != -1) {
					fragmentMeasurements.add(Pair.of(firstIntraFragSyncEvent, lastIntraFragSyncEvent));
				}
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

	public List<Pair<Integer, Integer>> getMeasurementEventIndicesEdgeVisible(PetriNetRPSTNode fragment,
			SyncReplayResult alignment, XTrace trace, PetriNetRPST rpst) {
		AlignmentReplayer replayer = new AlignmentReplayer(rpst.getNet().getNet(), trace, alignment, fragment);

		List<Pair<Integer, Integer>> fragmentMeasurements = new LinkedList<>();

		// Index of the last synchronous move that is
		// a causal predecessor of the fragment 
		// Assume that first event (i.e., case start is always a predecessor
		// (even though the corresponding transition might not)
		// Note that we assume that the log does not contain empty traces.
		int indexLastSyncCausalPred = 0;

		while (replayer.hasNext()) {
			replayer.next();

			// Can do something with the event
			if (replayer.curStepType() == SYNC_MV) {
				Transition modelStep = replayer.curModelStep();
				// Update causal predecessor
				if (fragNCausalPredTrans.contains(modelStep) 
						&& !fragment.getTrans().contains(modelStep)) {
					indexLastSyncCausalPred = replayer.curLogIndex();
				}
			}

			// Fragment exited, handle before entries
			// In case the same step exits and enters the fragment
			if (replayer.hasExited() && replayer.didLastExitedExecActivateFragTrans()) {
				// Synchronous fragment execution? -> Fragment is single edge
				if (replayer.curStepType() == SYNC_MV) {
					fragmentMeasurements.add(Pair.of(indexLastSyncCausalPred, replayer.curLogIndex()));
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
			Date start = RPSTHelper.get_event_timestamp(trace.get(fragExec.getLeft()));
			Date end = RPSTHelper.get_event_timestamp(trace.get(fragExec.getRight()));
			return new DurationMeasurementValue(Duration.between(start.toInstant(), end.toInstant()));
		} else {
			return (new DurationMeasurementValue()).getAverage(
					fragExecEventBoundaries.stream()
						.map(fragExec -> {
							Date start = RPSTHelper.get_event_timestamp(trace.get(fragExec.getLeft()));
							Date end = RPSTHelper.get_event_timestamp(trace.get(fragExec.getRight()));
							return new DurationMeasurementValue(Duration.between(start.toInstant(), end.toInstant()));
						})
						.collect(Collectors.toList()));
		}
	}

	public String getName() {
		return NAME;
	}
}
