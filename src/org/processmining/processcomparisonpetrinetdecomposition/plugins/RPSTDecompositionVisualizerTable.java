package org.processmining.processcomparisonpetrinetdecomposition.plugins;

import java.util.Stack;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPST;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPSTNode;
import org.processmining.processcomparisonpetrinetdecomposition.help.YourHelp;
import org.processmining.processcomparisonpetrinetdecomposition.models.DecompositionInfoProvider;
import org.processmining.processcomparisonpetrinetdecomposition.models.RPSTDecomposition;
import org.processmining.processcomparisonpetrinetdecomposition.util.RPSTHelper;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;


@Plugin(name = "RPST-Decomposition Result Table", parameterLabels = { "RPST-Decomposition" }, 
returnLabels = { "Latex table code" }, returnTypes = { JComponent.class }, help = YourHelp.TEXT)
@Visualizer
public class RPSTDecompositionVisualizerTable {
	/**
	 * The plug-in variant that runs in any context and requires a parameters.
	 * 
	 * @param context The context to run in.
	 * @param input1 The first input.
	 * @param input2 The second input.
	 * @param parameters The parameters to use.
	 * @return The output.
	 */
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Moritz Gose, Tobias Brockhoff", email = "moritz.gose@rwth-aachen.de, brockhoff@pads.rwth-aachen.de")
	@PluginVariant(variantLabel = "Print RPST-Deomposition", requiredParameterLabels = { 0 })
	public JComponent run(PluginContext context, RPSTDecomposition decomposition) {
		DecompositionInfoProvider info = decomposition.getInfo();
		PetriNetRPST rpst = info.getRPST();
		
		String result = "\\begin{tabular}{c c c c c}\n\\hline\nFragment & p-Value & Cohen's d & avg 1 & avg 2 \\\\\n\\hline\n";
		Stack<PetriNetRPSTNode> stack = new Stack<>();
		stack.push(rpst.getRoot());
		
		while (!stack.empty()) {
			PetriNetRPSTNode cur = stack.pop();
			
			for (PetriNetRPSTNode child : rpst.getChildren(cur)) {
				stack.push(child);
			}
			  
			result += RPSTHelper.createNodeName(cur, rpst).replace("->", "$\\rightarrow$") + " & " + String.format("%.3f",info.getPValue(cur)) + " & " + String.format("%.2f",info.getDIndex(cur)) + " & " + info.getAverage(cur, 0).getValueString() + " & " + info.getAverage(cur, 1).getValueString() + "\\\\\n";                   
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
