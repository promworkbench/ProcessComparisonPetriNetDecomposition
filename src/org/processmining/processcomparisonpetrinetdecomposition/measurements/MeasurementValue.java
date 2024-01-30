package org.processmining.processcomparisonpetrinetdecomposition.measurements;

import java.util.List;

public interface MeasurementValue extends Comparable<MeasurementValue>{
	public void setValue(Object value);
	public Object getValue();
	public String getValueString();
	public double[] sampleToDoubleArray(List<MeasurementValue> sample);
//	public double getPValue(List<MeasurementValue> sample1, List<MeasurementValue> sample2);
//	public double getDifference(List<MeasurementValue> sample1, List<MeasurementValue> sample2);
//	public double getDIndex(List<MeasurementValue> sample1, List<MeasurementValue> sample2);
	public MeasurementValue getAverage(List<MeasurementValue> sample);
	public MeasurementValue getMedian(List<MeasurementValue> sample);
}
