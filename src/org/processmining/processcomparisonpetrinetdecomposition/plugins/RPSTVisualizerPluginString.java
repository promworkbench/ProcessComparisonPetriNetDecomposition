package org.processmining.processcomparisonpetrinetdecomposition.plugins;

import java.util.Collection;
import java.util.Stack;

import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPST;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPSTNode;
import org.processmining.processcomparisonpetrinetdecomposition.help.YourHelp;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;

import edu.uci.ics.jung.graph.DirectedGraph;

@Plugin(name = "RPST-Decomposition Visualizer (String)", parameterLabels = { "RPST" }, 
	    returnLabels = { "HTML String representing tree structure" }, returnTypes = { String.class }, help = YourHelp.TEXT)
public class RPSTVisualizerPluginString {

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
	public String run(PluginContext context, PetriNetRPST rpst) {
		String output = "<pre>";
		
		DirectedGraph<PetriNetRPSTNode, String> tree = rpst.getTree();
		PetriNetRPSTNode root = rpst.getRoot();
		Stack<PetriNetRPSTNode> stack = new Stack<PetriNetRPSTNode>();
		stack.push(root);
		int indent_level = 0;
		while(!stack.isEmpty()) {
			String indent = "";
			for (int i=0; i < indent_level; i++) {
				indent += "   ";
			}
			PetriNetRPSTNode cur = stack.pop();
			if (cur == null) {
				output += "),";
				indent_level -= 1;
			}
			else {
				PetrinetNode entry = cur.getEntry();
				PetrinetNode exit = cur.getExit();
				output += "\n" + indent + entry + " -> " + exit;
				output += "(";
				stack.push(null);
				Collection<PetriNetRPSTNode> sucs = tree.getSuccessors(cur);
				for (PetriNetRPSTNode suc : sucs) {
					stack.push(suc);
				}
				indent_level += 1;
			}
		}
				
		return output;
	}
}
