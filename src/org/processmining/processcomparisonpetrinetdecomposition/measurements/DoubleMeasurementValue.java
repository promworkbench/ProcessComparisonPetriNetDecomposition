package org.processmining.processcomparisonpetrinetdecomposition.measurements;

import java.util.Arrays;
import java.util.List;

public class DoubleMeasurementValue implements MeasurementValue {
	double value; 
	
	public DoubleMeasurementValue() {
		this.value = 0;
	}
	
	public DoubleMeasurementValue(double value) {
		this.value = value;
	}
	
	public void setValue(Object value) {
		if (value instanceof Double) {
			value = this.value;
		}
		else {
			value = 0;
		}

	}

	public Double getValue() {
		return Double.valueOf(value);
	}
	
	public String getValueString() {
		return String.format("%.3f",value);
	}

	public double[] sampleToDoubleArray(List<MeasurementValue> sample) {
		double[] sample_values = new double[sample.size()];
		for (int i = 0; i < sample.size(); i++) {
			sample_values[i] = ((Double) sample.get(i).getValue());
		}
		return sample_values;
	}
	
	public int compareTo(MeasurementValue o) {
		if (!(o instanceof DoubleMeasurementValue)) {
			return 0;
		}
		return Double.compare(value, (double) o.getValue());
	}
	
//	public double getPValue(List<MeasurementValue> sample1, List<MeasurementValue> sample2) {
//		Pair<double[], double[]> sample_values = samplesToDoubleArray(sample1, sample2);
//		double p_value = TTestHelper.tTest(sample_values.getLeft(), sample_values.getRight()); // two-tailed t-test for unequal variances
//		return p_value;
//	}
//
//	public double getDifference(List<MeasurementValue> sample1, List<MeasurementValue> sample2) {
//		if (!(sample1.get(0) instanceof DoubleMeasurementValue) || !(sample2.get(0) instanceof DoubleMeasurementValue)) {
//			//throw new InvalidTypeException();
//			return 0;
//		}
//		double p_value = getPValue(sample1, sample2);
//		
//		DoubleMeasurementValue avg1 = getAverage(sample1);
//		DoubleMeasurementValue avg2 = getAverage(sample2);
//		if (avg1.getValue() > avg2.getValue()) {
//			return 1-p_value;
//		}
//		else {
//			return -1+p_value;
//		}
////		if (p_value < 0.05) {
////			if (average(sample1) > average(sample2)) return 1;
////			else return -1;
////		}
////		else {
////			return 0;
////		}
//	}
//	
//	public double getDIndex(List<MeasurementValue> sample1, List<MeasurementValue> sample2) {
//		Pair<double[], double[]> sample_values = samplesToDoubleArray(sample1, sample2);
//		return TTestHelper.cohend(sample_values.getLeft(), sample_values.getRight());
//	}
	
	public double average(List<MeasurementValue> list) {
		if (list == null || list.size() == 0) {
			return 0;
		}
		if (!(list.get(0) instanceof DoubleMeasurementValue)) {
			//throw new InvalidTypeException();
			return 0;
		}
		double sum = 0;
		for (MeasurementValue item : list) {
			sum += (double)item.getValue();
		}
		return sum/list.size();
	}

	public DoubleMeasurementValue getAverage(List<MeasurementValue> sample) {
		double average = average(sample);
		return new DoubleMeasurementValue(average);
	}
	
	public MeasurementValue getMedian(List<MeasurementValue> sample) {
		double[] values = new double[sample.size()];
		for (int i = 0; i < sample.size(); i++) {
			values[i] = (double) sample.get(i).getValue();
		}
		Arrays.sort(values);
		if (values.length % 2 == 0) {
			double median = (values[values.length/2-1] + values[values.length/2]) / 2;
			return new DoubleMeasurementValue(median);
		}
		else {
			double median = values[values.length/2];
			return new DoubleMeasurementValue(median);
		}
	}
}
