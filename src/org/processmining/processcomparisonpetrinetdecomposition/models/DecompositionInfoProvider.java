package org.processmining.processcomparisonpetrinetdecomposition.models;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPST;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPSTNode;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.MeasurementValue;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.NullMeasurementValue;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;


public class DecompositionInfoProvider {
	// reference from fragment to index
	private final Map<PetriNetRPSTNode, Integer> fragToInt;
	
	// fragment info
	private Double[] scores;
	private double[] differences;
	private MeasurementValue[][] averages;
	private MeasurementValue[][] medians;
	private double[] p_values;
	private double[] d_indices;
	private MeasurementValue[][] fitness;
	private MeasurementValue[][] reached_rate;
	
	// global info
	private String[] log_names;
	private double[] global_fitness;
	
	// base data for further computation
	private PetriNetRPST rpst;
	private PNRepResult alignments1;
	private PNRepResult alignments2;
	private XLog log1;
	private XLog log2;
	
	public DecompositionInfoProvider(PetriNetRPST rpst, PNRepResult alignments1, XLog log1, PNRepResult alignments2, XLog log2) {
		this.rpst = rpst;
		this.alignments1 = alignments1;
		this.alignments2 = alignments2;
		this.log1 = log1;
		this.log2 = log2;		
		
		Collection<PetriNetRPSTNode> fragments = rpst.getNodes();
		// define fragment / index mapping
		fragToInt = new HashMap<>();
		int index = 0;
		for (PetriNetRPSTNode fragment : fragments) {
			fragToInt.put(fragment, index);
			index += 1;
		}
		// initialize arrays
		scores = new Double[fragments.size()];
		differences = new double[fragments.size()];
		averages = new MeasurementValue[2][fragments.size()];
		medians = new MeasurementValue[2][fragments.size()];
		p_values = new double[fragments.size()];
		d_indices= new double[fragments.size()];
		fitness = new MeasurementValue[2][fragments.size()];
		reached_rate = new MeasurementValue[2][fragments.size()];
		
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < fragments.size(); j++) {
				averages[i][j] = new NullMeasurementValue();
				medians[i][j] = new NullMeasurementValue();
				fitness[i][j] = new NullMeasurementValue();
			}
		}
		
		log_names = new String[2];
		global_fitness = new double[2];
	}
	
	public Object[] getFragmentInfo() {
		Object[] values = new Object[4];
		
		return values;
	}
	
	public String[] getFragmentInfoString() {
		
		return null;
	}
	
	public void addScore(PetriNetRPSTNode fragment, Double score) {
		scores[fragToInt.get(fragment)] = score;
	}
	
	public Double getScore(PetriNetRPSTNode fragment) {
		return scores[fragToInt.get(fragment)];
	}
	
	public void addDifference(PetriNetRPSTNode fragment, double difference) {
		differences[fragToInt.get(fragment)] = difference;
	}
	
	public double getDifference(PetriNetRPSTNode fragment) {
		return differences[fragToInt.get(fragment)];
	}
	
	public void addAverage(PetriNetRPSTNode fragment, int log, MeasurementValue average) {
		averages[log][fragToInt.get(fragment)] = average;
	}
	
	public MeasurementValue getAverage(PetriNetRPSTNode fragment, int log) {
		return averages[log][fragToInt.get(fragment)];
	}
	
	public void addMedian(PetriNetRPSTNode fragment, int log, MeasurementValue median) {
		medians[log][fragToInt.get(fragment)] = median;
	}
	
	public MeasurementValue getMedian(PetriNetRPSTNode fragment, int log) {
		return medians[log][fragToInt.get(fragment)];
	}
	
	public void addPValue(PetriNetRPSTNode fragment, double p_value) {
		p_values[fragToInt.get(fragment)] = p_value;
	}
	
	public double getPValue(PetriNetRPSTNode fragment) {
		return p_values[fragToInt.get(fragment)];
	}
	
	public void addDIndex(PetriNetRPSTNode fragment, double d_index) {
		d_indices[fragToInt.get(fragment)] = d_index;
	}
	
	public double getDIndex(PetriNetRPSTNode fragment) {
		return d_indices[fragToInt.get(fragment)];
	}
	
	public void addFitness(PetriNetRPSTNode fragment, int log, MeasurementValue fitness) {
		this.fitness[log][fragToInt.get(fragment)] = fitness;
	}
	
	public MeasurementValue getFitness(PetriNetRPSTNode fragment, int log) {
		return fitness[log][fragToInt.get(fragment)];
	}
	
	public void addReachedRate(PetriNetRPSTNode fragment, int log, MeasurementValue reached_rate) {
		this.reached_rate[log][fragToInt.get(fragment)] = reached_rate;
	}
	
	public MeasurementValue getReachedRate(PetriNetRPSTNode fragment, int log) {
		return reached_rate[log][fragToInt.get(fragment)];
	}
	
	public void setLogName(int log, String name) {
		log_names[log] = name;
	}
	
	public String getLogName(int log) {
		return log_names[log];
	}
	
	public void setGlobalFitness(double fitness0, double fitness1) {
		global_fitness[0] = fitness0;
		global_fitness[1] = fitness1;
	}
	
	public double getGlobalFitness(int log) {
		return global_fitness[log];
	}
	
	public void setRPST(PetriNetRPST rpst) {
		this.rpst = rpst;
	}
	
	public PetriNetRPST getRPST() {
		return rpst;
	}

	public PNRepResult getAlignments1() {
		return alignments1;
	}

	public void setAlignments1(PNRepResult alignments1) {
		this.alignments1 = alignments1;
	}

	public PNRepResult getAlignments2() {
		return alignments2;
	}

	public void setAlignments2(PNRepResult alignments2) {
		this.alignments2 = alignments2;
	}

	public XLog getLog1() {
		return log1;
	}

	public void setLog1(XLog log1) {
		this.log1 = log1;
	}

	public XLog getLog2() {
		return log2;
	}

	public void setLog2(XLog log2) {
		this.log2 = log2;
	}
}
