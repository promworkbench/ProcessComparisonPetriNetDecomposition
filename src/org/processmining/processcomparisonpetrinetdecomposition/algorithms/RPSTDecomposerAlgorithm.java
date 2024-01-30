package org.processmining.processcomparisonpetrinetdecomposition.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPST;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPSTNode;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.MeasurementValue;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.NullMeasurementValue;
import org.processmining.processcomparisonpetrinetdecomposition.models.DecompositionInfoProvider;
import org.processmining.processcomparisonpetrinetdecomposition.models.RPSTDecomposition;
import org.processmining.processcomparisonpetrinetdecomposition.parameters.DecompositionParameters;
import org.processmining.processcomparisonpetrinetdecomposition.util.TTestHelper;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;


public class RPSTDecomposerAlgorithm {
	/**
	 * The method that implements your algorithm.
	 * 
	 * Note that this method only uses the boolean which is stored in the parameters.
	 * Nevertheless, it could have used the integer and/or the String as well.
	 * 
	 * @param context The context where to run in.
	 * @param input1 The first input.
	 * @param input2 The second input.
	 * @param parameters The parameters to use.
	 * @return The output.
	 */
	public RPSTDecomposition apply(PluginContext context, PetriNetRPST rpst, PNRepResult alignments1, XLog log1, PNRepResult alignments2, XLog log2, DecompositionParameters parameters, DecompositionInfoProvider info) {
		PetriNetRPSTNode root = rpst.getRoot();
		context.log("Measuring fragments...");
		context.getProgress().setMinimum(0);
		context.getProgress().setMaximum(rpst.getTree().getVertexCount());
		EvalTree(context, rpst, alignments1, log1, alignments2, log2, root, parameters, info);
		context.log("Completed fragment measurements");
		// create decomposition
		ArrayList<PetriNetRPSTNode> decomposition_nodes = new ArrayList<PetriNetRPSTNode>();
		// depth-first traverse rpst, once a node, whose parents score is null, is found, it is added to the decomposition
		// if a nodes score is null, all its children are pushed on the stack
		// once the stack is empty, a valid decomposition has been found
		Stack<PetriNetRPSTNode> stack = new Stack<PetriNetRPSTNode>();
		stack.push(root);
		context.log("Decomposition Scores:");
		while (!stack.isEmpty()) {
			PetriNetRPSTNode cur_fragment = stack.pop();
			context.log("cur node " + cur_fragment + " has score " + info.getScore(cur_fragment));
			if (info.getScore(cur_fragment) != null) {
				decomposition_nodes.add(cur_fragment);
			}
			else {
				if (rpst.getChildren(cur_fragment).isEmpty()) {
				}
				for (PetriNetRPSTNode child : rpst.getChildren(cur_fragment)) {
					stack.push(child);
				}
			}
		}
		RPSTDecomposition decomposition = new RPSTDecomposition(decomposition_nodes, rpst, info);
		
		return decomposition;
	}
	
	public Double EvalTree(PluginContext context, PetriNetRPST rpst , PNRepResult alignments1, XLog log1, PNRepResult alignments2, XLog log2, PetriNetRPSTNode fragment, DecompositionParameters parameters, DecompositionInfoProvider info) {
		// First evaluate all children, if a child has a score of null also set a score of null for the current fragment
		// keep track of the scores of the children, only evaluate current fragment if all children scores are identical and not null (or if its a leaf)		
		Double child_signum = null;
		boolean children_scores_similar = true;
		
		for (PetriNetRPSTNode child : rpst.getChildren(fragment)) {
			Double child_score = EvalTree(context, rpst, alignments1, log1, alignments2, log2, child, parameters, info);
			if (child.getArcs().size() >= parameters.getMinFragmentSize()) { // only consider child-score if the child is large enough
				if (child_signum == null) {
					if (child_score == null) {
						// noop
					}
					else if (Math.abs(child_score) >= 0.95) {
						child_signum = Math.signum(child_score);
					}
					else {
						child_signum = 0.0;
					}
				}
				if (child_score == null || (Math.abs(child_score) >= 0.95 && Math.signum(child_score) != child_signum) || (Math.abs(child_score) < 0.95 && child_signum != 0)) {
					children_scores_similar = false;
				}
			}
		}
				
		// all children are evaluated, now evaluate current fragment regardless of the children scores being similar to be able to visualize all scores
		// now calculate statistical significance for current fragment
		// first calculate the measurement values for each trace in the two logs for the current fragment
		List<MeasurementValue> sample1 = parameters.getMeasurement().getFragmentMeasurements(fragment, alignments1, log1, rpst, parameters, parameters.getMapping1());
		List<MeasurementValue> sample2 = parameters.getMeasurement().getFragmentMeasurements(fragment, alignments2, log2, rpst, parameters, parameters.getMapping2());
		context.getProgress().inc();
		
		// get difference of means for current fragment
		double difference = 0;
		double d_index = 0;
		if (sample1.size() != 0 && sample2.size() != 0) {
			double[] sample_values1 = sample1.get(0).sampleToDoubleArray(sample1);
			double[] sample_values2 = sample2.get(0).sampleToDoubleArray(sample2);
			difference = getDifference(sample1, sample2, sample_values1, sample_values2);
			d_index = TTestHelper.cohend(sample_values1, sample_values2);
		}
		
		Double score;
		if (children_scores_similar && (child_signum == null || //signum is null if there are no children, then we assign difference score as base case of recursion
				(Math.abs(difference) >= 0.95 && Math.signum(difference) == child_signum) || 
				(Math.abs(difference) < 0.95 && child_signum == 0))) { 
			score = difference;
		}
		else {
			score = null;
		}
		
		// calculate averages and medians
		MeasurementValue average1;
		MeasurementValue median1;
		if (sample1.size() == 0) {
			average1 = new NullMeasurementValue();
			median1 = new NullMeasurementValue();
		}
		else {
			average1 = sample1.get(0).getAverage(sample1);
			median1 = sample1.get(0).getMedian(sample1);
		}
		MeasurementValue average2;
		MeasurementValue median2;
		if (sample2.size() == 0) {
			average2 = new NullMeasurementValue();
			median2 = new NullMeasurementValue();
		}
		else {
			average2 = sample2.get(0).getAverage(sample2);
			median2 = sample2.get(0).getMedian(sample2);
		}
		
		// put info data
		info.addScore(fragment, score);
		info.addDifference(fragment, difference);
		info.addDIndex(fragment, d_index);
		info.addAverage(fragment, 0, average1);
		info.addAverage(fragment, 1, average2);
		info.addMedian(fragment, 0, median1);
		info.addMedian(fragment, 1, median2);
		double p_value = 1 - Math.abs(difference);
		info.addPValue(fragment, p_value);

		return score;
	}

