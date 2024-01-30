package org.processmining.processcomparisonpetrinetdecomposition.parameters;

import org.processmining.basicutils.parameters.impl.PluginParametersImpl;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.Measurement;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.ReachedMeasurement;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;

public class DecompositionParameters extends PluginParametersImpl{
	private int minFragmentSize;
	private Measurement measurement;
	private boolean mergeBottomUp;
	private boolean ignoreLongTermDependencies;
	private TransEvClassMapping mapping1;
	private TransEvClassMapping mapping2;
//	private DecompositionInfoProvider info;
	
	public DecompositionParameters(TransEvClassMapping mapping1, TransEvClassMapping mapping2) {
		super();
		minFragmentSize = 1;
		measurement = new ReachedMeasurement();
		mergeBottomUp = true;
		setMapping1(mapping1);
		setMapping2(mapping2);
//		setInfo(info);
	}

	public DecompositionParameters(DecompositionParameters parameters) {
		super(parameters);
		setMeasurement(parameters.getMeasurement());
		setMinFragmentSize(parameters.getMinFragmentSize());
		setMergeBottomUp(parameters.isMergeBottomUp());
		setMapping1(parameters.getMapping1());
		setMapping2(parameters.getMapping2());
//		setInfo(parameters.getInfo());
		setIgnoreLongTermDependencies(parameters.isIgnoreLongTermDependencies());
	}
	
	public boolean equals(Object object) {
		if (object instanceof DecompositionParameters) {
			DecompositionParameters parameters = (DecompositionParameters) object;
			return super.equals(parameters) &&
					getMinFragmentSize() == parameters.getMinFragmentSize() &&
					getMeasurement().getName().equals(parameters.getMeasurement().getName()) &&
					isMergeBottomUp() == parameters.isMergeBottomUp() &&
					getMapping1().equals(parameters.getMapping1()) &&
					getMapping2().equals(parameters.getMapping2()) &&
//					getInfo().equals(parameters.getInfo())&&
					isIgnoreLongTermDependencies() == parameters.isIgnoreLongTermDependencies();
		}
		return false;
	}
	
	public int getMinFragmentSize() {
		return minFragmentSize;
	}

	public void setMinFragmentSize(int minFragmentSize) {
		this.minFragmentSize = minFragmentSize;
	}

	public Measurement getMeasurement() {
		return measurement;
	}

	public void setMeasurement(Measurement measurement) {
		this.measurement = measurement;
	}

	public boolean isMergeBottomUp() {
		return mergeBottomUp;
	}

	public void setMergeBottomUp(boolean mergeBottomUp) {
		this.mergeBottomUp = mergeBottomUp;
	}

	public TransEvClassMapping getMapping1() {
		return mapping1;
	}
	
	public void setMapping1(TransEvClassMapping mapping) {
		this.mapping1 = mapping;
	}
	
	public TransEvClassMapping getMapping2() {
		return mapping2;
	}
	
	public void setMapping2(TransEvClassMapping mapping) {
		this.mapping2 = mapping;
	}
	
//	public DecompositionInfoProvider getInfo() {
//		return info;
//	}
//	
//	public void setInfo(DecompositionInfoProvider info) {
//		this.info = info;
//	}
		
	public boolean isIgnoreLongTermDependencies() {
		return ignoreLongTermDependencies;
	}

	public void setIgnoreLongTermDependencies(boolean ignoreLongTermDependencies) {
		this.ignoreLongTermDependencies = ignoreLongTermDependencies;
	}

	public String toString() {
		return "(" + getMeasurement().getName() + "," + getMinFragmentSize() + "," + isMergeBottomUp() + ")";
	}
}
