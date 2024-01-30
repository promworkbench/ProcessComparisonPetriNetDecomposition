package org.processmining.processcomparisonpetrinetdecomposition.measurements;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.extension.std.XExtendedEvent;
import org.deckfour.xes.model.XEvent;
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

/**
 * A measurement that is similar to the TimeInFragment measurement, 
 * but considers a fragment to be finished once the exit place is marked, or, 
 * if the exit node is a transition that has an ingoing arc from outside the fragment, 
 * once all places that activate it are marked in the fragment.
 * @author Morit
 *
 */
public class FragmentWaitTimeMeasurement extends Measurement{
	private final String NAME = "Subprocess cycle time (approx. conditional predecessor only)";
	ArrayList<Transition> prior_trans;
	ArrayList<Transition> post_trans;
	ArrayList<Transition> exit_trans;  // might not be necessary but needs proof
	boolean has_independent_exit_trans; // false if the exit node is a transition and it has an ingoing arc from outside the fragment, else true
	

	/**
	 * For the most part, this function is identical to the one in the TimeInFragmentMeasuremet. The only thing that changes is the condition of fragment exit and the assigning of the exit timestamp.
	 * 
	 */
	public MeasurementValue evaluate(PetriNetRPSTNode fragment, SyncReplayResult alignment, XTrace trace, PetriNetRPST rpst, TransEvClassMapping mapping) {
		AlignmentReplayer replayer = new AlignmentReplayer(rpst.getNet().getNet(), trace, alignment, fragment);
		
		// if final place is the exit place of the fragment set has_independent_exit_trans to true because in this case the last transition ends the trace and thus also the execution
		if (rpst.getNet().getNet().getOutEdges(fragment.getExit()).isEmpty()) has_independent_exit_trans = true; 
		
		Set<Transition> fragment_trans = fragment.getTrans();
		Date latest_timestamp = null;
		Date latest_post_timestamp = null;
		Date cur_timestamp_entry = null;
		Date old_timestamp_entry = null;
		Date cur_timestamp_exit = null;
		boolean looking_for_entry_time = false;
		boolean looking_for_exit_time = false;
		
		boolean last_frag_trans_sync = false;
		Date last_frag_trans_date = null;
		
		ArrayList<Pair<Date,Date>> timestamps = new ArrayList<>();
		
		// check if fragment contains initial place, then cur_timestamp_entry = start time
		if (replayer.hasEntered()) {
			cur_timestamp_entry = RPSTHelper.get_event_timestamp(trace.get(0));
		}
		
		while (replayer.hasNext()) {
			replayer.next();
			if (looking_for_entry_time && replayer.curStepType() == SYNC_MV && fragment_trans.contains(replayer.curModelStep())) {
				cur_timestamp_entry = RPSTHelper.get_event_timestamp(replayer.curLogStep());
				looking_for_entry_time = false;
			}
			if (looking_for_exit_time && replayer.curStepType() == SYNC_MV && post_trans.contains(replayer.curModelStep())) {
				cur_timestamp_exit = RPSTHelper.get_event_timestamp(replayer.curLogStep());
				
				if (old_timestamp_entry == null) { // fragment only contains final place and transition before final place -> set entry time to exit time
					old_timestamp_entry = RPSTHelper.copy_timestamp(cur_timestamp_exit);
				}
				
				timestamps.add(new ImmutablePair<Date,Date>(old_timestamp_entry, cur_timestamp_exit));
				looking_for_exit_time = false;
			}
			if (replayer.curStepType() == SYNC_MV && prior_trans.contains(replayer.curModelStep())) {
				latest_timestamp = RPSTHelper.get_event_timestamp(replayer.curLogStep());
			}
			if (replayer.curStepType() == SYNC_MV && post_trans.contains(replayer.curModelStep())) {
				latest_post_timestamp = RPSTHelper.get_event_timestamp(replayer.curLogStep());
			}
			if (replayer.hasExited()) {
				if (replayer.exitedSyncExecution()) {
					if (has_independent_exit_trans && replayer.curStepType() == SYNC_MV) {
						cur_timestamp_exit = RPSTHelper.get_event_timestamp(replayer.curLogStep());
						
						if (cur_timestamp_entry == null) { // fragment only contains final place and transition before final place -> set entry time to exit time
							cur_timestamp_entry = RPSTHelper.copy_timestamp(cur_timestamp_exit);
						}
						
						timestamps.add(new ImmutablePair<Date,Date>(cur_timestamp_entry, cur_timestamp_exit));
					}
					else {
						if( last_frag_trans_date != null) {
							cur_timestamp_exit = RPSTHelper.copy_timestamp(last_frag_trans_date);
							
							if (cur_timestamp_entry == null) { // fragment only contains final place and transition before final place -> set entry time to exit time
								cur_timestamp_entry = RPSTHelper.copy_timestamp(cur_timestamp_exit);
							}
							
							timestamps.add(new ImmutablePair<Date,Date>(cur_timestamp_entry, cur_timestamp_exit));
						}
					}
//					else { // exit is either dependent transition or place -> consider last fragment transition, if it was synchronous, else look for exit later
//						if (!has_independent_exit_trans && last_frag_trans_sync) {
//							cur_timestamp_exit = RPSTHelper.copy_timestamp(last_frag_trans_date);
//							
//							if (cur_timestamp_entry == null) { // fragment only contains final place and transition before final place -> set entry time to exit time
//								cur_timestamp_entry = RPSTHelper.copy_timestamp(cur_timestamp_exit);
//							}
//							
//							timestamps.add(new ImmutablePair<Date,Date>(cur_timestamp_entry, cur_timestamp_exit));
//						}
//						else {							
//							looking_for_exit_time = true;
//							if (cur_timestamp_entry == null) {
//								old_timestamp_entry = null;
//							}
//							else {
//								old_timestamp_entry = RPSTHelper.copy_timestamp(cur_timestamp_entry);
//							}
//						}
//					}
				}
				else {
					looking_for_entry_time = false;
				}
			}
			if (replayer.hasEntered()) {
				if (latest_timestamp != null) {
					cur_timestamp_entry = latest_timestamp;
				}
				else {
					looking_for_entry_time = true;
				}
			}
			if (replayer.isInFragment() && (replayer.curStepType() == SYNC_MV || replayer.curStepType() == MODEL_MV)  && fragment_trans.contains(replayer.curModelStep())) {
				if (replayer.curStepType() == SYNC_MV) {
					last_frag_trans_sync = true;
					last_frag_trans_date = RPSTHelper.get_event_timestamp(replayer.curLogStep());
				}
				else {
					last_frag_trans_sync = false;
				}
			}
		}
		
		if (looking_for_exit_time) {
			cur_timestamp_exit = RPSTHelper.copy_timestamp(latest_post_timestamp);
			
			if (old_timestamp_entry == null) { // fragment only contains final place and transition before final place -> set entry time to exit time
				old_timestamp_entry = RPSTHelper.copy_timestamp(cur_timestamp_exit);
			}
			
			timestamps.add(new ImmutablePair<Date,Date>(old_timestamp_entry, cur_timestamp_exit));
			looking_for_exit_time = false;
		}
		
		if (timestamps.size() == 0) {
			return null;
		}
		else if (timestamps.size() == 1) {
			Date start = timestamps.get(0).getLeft();
			Date end = timestamps.get(0).getRight();
			return new DurationMeasurementValue(Duration.between(start.toInstant(), end.toInstant()));
		}
		else {
			ArrayList<MeasurementValue> durations = new ArrayList<>(timestamps.size());
			for (Pair<Date, Date> pair : timestamps) {
				Date start = pair.getLeft();
				Date end = pair.getRight();
				durations.add(new DurationMeasurementValue(Duration.between(start.toInstant(), end.toInstant())));
			}
			
			return (new DurationMeasurementValue()).getAverage(durations);
		}
	}