	private double getDifference(List<MeasurementValue> sample1, List<MeasurementValue> sample2, double[] sample_values1, double[] sample_values2) {
		double p_value = TTestHelper.tTest(sample_values1, sample_values2);
		MeasurementValue avg1 = sample1.get(0).getAverage(sample1);
		MeasurementValue avg2 = sample2.get(0).getAverage(sample2);
		
		if (avg1.compareTo(avg2) > 0) {
			return 1-p_value;
		}
		else {
			return -1+p_value;
		}
	}
	
	
	
	
	
	
	
//	public void EvalWholeTree(PluginContext context, PetriNetRPST rpst , PNRepResult alignments1, XLog log1, PNRepResult alignments2, XLog log2, PetriNetRPSTNode fragment, DecompositionParameters parameters) {
//		Stack<PetriNetRPSTNode> stack = new Stack<PetriNetRPSTNode>();
//		stack.push(rpst.getRoot());
//		while (!stack.isEmpty()) {
//			PetriNetRPSTNode cur_fragment = stack.pop();
//			
//			// evaluate fragment by checking for a statistically significant difference in measurement values between the two logs
//			ArrayList<MeasurementValue> sample1 = this.getFragmentMeasurements(fragment, alignments1, log1, parameters.getMeasurement());
//			ArrayList<MeasurementValue> sample2 = this.getFragmentMeasurements(fragment, alignments2, log2, parameters.getMeasurement());
//			MeasurementValue average1 = sample1.get(0).getAverage(sample1);
//			MeasurementValue average2 = sample2.get(0).getAverage(sample2);
//			ImmutablePair<MeasurementValue, MeasurementValue> average_pair = new ImmutablePair<MeasurementValue, MeasurementValue>(average1, average2);
//			measurement_averages.put(fragment, average_pair);
//			// get difference of means for current fragment
//			int difference = sample1.get(0).getDifference(sample1, sample2);
//			differences.put(fragment, difference);
//		}
//	}
	
	
	
	
	
	
	
//	public Integer EvalTree(PluginContext context, PetriNetRPST rpst , PNRepResult alignments1, XLog log1, PNRepResult alignments2, XLog log2, PetriNetRPSTNode fragment, DecompositionParameters parameters) {
//		// First evaluate all children, if a child has a score of null also set a score of null for the current fragment
//		// keep track of the scores of the children, only evaluate current fragment if all children scores are identical and not null (or if its a leaf)
//		Integer children_scores = null; 
//		boolean children_scores_similar = true;
//		for (PetriNetRPSTNode child : rpst.getChildren(fragment)) {
//			Integer child_score = EvalTree(context, rpst, alignments1, log1, alignments2, log2, child, parameters);
//			fragment_scores.put(child, child_score);
//			if (child.getArcs().size() >= parameters.getMinFragmentSize()) { // only consider child-score if the child is large enough
//				if (children_scores == null) {
//					children_scores = child_score;
//				}
//				if (child_score == null || !child_score.equals(children_scores)) {
//					children_scores_similar = false;
//				}
//			}
//		}
//		if (!children_scores_similar) {
//			return null;
//		}
//		// all children are evaluated and have scores different from null
//		// now calculate statistical significance for current fragment
//		// first calculate the measurement values for each trace in the two logs for the current fragment
//		ArrayList<MeasurementValue> sample1 = this.getFragmentMeasurements(fragment, alignments1, log1, measurement);
//		ArrayList<MeasurementValue> sample2 = this.getFragmentMeasurements(fragment, alignments2, log2, measurement);
//		MeasurementValue average1 = sample1.get(0).getAverage(sample1);
//		MeasurementValue average2 = sample2.get(0).getAverage(sample2);
//		ImmutablePair<MeasurementValue, MeasurementValue> average_pair = new ImmutablePair<MeasurementValue, MeasurementValue>(average1, average2);
//		measurement_averages.put(fragment, average_pair);
//		// get difference of means for current fragment
//		int difference = sample1.get(0).getDifference(sample1, sample2);
//		differences.put(fragment, difference);
//		if (children_scores == null || difference == children_scores) { //children_scores is null if there are no children, then we assign difference score as base case of recursion
//			return difference;
//		}
//		else {
//			return null;
//		}
//	}	
}
