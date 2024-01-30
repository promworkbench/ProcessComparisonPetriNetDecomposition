package org.processmining.processcomparisonpetrinetdecomposition.measurements;

import java.util.Arrays;
import java.util.List;

/**
 * A measurement value that holds the value null but behaves like an ordinary measurement value
 * @author Moritz Gose
 *
 */
public class NullMeasurementValue implements MeasurementValue{

	public NullMeasurementValue() {
		
	}
	
	public void setValue(Object value) {
		// TODO Auto-generated method stub
		
	}

	public Object getValue() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getValueString() {
		// TODO Auto-generated method stub
		return "null";
	}

//	public double getPValue(List<MeasurementValue> sample1, List<MeasurementValue> sample2) {
//		// TODO Auto-generated method stub
//		return 1;
//	}
//
//	public double getDifference(List<MeasurementValue> sample1, List<MeasurementValue> sample2) {
//		// TODO Auto-generated method stub
//		return 0;
//	}

	public MeasurementValue getAverage(List<MeasurementValue> sample) {
		// TODO Auto-generated method stub
		return new NullMeasurementValue();
	}
	
	public MeasurementValue getMedian(List<MeasurementValue> sample) {
		// TODO Auto-generated method stub
		return new NullMeasurementValue();
	}

	public int compareTo(MeasurementValue o) {
		return 0;
	}

	public double[] sampleToDoubleArray(List<MeasurementValue> sample) {
		double[] zero_array = new double[sample.size()];
		Arrays.fill(zero_array, 0);
		return zero_array;
	}
}
