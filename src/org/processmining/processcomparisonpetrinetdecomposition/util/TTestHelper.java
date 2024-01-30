package org.processmining.processcomparisonpetrinetdecomposition.util;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.inference.TTest;

public class TTestHelper {
	/**
	 * Manually calculates the p-value of Welch's t-test for the two given sample arrays.
	 * 
	 * @param sample1 double array of sample values. 
	 * @param sample2 double array of sample values.
	 * @return p-value 
	 */
	public static double tTest(final double[] sample1, final double[] sample2) {
		TTest test = new TTest();
		double p_value;
		double variance1 = StatUtils.variance(sample1);
		double variance2 = StatUtils.variance(sample2);
		if (variance1 == 0 && variance2 != 0) {
			double mu = StatUtils.mean(sample1);
			p_value = test.tTest(mu, sample2);
		}
		else if (variance1 != 0 && variance2 == 0) {
			double mu = StatUtils.mean(sample2);
			p_value = test.tTest(mu, sample1);
		}
		else if (variance1 == 0 && variance2 == 0) {
			if (StatUtils.mean(sample1) != StatUtils.mean(sample2)) {
				p_value = 0.049;
			}
			else {
				p_value = 1;
			}
		}
		else {
			p_value = test.tTest(sample1, sample2);
		}
		return p_value;
	}
	
	/**
	 * Calculates the effect size between the two sample arrays according to Cohen'd for a two-tailed t-test with unequal variance
	 * @param sample1 Non-empty array of values from population 1.
	 * @param sample2 Non-empty array of values from population 2.
	 * @return Effect size according to Cohen's d.
	 */
	public static double cohend(final double[] sample1, final double[] sample2)	{
		double mean1 = StatUtils.mean(sample1);
		double mean2 = StatUtils.mean(sample2);
		double variance1 = StatUtils.variance(sample1);
		double variance2 = StatUtils.variance(sample2);
		
		return cohend(mean1, mean2, variance1, variance2, sample1.length, sample2.length);
	}
	
	/**
	 * Calculates the effect size according Cohen's d for a two-tailed t-test with unequal variance.
	 * @param m1 Mean of sample 1.
	 * @param m2 Mean of sample 2.
	 * @param v1 Variance of sample 1.
	 * @param v2 Variance of sample 2.
	 * @param n1 Size of sample 1.
	 * @param n2 Size of sample 2.
	 * @return Effect size according to Cohen's d.
	 */
	public static double cohend(double m1, double m2, double v1, double v2, int n1, int n2) {
		double std_dev = pooledStandardDeviation(v1, v2, n1, n2);
		if (std_dev == 0) return 0;
		return Math.abs(m1-m2) /std_dev;
	}
	
	private static double pooledStandardDeviation(double v1, double v2, int n1, int n2) {
		if (n1 + n2 - 2 == 0) return 0;
		return Math.sqrt(((n1 - 1) * v1 + (n2 - 1) * v2) / (n1 + n2 - 2));
	}
}