	@Override
	public ArrayList<MeasurementValue> getFragmentMeasurements(PetriNetRPSTNode fragment, PNRepResult alignments, XLog log, PetriNetRPST rpst, DecompositionParameters parameters, TransEvClassMapping mapping) {
		ArrayList<MeasurementValue> scores = new ArrayList<MeasurementValue>(log.size());
		exit_trans = new ArrayList<>(); // if the exit node is a place, any fragment transition that has an arc to the place is an exit transition. If the exit node is a transition then either the transition is 
										//the exit transition if it has no ingoing arcs from outside the fragment, or the places that activate it are the exit nodes. In the latter case, exit trans will 
										//still be set to the exit node, but a flag will be set to consider that in the evaluation.
		has_independent_exit_trans = true; // set to true by default
		if (fragment.getExit() instanceof Transition) {
			exit_trans.add((Transition) fragment.getExit());
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edges = rpst.getNet().getNet().getInEdges(fragment.getExit());
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : edges) {
				if (!fragment.getPlaces().contains(edge.getSource())) {
					has_independent_exit_trans = false;
					break;
				}
			}
		}
		else { // exit node is a place
			has_independent_exit_trans = false;
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edges = rpst.getNet().getNet().getOutEdges(fragment.getExit()); // exit transitions are the transitions that come after the exit place (however as it is not an independent exit, the exit time will be the one of the last event in the fragment)
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : edges) {
				exit_trans.add((Transition)edge.getTarget());
			}
		}
		
		prior_trans = searchTransitions(fragment, rpst.getNet().getNet(), true);
		post_trans = searchTransitions(fragment, rpst.getNet().getNet(), false);
		// iterate over all alignments; for each alignment iterate over all traces of this variant and calculate their scores
		for (SyncReplayResult alignment : alignments) {
			for (int trace_idx : alignment.getTraceIndex()) {
				// TODO super inefficient! -> enough to find the indices
				MeasurementValue score = evaluate(fragment, alignment, log.get(trace_idx), rpst, mapping);
				if (score != null) {
					scores.add(score);
				}
			}
		}
		
		//------------- DEBUGGING -------------
		
