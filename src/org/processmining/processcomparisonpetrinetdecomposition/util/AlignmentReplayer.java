package org.processmining.processcomparisonpetrinetdecomposition.util;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPSTNode;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.Measurement;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public class AlignmentReplayer {
	private final Petrinet net;
	private final XTrace trace;
	private final SyncReplayResult alignment;
	private final PetriNetRPSTNode fragment;
	
	private int trace_idx;
	private int alignment_idx;
	private int tokens;  // tokens in fragment after executing the transitions until and including the current alignment_idx
	private boolean fragment_entered;  // true if the fragment was entered by the transition at the current alignment_idx
	private boolean fragment_exited;  // true if the fragment was exited by the transition at the current alignment_idx
	private boolean currentExecActivatedFragTrans;  // true if tokens > 0 and there occured at least one synchronous or model move since the fragment was entered
	private boolean lastExitedExecActivatedFragTrans;  // true if last exited execution fired intra-frament transition
	private boolean in_sync_execution;  // true if tokens > 0 and there occured at least one synchronous step since the fragment was entered
	private boolean exited_sync_execution; // true if current move is left an execution in which in_sync_execution was true
	private boolean contains_final_place;
	
	public AlignmentReplayer(Petrinet net, XTrace trace, SyncReplayResult alignment, PetriNetRPSTNode fragment) {
		this.net = net;
		this.trace = trace;
		this.alignment = alignment;
		this.fragment = fragment;
		trace_idx = -1;
		alignment_idx = -1;
		fragment_exited = false;
		in_sync_execution = false;
		exited_sync_execution = false;
		currentExecActivatedFragTrans = false;
		if (RPSTHelper.isInitialPlaceInFragment(fragment, net)) {
			tokens = 1;
			fragment_entered = true;
		}
		else {
			tokens = 0;
			fragment_entered = false;
		}
		contains_final_place = RPSTHelper.isFinalPlaceInFragment(fragment, net);
	}
	
	public boolean next() {
		if(!hasNext()) {
			return false;
		}
		alignment_idx += 1;
		boolean was_in_fragment = isInFragment();		
		boolean sync_move = false;
		// This step is associated to a transition that is contained in the fragment
		// (false for log moves)
		boolean curTransitionInFragment = false;
		
		// update tokens
		int tokens_after_consumption=tokens; 
		if (curStepType().equals(Measurement.SYNC_MV)) {
			trace_idx += 1;
			tokens_after_consumption = tokens - RPSTHelper.fragTokenConsumed(fragment, net, getModelStep(alignment_idx));
			tokens = tokens_after_consumption + RPSTHelper.fragTokenAdded(fragment, net, getModelStep(alignment_idx));
			sync_move = true;
			curTransitionInFragment = fragment.getTrans().contains(curModelStep());
		}
		else if (curStepType().equals(Measurement.MODEL_MV)) {
			tokens_after_consumption = tokens - RPSTHelper.fragTokenConsumed(fragment, net, getModelStep(alignment_idx));
			tokens = tokens_after_consumption + RPSTHelper.fragTokenAdded(fragment, net, getModelStep(alignment_idx));
			curTransitionInFragment = fragment.getTrans().contains(curModelStep());
		}
		else if (curStepType().equals(Measurement.LOG_MV)) {
			trace_idx += 1;
		}
		else if (curStepType().equals(Measurement.INVISIBLE_MV)) {
			tokens_after_consumption = tokens - RPSTHelper.fragTokenConsumed(fragment, net, getModelStep(alignment_idx));
			tokens = tokens_after_consumption + RPSTHelper.fragTokenAdded(fragment, net, getModelStep(alignment_idx));
			curTransitionInFragment = fragment.getTrans().contains(curModelStep());
		}
		
		
		////////////////////////////////////////
		// Update status variables
		////////////////////////////////////////
		// Enter
		fragment_entered = isInFragment() && (!was_in_fragment || (tokens_after_consumption == 0 && !fragment.getTrans().contains(curModelStep()))); 
		// Exit
		fragment_exited = (was_in_fragment && (!isInFragment() || (tokens_after_consumption == 0 && !fragment.getTrans().contains(curModelStep())))) || (!hasNext()); // also consider fragment exited once the replay finishes  
		
		////////////////////////////////////////
		// Update if executions were properly entered (i.e., fired a transition)
		////////////////////////////////////////
		// If entered and exited, we need to distinguish
		// 1. entered -> and immediate exit?
		// 2. exit -> and immediate novel enter?
		if (fragment_entered && fragment_exited) {
			// Case 1 (only one transition was executed)
			if (!was_in_fragment) {
				lastExitedExecActivatedFragTrans = curTransitionInFragment;
			}
			// Case 2
			else {
				// Can only occur in case of outside short loops => current Transition cannot be in the fragment 
				lastExitedExecActivatedFragTrans = currentExecActivatedFragTrans;
			}
		}
		else {
			// Reset status on entry
			if (fragment_entered) {
				currentExecActivatedFragTrans = false;
			}

			// Save status of last execution
			if (fragment_exited) {
				lastExitedExecActivatedFragTrans = curTransitionInFragment || currentExecActivatedFragTrans;
			}
			
			// Update status if inside fragment and not yet true
			if (isInFragment()) {
				currentExecActivatedFragTrans |= curTransitionInFragment;
			}
		}
				
		if (fragment_exited) { // update exited_sync_execution and in_sync_execution
			exited_sync_execution = in_sync_execution || (sync_move && fragment.getTrans().contains(curModelStep()));
			in_sync_execution = false;
		}
		if (sync_move && isInFragment() && fragment.getTrans().contains(curModelStep())) {
			in_sync_execution = true;
		}
		
		return true;
	}
	
	public boolean isInSyncExecution() {
		return in_sync_execution;
	}
	
	public boolean exitedSyncExecution () {
		return exited_sync_execution;
	}
	
	public boolean hasEntered() {
		return fragment_entered;
	}
	
	public boolean hasExited() {
		return fragment_exited;
	}
	
	public int curTokens() {
		return tokens;
	}
	
	public boolean isInFragment() {
		return tokens  > 0;
	}
	
	public StepTypes curStepType() {
		return getStepType(alignment_idx);
	}
	
	public Transition curModelStep() {
		return (Transition)alignment.getNodeInstance().get(alignment_idx);
	}
	
	public XEvent curLogStep() {
		return trace.get(trace_idx);
	}
	
	public int curLogIndex() {
		return trace_idx;
	}
	
	public boolean didLastExitedExecActivateFragTrans() {
		return lastExitedExecActivatedFragTrans;
	}
	
	public boolean hasNext() {
		return alignment_idx < alignment.getStepTypes().size() - 1;
	}
	
	private StepTypes getStepType(int idx) {
		return alignment.getStepTypes().get(idx);
	}

	private Transition getModelStep(int idx) {
		return (Transition)alignment.getNodeInstance().get(idx);
	}
	
	private XEvent getLogStep(int idx) {
		return trace.get(idx);
	}
}
