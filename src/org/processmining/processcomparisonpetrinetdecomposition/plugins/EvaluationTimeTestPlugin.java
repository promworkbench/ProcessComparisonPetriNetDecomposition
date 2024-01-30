package org.processmining.processcomparisonpetrinetdecomposition.plugins;

import java.time.Duration;
import java.time.Instant;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPST;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPSTNode;
import org.processmining.acceptingpetrinetdecomposer.parameters.sese.GenerateRPSTFromPetriNetParameters;
import org.processmining.processcomparisonpetrinetdecomposition.algorithms.RPSTDecomposerAlgorithm;
import org.processmining.processcomparisonpetrinetdecomposition.connections.RPSTPetriNetConnection;
import org.processmining.processcomparisonpetrinetdecomposition.help.YourHelp;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.ActivitiyOutsideFragmentMeasurement;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.DurationMeasurementValue;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.ElapsedTimeMeasurement;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.ExecutionFitnessMeasurement;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.FitnessMeasurement;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.FragmentQueueMeasurement;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.FragmentWaitTimeMeasurement;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.IndependentReachedMeasurement;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.LoopIterationMeasurement;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.Measurement;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.ReachedMeasurement;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.TimeInFragmentMeasurement;
import org.processmining.processcomparisonpetrinetdecomposition.models.DecompositionInfoProvider;
import org.processmining.processcomparisonpetrinetdecomposition.models.RPSTDecomposition;
import org.processmining.processcomparisonpetrinetdecomposition.parameters.DecompositionParameters;
import org.processmining.processcomparisonpetrinetdecomposition.util.RPSTHelper;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.connections.petrinets.EvClassLogPetrinetConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.PNLogReplayer;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;
import nl.tue.astar.AStarException;

@Plugin(name = "Decompose RPST Time Test", parameterLabels = { "RPST", "Alignments1", "Log1", "Alignments2", "Log2", "Petri net", "Parameters" }, 
	    returnLabels = { "RPST Decomposition" }, returnTypes = { JComponent.class }, help = YourHelp.TEXT)
public class EvaluationTimeTestPlugin extends RPSTDecomposerAlgorithm {

	/**
	 * The default plugin variant that runs in any context and requires parameters.
	 * 
	 * @param context Plugin context to run in.
	 * @param rpst RPST of the Petri net N over which the logs should be compared.
	 * @param alignments1 Alignments of log1 and N.
	 * @param log1 First process for comparison.
	 * @param alignments2 Alignments of log2 and N.
	 * @param log2 Second process for comparison.
	 * @param parameters Decomposition parameters, need at least the transition event mappings for both logs and the model.
	 * @return RPSTDecomposition enriched with measurement information according to the measurement specified in the parameters.
	 */
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Moritz Gose", email = "moritz.gose@rwth-aachen.de")
	@PluginVariant(variantLabel = "Decompose RPST", requiredParameterLabels = { 0,1,2, 3, 4, 6 })
	public JComponent runDefault(PluginContext context, PetriNetRPST rpst, PNRepResult alignments1, XLog log1, PNRepResult alignments2, XLog log2, DecompositionParameters parameters) {
		return runConnections(context, rpst, alignments1, log1, alignments2, log2, parameters);
	}	