//		String node_name = "";
//		Collection<PetriNetRPSTNode> children = rpst.getChildren(fragment);
//		if (children.isEmpty()) { // node is leaf
//			node_name = fragment.getEntry() + "\n->\n" + fragment.getExit();
//		}
//		else { // node is inner-node
//			node_name = fragment.toString();
//		}
//		System.out.println(String.format("Fragment: {}", node_name));
//		System.out.println("Prior Transitions:");
//		for (Transition trans : prior_trans) {
//			System.out.println(trans);
//		}
//		System.out.println("Posterior Transitions:");
//		for (Transition trans : post_trans) {
//			System.out.println(trans);
//		}
//		System.out.println("Exit Transitions:");
//		for (Transition trans : exit_trans) {
//			System.out.println(trans);
//		}
//		System.out.println("Fragment Transitions:");
//		for (Transition trans : fragment.getTrans()) {
//			System.out.println(trans);
//		}
//		System.out.println("Independent exit: " + has_independent_exit_trans);
		
		//----------- DEBUGGING-END -----------
		
		return scores;
	} 
	
	private ArrayList<Transition> searchTransitions(PetriNetRPSTNode fragment, Petrinet net, boolean prior) {
		ArrayList<Transition> trans_list = new ArrayList<>();
		PetrinetNode start_node;
		if (prior) start_node = fragment.getEntry();
		else start_node = fragment.getExit();
		
		Stack<PetrinetNode> stack = new Stack<>();
		stack.push(start_node);
		while (!stack.isEmpty()) {
			PetrinetNode cur = stack.pop();
			if (cur instanceof Transition) {
				if (!trans_list.contains(cur)) {
					trans_list.add((Transition) cur);
				}
				else {
					continue;  // if cur is already in trans_list, then its predecessors/ successors were already traversed, hence continue to break out of loops in the net
				}
			}
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edges;
			if (prior) {
				edges = net.getInEdges(cur);
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : edges) {
					stack.push(edge.getSource());
				}
			}
			else {
				edges = net.getOutEdges(cur);
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : edges) {
					stack.push(edge.getTarget());
				}
			}
		}
		
		// add fragment trans to trans list if they are not already included
		for (Transition trans : fragment.getTrans()) {
			if (!trans_list.contains(trans)) {
				trans_list.add(trans);
			}
		}
		
		return trans_list;
	}

	
	private Date copy_timestamp(Date timestamp) {
		return new Date(timestamp.getTime());
	}
	
	private Date get_event_timestamp(XEvent event) {
		return copy_timestamp(XExtendedEvent.wrap(event).getTimestamp());
	}
	
	
	
	
	
	
	
	public String getName() {
		return NAME;
	}

}



