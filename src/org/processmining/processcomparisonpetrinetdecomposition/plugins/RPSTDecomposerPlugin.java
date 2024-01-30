package org.processmining.processcomparisonpetrinetdecomposition.plugins;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPST;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPSTNode;
import org.processmining.acceptingpetrinetdecomposer.parameters.sese.GenerateRPSTFromPetriNetParameters;
import org.processmining.processcomparisonpetrinetdecomposition.algorithms.RPSTDecomposerAlgorithm;
import org.processmining.processcomparisonpetrinetdecomposition.connections.RPSTPetriNetConnection;
import org.processmining.processcomparisonpetrinetdecomposition.dialogs.DecompositionDialog;
import org.processmining.processcomparisonpetrinetdecomposition.help.YourHelp;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.FitnessMeasurement;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.ReachedMeasurement;
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

import nl.tue.astar.AStarException;

@Plugin(name = "Decompose Petri net for Process Comparison (RPST-based)", parameterLabels = { "RPST", "Alignments1", "Log1 (red)", "Alignments2", "Log2 (blue)", "Petri net", "Parameters" }, 
	    returnLabels = { "RPST Decomposition for PC" }, returnTypes = { RPSTDecomposition.class }, help = YourHelp.TEXT)
public class RPSTDecomposerPlugin extends RPSTDecomposerAlgorithm {

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
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Moritz Gose, Tobias Brockhoff", email = "moritz.gose@rwth-aachen.de, brockhoff@pads.rwth-aachen.de")
	@PluginVariant(variantLabel = "Logs, Alignments, RPST, Parameters", requiredParameterLabels = { 0, 1, 2, 3, 4, 6 })
	public RPSTDecomposition runDefault(PluginContext context, PetriNetRPST rpst, PNRepResult alignments1, XLog log1, PNRepResult alignments2, XLog log2, DecompositionParameters parameters) {
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
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Moritz Gose, Tobias Brockhoff", email = "moritz.gose@rwth-aachen.de, brockhoff@pads.rwth-aachen.de")
	@PluginVariant(variantLabel = "Logs and PN", requiredParameterLabels = { 2, 4, 5 })
	public RPSTDecomposition runUI(UIPluginContext context, XLog log1, XLog log2, Petrinet net) {
		DecompositionParameters parameters = createParameters(context, net, log1, log2);
		if (parameters == null) {
			return null;
		}
		return runUI(context, log1, log2, net, parameters);
	}
	
	/**
	 * The standard plugin variant in an UIPluginContext with already defined parameters
	 * 
	 * @param context The context to run in.
	 * @param net Petri net over which the processes should be compared.
	 * @param log1 First process for comparison.
	 * @param log2 Second process for comparison.
	 * @param parameters Decomposition parameters, need at least the transition event mappings for both logs and the model.
	 * @return RPSTDecomposition enriched with measurement information according to the measurement specified in the parameters.
	 */
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Moritz Gose, Tobias Brockhoff", email = "moritz.gose@rwth-aachen.de, brockhoff@pads.rwth-aachen.de")
	@PluginVariant(variantLabel = "Logs, Petri net, and Parameters", requiredParameterLabels = { 2, 4, 5, 6 })
	public RPSTDecomposition runUI(UIPluginContext context, XLog log1, XLog log2, Petrinet net, DecompositionParameters parameters) {
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
	
	/**
	 * Retrieves parameters by user input using a dialog.
	 * @param context The context to run in.
	 * @param net Petri net over which the processes should be compared.
	 * @param log1 First process for comparison.
	 * @param log2 Second process for comparison.
	 * @return Null if dialog was cancelled, else the created parameters.
	 */
	private DecompositionParameters createParameters(UIPluginContext context, Petrinet net, XLog log1, XLog log2) {
		// Get transition event mapping
		// check connection in order to determine whether mapping step is needed or not
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
	    // Get a dialog for this parameters.
	    DecompositionDialog dialog = new DecompositionDialog(context, net, parameters);
	    // Show the dialog. User can now change the parameters.
	    InteractionResult result = context.showWizard("Set Parameters", true, true, dialog);
	    // User has close the dialog.
	    if (result == InteractionResult.FINISHED) {
			// Apply the algorithm depending on whether a connection already exists.
	    	return parameters;
	    }
	    else {
	    	return null;
	    }
	}
	
	private RPSTDecomposition runConnections(PluginContext context, PetriNetRPST rpst, PNRepResult alignments1, XLog log1, PNRepResult alignments2, XLog log2, DecompositionParameters parameters) {
		DecompositionInfoProvider info = new DecompositionInfoProvider(rpst, alignments1, log1, alignments2, log2);
		
		RPSTDecomposition decomposition = apply(context, rpst, alignments1, log1, alignments2, log2, parameters, info);
		// add fragment fitness information
		DecompositionParameters fitness_parameters = new DecompositionParameters(parameters.getMapping1(), parameters.getMapping2());
		fitness_parameters.setMeasurement(new FitnessMeasurement());
		RPSTDecomposition fitness_decomposition = apply(context, rpst, alignments1, log1, alignments2, log2, fitness_parameters, new DecompositionInfoProvider(rpst, alignments1, log1, alignments2, log2));
		for (int i = 0; i < 2; i++) {
			for (PetriNetRPSTNode node : rpst.getNodes()) {
				info.addFitness(node, i, fitness_decomposition.getInfo().getAverage(node, i));
			}
		}
		// add times reached information
		DecompositionParameters reached_parameters = new DecompositionParameters(parameters.getMapping1(), parameters.getMapping2());
		reached_parameters.setMeasurement(new ReachedMeasurement());
		RPSTDecomposition reached_decomposition = apply(context, rpst, alignments1, log1, alignments2, log2, reached_parameters,new DecompositionInfoProvider(rpst, alignments1, log1, alignments2, log2));
		for (int i = 0; i < 2; i++) {
			for (PetriNetRPSTNode node : rpst.getNodes()) {
				info.addReachedRate(node, i, reached_decomposition.getInfo().getAverage(node, i));
			}
		}
		// add log names
		String name1;
		String name2;
		if (log1.getAttributes().keySet().contains("concept:name")) {
			name1 = log1.getAttributes().get("concept:name").toString();
		}
		else {
			name1 = "[log1]";
		}
		if (log2.getAttributes().keySet().contains("concept:name")) {
			name2 = log2.getAttributes().get("concept:name").toString();
		}
		else {
			name2 = "[log2]";
		}
		info.setLogName(0, name1);
		info.setLogName(1, name2);
		// add fitness
		double fitness1 = (double) alignments1.getInfo().get(PNRepResult.TRACEFITNESS);
		double fitness2 = (double) alignments2.getInfo().get(PNRepResult.TRACEFITNESS);
		info.setGlobalFitness(fitness1, fitness2);
		return decomposition;
	}
}