	/**
	 * The standarad plugin variant to be used by the user in an UIPluginContext.
	 * 
	 * @param context The context to run in.
	 * @param net Petri net over which the processes should be compared.
	 * @param log1 First process for comparison.
	 * @param log2 Second process for comparison.
	 * @return RPSTDecomposition enriched with measurement information according to the measurement specified in the parameters.
	 */
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Moritz Gose", email = "moritz.gose@rwth-aachen.de")
	@PluginVariant(variantLabel = "Create and Decompose RPST", requiredParameterLabels = { 2, 4, 5 })
	public JComponent runUI(UIPluginContext context, Petrinet net, XLog log1, XLog log2) {
		EvClassLogPetrinetConnection conn1;
		EvClassLogPetrinetConnection conn2;
		try {
			// connection is found, no need for mapping step
			// connection is not found, another plugin to create such connection is automatically executed!!!
			conn1 = context.getConnectionManager().getFirstConnection(EvClassLogPetrinetConnection.class, context, net, log1);
			conn2 = context.getConnectionManager().getFirstConnection(EvClassLogPetrinetConnection.class, context, net, log2);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(new JPanel(), "No mapping can be constructed between the net and the log");
			return null;
		}
		TransEvClassMapping mapping1 = conn1.getObjectWithRole(EvClassLogPetrinetConnection.TRANS2EVCLASSMAPPING);
		TransEvClassMapping mapping2 = conn2.getObjectWithRole(EvClassLogPetrinetConnection.TRANS2EVCLASSMAPPING);
		// Get the default parameters.
		DecompositionParameters parameters = new DecompositionParameters(mapping1, mapping2);
		
		// create Alignments
		PNRepResult alignments1 = null;
		PNRepResult alignments2 = null;
		try {
			alignments1 = (new PNLogReplayer()).replayLogGUI( context, net, log1);
			alignments2 = (new PNLogReplayer()).replayLogGUI( context, net, log2);
		} catch (ConnectionCannotBeObtained e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AStarException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// create RPST
		RPSTPetriNetConnection conn_rpst;
		try {
			// connection is found, no need for mapping step
			// connection is not found, another plugin to create such connection is automatically executed!!!
			conn_rpst = context.getConnectionManager().getFirstConnection(RPSTPetriNetConnection.class, context, net);
			System.out.println("RPST CONNECTION EXISTS");
		} catch (ConnectionCannotBeObtained e) {
			PetriNetRPST rpst = RPSTHelper.generateRPST(context, net, parameters.isIgnoreLongTermDependencies(), log1, alignments1, log2, alignments2);
			conn_rpst = new RPSTPetriNetConnection(net, rpst, new GenerateRPSTFromPetriNetParameters());
			context.addConnection(conn_rpst);
			System.out.println("RPST CONNECTION DOES NOT EXISTS");
		}
		//PetriNetRPST rpst = RPSTHelper.generateRPST(context, net, parameters.isIgnoreLongTermDependencies(), log1, alignments1, log2, alignments2);
		PetriNetRPST rpst = conn_rpst.getObjectWithRole(RPSTPetriNetConnection.RPST);
		return runDefault(context, rpst, alignments1, log1, alignments2, log2, parameters);
	}	
	
//	/**
//	 * Apply the algorithm depending on whether a connection already exists.
//	 * 
//	 * @param context The context to run in.
//	 * @param input1 The first input.
//	 * @param input2 The second input.
//	 * @return The output.
//	 */
//	private YourOutput runConnections(PluginContext context, YourFirstInput input1, YourSecondInput input2, YourParameters parameters) {
//		if (parameters.isTryConnections()) {
//			// Try to found a connection that matches the inputs and the parameters.
//			Collection<YourConnection> connections;
//			try {
//				connections = context.getConnectionManager().getConnections(
//						YourConnection.class, context, input1, input2);
//				for (YourConnection connection : connections) {
//					if (connection.getObjectWithRole(YourConnection.FIRSTINPUT)
//							.equals(input1) && connection.getObjectWithRole(YourConnection.SECONDINPUT)
//							.equals(input2) && connection.getParameters().equals(parameters)) {
//						// Found a match. Return the associated output as result of the algorithm.
//						return connection
//								.getObjectWithRole(YourConnection.OUTPUT);
//					}
//				}
//			} catch (ConnectionCannotBeObtained e) {
//			}
//		}
//		// No connection found. Apply the algorithm to compute a fresh output result.
//		YourOutput output = apply(context, input1, input2, parameters);
//		if (parameters.isTryConnections()) {
//			// Store a connection containing the inputs, output, and parameters.
//			context.getConnectionManager().addConnection(
//					new YourConnection(input1, input2, output, parameters));
//		}
//		// Return the output.
//		return output;
//	}
	
	
	private JComponent runConnections(PluginContext context, PetriNetRPST rpst, PNRepResult alignments1, XLog log1, PNRepResult alignments2, XLog log2, DecompositionParameters parameters) {
		final Measurement[] measurement_options = {new ReachedMeasurement(), new IndependentReachedMeasurement(), new LoopIterationMeasurement(), new TimeInFragmentMeasurement(), new FragmentWaitTimeMeasurement(), new ElapsedTimeMeasurement(), new FitnessMeasurement(), new ExecutionFitnessMeasurement(), new ActivitiyOutsideFragmentMeasurement(), new FragmentQueueMeasurement()};
		String result = "Nodes: " + rpst.getNodes().size() + "\n";
		result += "Traces: " + log1.size() + ", " + log2.size() + "\n";
		result += "\\begin{tabular}{c c c}\n\\hline\nMeasurement & Time & Significant Differences\\\\\n\\hline\n";
		for (Measurement measurement : measurement_options) {
			DecompositionParameters cur_params = new DecompositionParameters(parameters.getMapping1(), parameters.getMapping2());
			cur_params.setMeasurement(measurement);
			Instant start = Instant.now();
			RPSTDecomposition decomp = apply(context, rpst, alignments1, log1, alignments2, log2, cur_params, new DecompositionInfoProvider(rpst, alignments1, log1, alignments2, log2));
			Instant end = Instant.now();
			
			int significant_differences = 0;
			for (PetriNetRPSTNode frag : decomp.getInfo().getRPST().getNodes()) {
				if (decomp.getInfo().getPValue(frag)<0.05) {
					significant_differences += 1;
				}
			}
			
			result += measurement.getName() + " & " +   (new DurationMeasurementValue(Duration.between(start, end))).getValueString() + " & " + significant_differences + "\\\\\n";
		}
		result += "\\hline\n\\end{tabular}";

		JComponent mainComponent = new JPanel();
		// Create layout with two rows and one column.
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL} };
		mainComponent.setLayout(new TableLayout(size));
		mainComponent.add(new JTextArea(result), "0, 0"); // Upper left
		return mainComponent;
	}
}
;
