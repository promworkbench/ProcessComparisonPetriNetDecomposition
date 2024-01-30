package org.processmining.processcomparisonpetrinetdecomposition.help;

public class YourHelp {

	public final static String TEXT = ""
			+ "Compare two event logs based on a shared Petri net.\n"
			+ "First, we decompose the Petri net into a hierarchy of subprocesses. To this end, the RPST is used, but there is also "
			+ "an additional option where we first remove places that are not required for the workflow net property (connectedness) "
			+ "and that hold tokens for a long time. Next, for each trace, we extract a measurement using a user-selected measurement function. "
			+ "Finally, we detect differences using hypothesis tests for each subprocess.";
}
