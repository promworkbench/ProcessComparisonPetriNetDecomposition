package org.processmining.processcomparisonpetrinetdecomposition.measurements;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPST;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPSTNode;
import org.processmining.processcomparisonpetrinetdecomposition.parameters.DecompositionParameters;
import org.processmining.processcomparisonpetrinetdecomposition.util.AlignmentReplayer;
import org.processmining.processcomparisonpetrinetdecomposition.util.RPSTHelper;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public class TimeInFragmentMeasurement extends Measurement {

	private final String NAME = "Subprocess cycle time (approx. conditional predecessor/successor)";
	/**
	 * Set of transitions that a causal predecessors of this fragment
	 */
	private Set<Transition> fragNCausalPredTrans;;

	/**
	 * Set of transitions that a causal predecessors of this fragment
	 */
	private Set<Transition> fragNCausalSuccTrans;

	@Override
	public ArrayList<MeasurementValue> getFragmentMeasurements(PetriNetRPSTNode fragment, PNRepResult alignments,
			XLog log, PetriNetRPST rpst, DecompositionParameters parameters, TransEvClassMapping mapping) {
		ArrayList<MeasurementValue> scores = new ArrayList<MeasurementValue>(log.size());

		fragNCausalPredTrans = searchCausalPredSuccTransitions(fragment, rpst.getNet().getNet(), true);
		fragNCausalSuccTrans = searchCausalPredSuccTransitions(fragment, rpst.getNet().getNet(), false);

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

		// Index of the last synchronous move that is
		// a causal predecessor of the fragment or contained in the fragment;
		// Assume that first event (i.e., case start is always a predecessor
		// (even though the corresponding transition might not)
		// Note that we assume that the log does not contain empty traces.
		int indexLastSyncFragOrCausalPred = 0;

		// Indices of events that marks the exit of this subprocess
		// 1. The non-subprocess transition that consumed the last token
		// 2. A causal successor of the former 
		// 3. The case end
		// Right element is initially a fallback exit if it exists (last sync move in fragment)
		// ----------
		// For non-SESE subprocesses and crazy nets,
		// it might make sense to have subprocesses executions and loops
		// only made of model moves to create tokens somewhere else in the net
		int indexEntryCur = -1;
		List<Pair<Integer, Integer>> pendingEntries = new ArrayList<Pair<Integer, Integer>>(100);
		
		// Check if fragment contains initial place
		if (replayer.hasEntered()) {
			indexEntryCur = indexLastSyncFragOrCausalPred;
		}
		
		//if (fragment.getName().contains("xor_") && (fragment.getName().contains("P") || fragment.getName().contains("SF"))) {
		//	System.out.println("Check");
		//}

		while (replayer.hasNext()) {
			replayer.next();

			// Can do something with the event
			if (replayer.curStepType() == SYNC_MV) {
				Transition modelStep = replayer.curModelStep();
				// Update causal predecessor
				// Note that this also includes all synchronous intra-fragment moves
				if (fragNCausalPredTrans.contains(modelStep)) {
					indexLastSyncFragOrCausalPred = replayer.curLogIndex();
				}

				// Causally related successors close pending executions
				if (!pendingEntries.isEmpty() && fragNCausalSuccTrans.contains(modelStep)) {
					// Add new measurements
					int indexLog = replayer.curLogIndex();
					for (Pair<Integer, Integer> p : pendingEntries) {
						fragmentMeasurements.add(Pair.of(p.getLeft(), indexLog));
					}
					// Close 
					pendingEntries.clear();
				}
			}

			// Fragment exited, handle before entries
			// In case the same step exits and enters the fragment
			if (replayer.hasExited() && replayer.didLastExitedExecActivateFragTrans()) {
				// We enter and exit (in "this order") a fragment within a single move
				// Can happen for subprocesses that contain the final place.
				// Transition enters and puts token in final place. If there is not subsequent move
				// we consider the execution terminated  
				if (indexEntryCur == -1) {
					indexEntryCur = indexLastSyncFragOrCausalPred;
				}
				// We can directly use the associated event 
				if (replayer.curStepType() == SYNC_MV) {
					fragmentMeasurements.add(Pair.of(indexEntryCur, replayer.curLogIndex()));
				}
				// Model move (e.g., invisible transition)
				// => Need to search for a causal successor
				else {
					pendingEntries.add(Pair.of(indexEntryCur, indexLastSyncFragOrCausalPred));
				}
				indexEntryCur = -1;
			}

			// Set entry time when entered
			if (replayer.hasEntered()) {
				indexEntryCur = indexLastSyncFragOrCausalPred;
			}
		}
		
		// At pending measurements
		for (Pair<Integer, Integer> p : pendingEntries) {
			fragmentMeasurements.add(Pair.of(p.getLeft(), p.getRight()));
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

	public static Set<Transition> searchCausalPredSuccTransitions(PetriNetRPSTNode fragment, Petrinet net, boolean prior) {
		Set<Transition> causallyRelatedTrans = new HashSet<>();

		// Nodes to search in BFS
		Stack<PetrinetNode> stack = new Stack<>();
		// Nodes already visited
		Set<PetrinetNode> grayNodes = new HashSet<>();

		// TODO Wrong: t -> p (if token on p and we consider another successor of t)
		// Since the fragment might have multiple boundary nodes, 
		// start a search from all members
		stack.addAll(fragment.getPlaces());
		stack.addAll(fragment.getTrans());

		while (!stack.isEmpty()) {
			PetrinetNode cur = stack.pop();

			// Add to visited collection
			grayNodes.add(cur);

			// Each transition in list is causally related 
			// => So are all transitions in fragment 
			if (cur instanceof Transition) {
				causallyRelatedTrans.add((Transition) cur);
			}

			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edges;
			// Add predecessors/successors to search queue
			if (prior) {
				// Predecessors
				net.getInEdges(cur).stream().map(PetrinetEdge::getSource).filter(n -> !grayNodes.contains(n)) // not visited yet
						.forEach(stack::add);
			} else {
				// Successors
				net.getInEdges(cur).stream().map(PetrinetEdge::getTarget).filter(n -> !grayNodes.contains(n)) // not visited yet
						.forEach(stack::add);
			}
		}

		return causallyRelatedTrans;
	}

	public String getName() {
		return NAME;
	}

}
