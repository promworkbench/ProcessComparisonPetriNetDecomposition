package org.processmining.processcomparisonpetrinetdecomposition.measurements;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPST;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPSTNode;
import org.processmining.processcomparisonpetrinetdecomposition.parameters.DecompositionParameters;
import org.processmining.processcomparisonpetrinetdecomposition.util.AlignmentReplayer;
import org.processmining.processcomparisonpetrinetdecomposition.util.RPSTHelper;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public class FragmentQueueMeasurement extends Measurement{
	private final String NAME = "Fragment queue length";
	
	final String INDEX_ATTRIBUTE = "trace_idx";
	final int EXIT_ID = 1;
	final int ENTER_ID = 0;
	
	public MeasurementValue evaluate(PetriNetRPSTNode fragment, SyncReplayResult alignment, XTrace trace,
			PetriNetRPST rpst, TransEvClassMapping mapping) {
		// not implemented for this measurement because all computations can be more efficiently bundled in getFragmentMeasurements()
		return null;
	}
	
	/**
	 * For each trace calculates the number of other traces that are currently in the fragment when the trace enters the fragment. This score is then normalized by the number of traces in the log to make it comparable
	 * across different log sizes.
	 */
	@Override
	public List<MeasurementValue> getFragmentMeasurements(PetriNetRPSTNode fragment, PNRepResult alignments, XLog log, PetriNetRPST rpst, DecompositionParameters parameters, TransEvClassMapping mapping) {
		
		// get entry/ exit times for each execution of each trace (sorted by time)
		
		List<Triple<Integer, Integer, Date>> times = get_entry_exit_times(fragment, alignments, log, rpst.getNet().getNet(), mapping);
		
		// iterate over times and keep track how many traces are in the fragment, whenever a trace enters it at the current number of traces in the
		// fragment to sum_trace_scores of the trace and increase num_trace_executions by one
		
		int[] sum_trace_scores = new int[log.size()];
		int[] num_trace_executions = new int[log.size()];
		for (int i = 0; i < sum_trace_scores.length; i++) {
			sum_trace_scores[i] = 0;
			num_trace_executions[i] = 0;
		}
		
		int traces_in_fragment = 0; // keeps track of how many fragments are currently inside the fragment
		for (Triple<Integer, Integer, Date> triple : times) {
			if (triple.getMiddle() == ENTER_ID) {
				sum_trace_scores[triple.getLeft()] += traces_in_fragment;
				num_trace_executions[triple.getLeft()] += 1;
				traces_in_fragment += 1;
			}
			if (triple.getMiddle() == EXIT_ID) {
				traces_in_fragment -= 1;
			}
		}
		
		// for each trace calculate the traces score by dividing the sum of scores by the number of executions
		
		ArrayList<MeasurementValue> scores = new ArrayList<MeasurementValue>(log.size());
		for (int i = 0; i < num_trace_executions.length; i++) {
			if (num_trace_executions[i] != 0) {
				scores.add(new DoubleMeasurementValue(((double) sum_trace_scores[i] / (double) num_trace_executions[i])/log.size()));
			}
		}
		return scores;
	}
	
	private List<Triple<Integer, Integer, Date>> get_entry_exit_times(PetriNetRPSTNode fragment, PNRepResult alignments, XLog log, Petrinet net, TransEvClassMapping mapping) {
		List<Triple<Integer, Integer, Date>> times = new ArrayList<>();
		for (SyncReplayResult alignment : alignments) {
			for (int trace_idx : alignment.getTraceIndex()) {
				for (Pair<Integer, Date> pair : get_fragment_entry_exit_times(fragment, alignment, log.get(trace_idx), net, mapping)) {
					times.add(new ImmutableTriple<Integer,Integer,Date>(trace_idx, pair.getLeft(), pair.getRight()));
				}
			}
		}
		times.sort(new Comparator<Triple<Integer, Integer, Date>>() {
			@Override
			public int compare(final Triple<Integer, Integer, Date> lt, final Triple<Integer, Integer, Date> rt) {
				int date_comparison = lt.getRight().compareTo(rt.getRight());
				int entry_exit_comparison = lt.getMiddle().compareTo(rt.getMiddle());
				return date_comparison != 0 ? date_comparison : entry_exit_comparison;
			}
		});	
		return times;
	}
	
	private List<Pair<Integer, Date>> get_fragment_entry_exit_times(PetriNetRPSTNode fragment, SyncReplayResult alignment, XTrace trace, Petrinet net, TransEvClassMapping mapping) {
		AlignmentReplayer replayer = new AlignmentReplayer(net, trace, alignment, fragment);
		List<Pair<Integer, Date>> times = new ArrayList<>();
		boolean entered = replayer.hasEntered();  // will be set to true after the fragment was entered and remains true until the fragment is exited or the entry is resolved by a synchronous activity, is true by default if the start place is inside the fragment.
		boolean exited = false;  // opposite of entered
		
		Date preliminary_entry_time = null;
		Date preliminary_old_entry_time = null;
		
		while (replayer.hasNext()) {
			// set old status variables
			boolean entered_old = false; // might be redundant since an old, tracked entry is irrelevant when a new entry occurs because that means that the fragment was also exited without any synchronous activities occurring.
			preliminary_old_entry_time = null;
			boolean exited_old = false;
			
			// continue replay
			replayer.next();
			
			if (replayer.hasEntered()) {
				entered_old = entered;
				entered = true;
			}
			if (replayer.hasExited()) {
				if (replayer.exitedSyncExecution()) {
					exited_old = exited;
					exited = true;
				}
				else if (!replayer.hasEntered()) { // if an async execution was exited, we do not want to record an entry time for it
					entered = false;
				}
				else if (replayer.hasEntered()) { // if an async execution was exited and a new execution began with the same move, we want to ignore the entry of the prior async execution
					entered_old = false;
				}
			}
			if (replayer.curStepType() == SYNC_MV) { // leads to bug, where entry of asynchronous executions is counted because the entry of the execution is a synchronous activity that does not belong to the fragment. Need to temporarily store entry time and only add it on exit?
				if (entered_old) {
					preliminary_old_entry_time = RPSTHelper.get_event_timestamp(replayer.curLogStep());
					//times.add(new ImmutablePair<Integer, Date>(ENTER_ID, RPSTHelper.get_event_timestamp(replayer.curLogStep())));
					entered_old = false;
				}
				if (entered) {
					preliminary_entry_time = RPSTHelper.get_event_timestamp(replayer.curLogStep());
					//times.add(new ImmutablePair<Integer, Date>(ENTER_ID, RPSTHelper.get_event_timestamp(replayer.curLogStep())));
					entered = false;
				}
				if (exited_old) {
					times.add(new ImmutablePair<Integer, Date>(EXIT_ID, RPSTHelper.get_event_timestamp(replayer.curLogStep())));
					exited_old = false;
				}
				if (exited) {
					times.add(new ImmutablePair<Integer, Date>(EXIT_ID, RPSTHelper.get_event_timestamp(replayer.curLogStep())));
					exited = false;
				}
			}
			if (replayer.isInSyncExecution() && preliminary_entry_time != null) {
				times.add(new ImmutablePair<Integer, Date>(ENTER_ID, preliminary_entry_time));
				preliminary_entry_time = null;
			}
			if (replayer.exitedSyncExecution()) {
				if (preliminary_old_entry_time != null) {
					times.add(new ImmutablePair<Integer, Date>(ENTER_ID, preliminary_old_entry_time));
					preliminary_old_entry_time = null;
				}
				if (preliminary_entry_time != null) {
					times.add(new ImmutablePair<Integer, Date>(ENTER_ID, preliminary_entry_time));
					preliminary_entry_time = null;
				}
			}
		}
		return times;
	}

	public String getName() {
		return NAME;
	}

}
