package org.processmining.processcomparisonpetrinetdecomposition.measurements;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class DurationMeasurementValue implements MeasurementValue{
	Duration value;
	
	public DurationMeasurementValue() {
		setValue(Duration.ZERO);
	}
	
	public DurationMeasurementValue(Duration value) {
		setValue(value);
	}
	
	public void setValue(Object value) {
		if (value instanceof Duration) {
			this.value = (Duration) value;
		}
		else {
			this.value = Duration.ZERO;
		}
	}

	public Duration getValue() {
		return value;
	}
	
	public String getValueString() {
		String s = "";
		Duration temp_value = Duration.ZERO.withSeconds(value.getSeconds()).withNanos(value.getNano());
		int cur_res = 0; // restrict resolution to certain number of fields
		int max_res = 2;
		
		if (cur_res  < max_res && temp_value.toDays() > 0) {
			s += temp_value.toDays() + " d ";
			cur_res  += 1;
		}
		temp_value = temp_value.minusDays(temp_value.toDays());
		
		if (cur_res  < max_res && temp_value.toHours() > 0) {
			s += temp_value.toHours() + " h ";
			cur_res  += 1;
		}
		temp_value = temp_value.minusHours(temp_value.toHours());
		
		if (cur_res  < max_res && temp_value.toMinutes() > 0) {
			s += temp_value.toMinutes() + " m ";
			cur_res  += 1;
		}
		temp_value = temp_value.minusMinutes(temp_value.toMinutes());
		
		if (cur_res < max_res && temp_value.toMillis() > 0) {
			s += (temp_value.toMillis() / 1000) + " s ";
			cur_res  += 1;
		}
		
		if (cur_res == 0) { // default case if the duration is zero
			s += "0 s";
		}
							
		return s;
		// Not compatible with Java 1.8
		//return s.strip(); // remove trailing whitespace
	}
	
//	public double getPValue(List<MeasurementValue> sample1, List<MeasurementValue> sample2) {
//		double[] sample1_values = new double[sample1.size()];
//		double[] sample2_values = new double[sample2.size()];
//		for (int i = 0; i < sample1.size(); i++) {
//			sample1_values[i] = ((Duration) sample1.get(i).getValue()).toSeconds();
//		}
//		for (int i = 0; i < sample2.size(); i++) {
//			sample2_values[i] = ((Duration) sample2.get(i).getValue()).toSeconds();
//		}
//		double p_value = TTestHelper.tTest(sample1_values, sample2_values); // two-tailed t-test for unequal variances		
//		return p_value;
//	}
//
//	public double getDifference(List<MeasurementValue> sample1, List<MeasurementValue> sample2) {
//		if (!(sample1.get(0) instanceof DurationMeasurementValue) || !(sample2.get(0) instanceof DurationMeasurementValue)) {
//			//throw new InvalidTypeException();
//			return 0;
//		}
//		double p_value = getPValue(sample1, sample2);
//		Duration avg1 = getAverage(sample1).getValue();
//		Duration avg2 = getAverage(sample2).getValue();
//		
//		if (avg1.compareTo(avg2) > 0) {
//			return 1-p_value;
//		}
//		else {
//			return -1+p_value;
//		}
////		if (p_value < 0.05) {
////			if (avg1.compareTo(avg2) > 0) return 1;
////			else return -1;
////		}
////		else {
////			return 0;
////		}
//	}

	public DurationMeasurementValue getAverage(List<MeasurementValue> sample) {
		if (sample == null || sample.size() == 0) {
			return new DurationMeasurementValue();
		}
		Duration sum = Duration.ZERO;
		for (MeasurementValue item : sample) {
			sum = sum.plus((Duration) item.getValue());
		}
		return new DurationMeasurementValue(sum.dividedBy(sample.size()));
	}
	
	public MeasurementValue getMedian(List<MeasurementValue> sample) {
		Duration[] values = new Duration[sample.size()];
		for (int i = 0; i < sample.size(); i++) {
			values[i] = (Duration) sample.get(i).getValue();
		}
		Arrays.sort(values);
		if (values.length % 2 == 0) {
			Duration median = (values[values.length/2-1].plus(values[values.length/2])).dividedBy(2);
			return new DurationMeasurementValue(median);
		}
		else {
			Duration median = values[values.length/2];
			return new DurationMeasurementValue(median);
		}
	}

	public int compareTo(MeasurementValue o) {
		if (!(o instanceof DurationMeasurementValue)) {
			return 0;
		}
		return value.compareTo((Duration)o.getValue());
	}

	public double[] sampleToDoubleArray(List<MeasurementValue> sample) {
		double[] sample_values = new double[sample.size()];
		for (int i = 0; i < sample.size(); i++) {
			sample_values[i] = (((Duration) sample.get(i).getValue()).toMillis()) / 1000;
		}
		return sample_values;
	}
}
