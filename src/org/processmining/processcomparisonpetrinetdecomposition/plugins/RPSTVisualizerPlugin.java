package org.processmining.processcomparisonpetrinetdecomposition.plugins;

import javax.swing.JComponent;

import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPST;
import org.processmining.processcomparisonpetrinetdecomposition.help.YourHelp;
import org.processmining.processcomparisonpetrinetdecomposition.visualization.RPSTVisualizerPanel;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

@Plugin(name = "RPST-Visualizer", parameterLabels = { "RPST" }, 
	    returnLabels = { "Dot Representation of the Tree" }, returnTypes = { JComponent.class }, help = YourHelp.TEXT)
@Visualizer
public class RPSTVisualizerPlugin {

	/**
	 * The plug-in variant that runs in any context and requires a parameters.
	 * 
	 * @param context The context to run in.
	 * @param input1 The first input.
	 * @param input2 The second input.
	 * @param parameters The parameters to use.
	 * @return The output.
	 */
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Moritz Gose, Tobias Brockhoff", 
	    email = "moritz.gose@rwth-aachen.de, brockhoff@pads.rwth-aachen.de")
	@PluginVariant(variantLabel = "Print RPST", requiredParameterLabels = { 0 })
	public JComponent run(PluginContext context, PetriNetRPST rpst) {
//		Dot dot = new Dot();
//		create_node(dot, rpst.getRoot(), rpst); // recursively create all nodes and edges
//		return new DotPanel(dot);
		return new RPSTVisualizerPanel(context, rpst);
	}
	
	
//	private DotNode create_node(Dot dot, PetriNetRPSTNode node, PetriNetRPST rpst) {
//		String node_name = "";
//		Collection<PetriNetRPSTNode> children = rpst.getChildren(node);
//		if (children.isEmpty()) { // node is leaf
//			node_name = node.getEntry() + "\n->\n" + node.getExit();
//		}
//		else { // node is inner-node
//			node_name = node.toString();
//		}
//		DotNode dot_node = dot.addNode(node_name);
//		dot.addNode(dot_node);
//		for (PetriNetRPSTNode child : children) {
//			DotEdge edge = dot.addEdge(dot_node, create_node(dot, child, rpst));
//		}
//		
//		return dot_node;
//	}
}
