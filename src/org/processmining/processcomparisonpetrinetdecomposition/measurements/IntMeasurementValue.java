package org.processmining.processcomparisonpetrinetdecomposition.measurements;
import java.util.Arrays;
import java.util.List;


public class IntMeasurementValue implements MeasurementValue {
	int value;
	
	public IntMeasurementValue() {
		this.value = 0;
	}
	
	public IntMeasurementValue(int value) {
		this.value = value;
	}
	
	public void setValue(Object value) {
		if (value instanceof Integer) {
			value = this.value;
		}
		else {
			value = 0;
		}
	}

	public Integer getValue() {
		return Integer.valueOf(value);
	}
	
	public String getValueString() {
		return String.valueOf(value);
	}
	
//	public double getPValue(final List<MeasurementValue> sample1, final List<MeasurementValue> sample2) {
//		double[] sample1_values = new double[sample1.size()];
//		double[] sample2_values = new double[sample2.size()];
//		for (int i = 0; i < sample1.size(); i++) {
//			sample1_values[i] = ((Integer) sample1.get(i).getValue()).doubleValue();
//		}
//		for (int i = 0; i < sample2.size(); i++) {
//			sample2_values[i] = ((Integer) sample2.get(i).getValue()).doubleValue();
//		}
//		double p_value = TTestHelper.tTest(sample1_values, sample2_values); // two-tailed t-test for unequal variances		
//		return p_value;
//	}
//	
//	public double getDifference(List<MeasurementValue> sample1, List<MeasurementValue> sample2) {
//		if (!(sample1.get(0) instanceof IntMeasurementValue) || !(sample2.get(0) instanceof IntMeasurementValue)) {
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
//		
////		
////		if (p_value < 0.05) {
////			if (average(sample1) > average(sample2)) return 1;
////			else return -1;
////		}
////		else {
////			return 0;
////		}
//		
//	}
	
	public double average(List<MeasurementValue> list) {
		if (list == null || list.size() == 0) {
			return 0;
		}
		if (!(list.get(0) instanceof IntMeasurementValue)) {
			//throw new InvalidTypeException();
			return 0;
		}
		
		double sum = 0;
		for (MeasurementValue item : list) {
			sum += (int)item.getValue();
		}
		return sum/list.size();
	}
	
	public DoubleMeasurementValue getAverage(List<MeasurementValue> sample) {
		double average = average(sample);
		return new DoubleMeasurementValue(average);
	}
	
	public MeasurementValue getMedian(List<MeasurementValue> sample) {
		int[] values = new int[sample.size()];
		for (int i = 0; i < sample.size(); i++) {
			values[i] = (int) sample.get(i).getValue();
		}
		Arrays.sort(values);
		if (values.length % 2 == 0) {
			double median = ((double)values[values.length/2-1] + (double)values[values.length/2]) / 2;
			return new DoubleMeasurementValue(median);
		}
		else {
			int median = values[values.length/2];
			return new IntMeasurementValue(median);
		}
	}

	public int compareTo(MeasurementValue o) {
		if (!(o instanceof IntMeasurementValue)) {
			return 0;
		}
		return Integer.compare(value, (int) o.getValue());
	}

	public double[] sampleToDoubleArray(List<MeasurementValue> sample) {
		double[] sample_values = new double[sample.size()];
		for (int i = 0; i < sample.size(); i++) {
			sample_values[i] = ((Integer) sample.get(i).getValue()).doubleValue();
		}
		return sample_values;
	}
}