//package org.processmining.processcomparisonpetrinetdecomposition.measurements;
//
//import java.time.Duration;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Date;
//import java.util.List;
//import java.util.Set;
//import java.util.Stack;
//
//import org.apache.commons.lang3.tuple.ImmutablePair;
//import org.apache.commons.lang3.tuple.Pair;
//import org.deckfour.xes.extension.std.XExtendedEvent;
//import org.deckfour.xes.model.XEvent;
//import org.deckfour.xes.model.XLog;
//import org.deckfour.xes.model.XTrace;
//import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPST;
//import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPSTNode;
//import org.processmining.processcomparisonpetrinetdecomposition.parameters.DecompositionParameters;
//import org.processmining.models.graphbased.directed.petrinet.Petrinet;
//import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
//import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
//import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
//import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
//import org.processmining.plugins.petrinet.replayresult.PNRepResult;
//import org.processmining.plugins.petrinet.replayresult.StepTypes;
//import org.processmining.plugins.replayer.replayresult.SyncReplayResult;
//
///**
// * A measurement that is similar to the TimeInFragment measurement, but considers a fragment to be finished once the exit place is marked, or, if the exit node is a transition that has an ingoing arc from outside the fragment, once all places that activate it are marked in the fragment.
// * @author Morit
// *
// */
//public class FragmentWaitTimeMeasurement extends Measurement{
//	private final String NAME = "Independent Sojourn Time Measurement";
//	ArrayList<Transition> prior_trans;
//	ArrayList<Transition> post_trans;
//	ArrayList<Transition> exit_trans;  // might not be necessary but needs proof
//	boolean has_independent_exit_trans; // false if the exit node is a transition and it has an ingoing arc from outside the fragment, else true
//	
//
//	/**
//	 * For the most part, this function is identical to the one in the TimeInFragmentMeasuremet. The only thing that changes is the condition of fragment exit and the assigning of the exit timestamp.
//	 * 
//	 */
//	public MeasurementValue evaluate(PetriNetRPSTNode fragment, SyncReplayResult alignment, XTrace trace, PetriNetRPST rpst, TransEvClassMapping mapping) {
//		Set<Transition> fragment_trans = fragment.getTrans();
//		List<StepTypes> step_types = alignment.getStepTypes();
//		List<Object> model_steps = alignment.getNodeInstance();
//		int trace_index = -1; // increase index before evaluating it
//		Date latest_timestamp = get_event_timestamp(trace.get(0));
//		Date cur_timestamp_entry = null;
//		Date cur_timestamp_exit = null;
//		boolean in_frag = false;
//		ArrayList<Pair<Date,Date>> timestamps = new ArrayList<>();
//		
//		if(prior_trans.size() == 0) { // if there are no prior transitions then the entry node of the fragment is the initial place, i.e. the trace begins inside the fragment 
//			in_frag = true;
//			cur_timestamp_entry = get_event_timestamp(trace.get(0));
//		}
//		for (int i = 0; i < model_steps.size(); i++) {
//			if (step_types.get(i).equals(SYNC_MV)) {
//				Transition cur_trans = (Transition)model_steps.get(i);
//				trace_index += 1;
//				
//				if (!in_frag) {
//					if (fragment_trans.contains(cur_trans)) {
//						in_frag = true;
//						if (prior_trans.contains(cur_trans)) {
//							cur_timestamp_entry = get_event_timestamp(trace.get(trace_index));
//						}
//						else {
//							cur_timestamp_entry = copy_timestamp(latest_timestamp);
//						}
//					}
//				}
//				if (in_frag) {
//					if (exit_trans.contains(cur_trans)) {
//						if (has_independent_exit_trans) {
//							cur_timestamp_exit = get_event_timestamp(trace.get(trace_index));
//							timestamps.add(new ImmutablePair<Date,Date>(cur_timestamp_entry, cur_timestamp_exit));
//							in_frag = false;
//						}
//						else {
//							cur_timestamp_exit = copy_timestamp(latest_timestamp);
//							timestamps.add(new ImmutablePair<Date,Date>(cur_timestamp_entry, cur_timestamp_exit));
//							in_frag = false;
//						}
//					}
//					if (fragment_trans.contains(cur_trans)) {
//						latest_timestamp = get_event_timestamp(trace.get(trace_index)); // update latest timestamp for the case that an invisible step exits the fragment
//					}
//				}	
//				if (!in_frag && prior_trans.contains(cur_trans)) { // only do that if not in the fragment since then we want to update latest timestamp only with fragment transitions, need to re-check if in_frag since it might have been set to false in the if clause before
//					latest_timestamp = get_event_timestamp(trace.get(trace_index)); 
//				}
//			}
//			else if (step_types.get(i).equals(MODEL_MV)) {
//				Transition cur_trans = (Transition)model_steps.get(i);
//				if (in_frag && exit_trans.contains(cur_trans)) { // set exit time to latest_timestamp as it is the timestamp of the last activity inside the fragment
//					cur_timestamp_exit = copy_timestamp(latest_timestamp);
//					timestamps.add(new ImmutablePair<Date,Date>(cur_timestamp_entry, cur_timestamp_exit));
//					in_frag = false;
//				}
//			}
//			else if (step_types.get(i).equals(LOG_MV)) {
//				trace_index += 1;
//			}
//			else if (step_types.get(i).equals(INVISIBLE_MV)) {
//				Transition cur_trans = (Transition)model_steps.get(i);
//				if (in_frag && exit_trans.contains(cur_trans)) {
//					cur_timestamp_exit = copy_timestamp(latest_timestamp);
//					timestamps.add(new ImmutablePair<Date,Date>(cur_timestamp_entry, cur_timestamp_exit));
//					in_frag = false;
//				}
//			}	
//		}
//		if (in_frag) {
//			cur_timestamp_exit = get_event_timestamp(trace.get(trace.size()-1));
//			timestamps.add(new ImmutablePair<Date,Date>(cur_timestamp_entry, cur_timestamp_exit));
//			in_frag = false;
//		}
//		
//		if (timestamps.size() == 0) {
//			return null;
//		}
//		else if (timestamps.size() == 1) {
//			Date start = timestamps.get(0).getLeft();
//			Date end = timestamps.get(0).getRight();
//			return new DurationMeasurementValue(Duration.between(start.toInstant(), end.toInstant()));
//		}
//		else {
//			ArrayList<MeasurementValue> durations = new ArrayList<>(timestamps.size());
//			for (Pair<Date, Date> pair : timestamps) {
//				Date start = pair.getLeft();
//				Date end = pair.getRight();
//				durations.add(new DurationMeasurementValue(Duration.between(start.toInstant(), end.toInstant())));
//			}
//			
//			return (new DurationMeasurementValue()).getAverage(durations);
//		}
//	}
//
//	@Override
//	public ArrayList<MeasurementValue> getFragmentMeasurements(PetriNetRPSTNode fragment, PNRepResult alignments, XLog log, PetriNetRPST rpst, DecompositionParameters parameters, TransEvClassMapping mapping) {
//		ArrayList<MeasurementValue> scores = new ArrayList<MeasurementValue>(log.size());
//		exit_trans = new ArrayList<>(); // if the exit node is a place, any transition that has an arc to the place is an exit transition. If the exit node is a transition then either the transition is 
//										//the exit transition if it has no ingoing arcs from outside the fragment, or the places that activate it are the exit nodes. In the latter case, exit trans will 
//										//still be set to the exit node, but a flag will be set to consider that in the evaluation.
//		has_independent_exit_trans = true; // set to true by default
//		if (fragment.getExit() instanceof Transition) {
//			exit_trans.add((Transition) fragment.getExit());
//			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edges = rpst.getNet().getNet().getInEdges(fragment.getExit());
//			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : edges) {
//				if (!fragment.getPlaces().contains(edge.getSource())) {
//					has_independent_exit_trans = false;
//					break;
//				}
//			}
//		}
//		else { // exit node is a place
//			has_independent_exit_trans = false;
//			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edges = rpst.getNet().getNet().getOutEdges(fragment.getExit()); // exit transitions are the transitions that come after the exit place (however as it is not an independent exit, the exit time will be the one of the last event in the fragment)
//			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : edges) {
//				exit_trans.add((Transition)edge.getTarget());
//			}
//		}
//		
//		prior_trans = searchTransitions(fragment, rpst.getNet().getNet(), true, exit_trans);
//		post_trans = searchTransitions(fragment, rpst.getNet().getNet(), false, exit_trans);
//		// iterate over all alignments; for each alignment iterate over all traces of this variant and calculate their scores
//		for (SyncReplayResult alignment : alignments) {
//			for (int trace_idx : alignment.getTraceIndex()) {
//				MeasurementValue score = evaluate(fragment, alignment, log.get(trace_idx), rpst, mapping);
//				if (score != null) {
//					scores.add(score);
//				}
//			}
//		}
//		
//		//------------- DEBUGGING -------------
//		
////		String node_name = "";
////		Collection<PetriNetRPSTNode> children = rpst.getChildren(fragment);
////		if (children.isEmpty()) { // node is leaf
////			node_name = fragment.getEntry() + "\n->\n" + fragment.getExit();
////		}
////		else { // node is inner-node
////			node_name = fragment.toString();
////		}
////		System.out.println(String.format("Fragment: {}", node_name));
////		System.out.println("Prior Transitions:");
////		for (Transition trans : prior_trans) {
////			System.out.println(trans);
////		}
////		System.out.println("Posterior Transitions:");
////		for (Transition trans : post_trans) {
////			System.out.println(trans);
////		}
////		System.out.println("Exit Transitions:");
////		for (Transition trans : exit_trans) {
////			System.out.println(trans);
////		}
////		System.out.println("Fragment Transitions:");
////		for (Transition trans : fragment.getTrans()) {
////			System.out.println(trans);
////		}
////		System.out.println("Independent exit: " + has_independent_exit_trans);
//		
//		//----------- DEBUGGING-END -----------
//		
//		return scores;
//	} 
//	
//	private ArrayList<Transition> searchTransitions(PetriNetRPSTNode fragment, Petrinet net, boolean prior, ArrayList<Transition> exit_trans) {
//		ArrayList<Transition> trans_list = new ArrayList<>();
//		PetrinetNode start_node;
//		if (prior) start_node = fragment.getEntry();
//		else start_node = fragment.getExit();
//		
//		Stack<PetrinetNode> stack = new Stack<>();
//		stack.push(start_node);
//		while (!stack.isEmpty()) {
//			PetrinetNode cur = stack.pop();
//			if (cur instanceof Transition) {
//				if (!trans_list.contains(cur)) {
//					trans_list.add((Transition) cur);
//				}
//				else {
//					continue;  // if cur is already in trans_list, then its predecessors/ successors were already traversed, hence continue to break out of loops in the net
//				}
//			}
//			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edges;
//			
//			if (prior) {
//				edges = net.getInEdges(cur);
//				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : edges) {
//					stack.push(edge.getSource());
//				}
//			}
//			else {
//				edges = net.getOutEdges(cur);
//				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : edges) {
//					stack.push(edge.getTarget());
//				}
//			}
//		}
//		
//		if (!prior && !trans_list.containsAll(exit_trans)) { // We begin traversal at the exit node, but if it is a place, the exit transitions are the transitions before it, hence we need to make sure that they are in the post set.
//			trans_list.addAll(exit_trans);
//		}
//		return trans_list;
//	}
//	
//	private Date copy_timestamp(Date timestamp) {
//		return new Date(timestamp.getTime());
//	}
//	
//	private Date get_event_timestamp(XEvent event) {
//		return copy_timestamp(XExtendedEvent.wrap(event).getTimestamp());
//	}
//	
//	
//	
//	
//	
//	
//	
//	public String getName() {
//		return NAME;
//	}
//
//}
