package org.processmining.processcomparisonpetrinetdecomposition.plugins;

import javax.swing.JComponent;

import org.processmining.processcomparisonpetrinetdecomposition.help.YourHelp;
import org.processmining.processcomparisonpetrinetdecomposition.models.RPSTDecomposition;
import org.processmining.processcomparisonpetrinetdecomposition.visualization.RPSTDecompositionPanel;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

@Plugin(name = "#RPST-Decomposition Visualizer", parameterLabels = { "RPST-Decomposition" }, 
	    returnLabels = { "Dot Representation of the Decomposition" }, returnTypes = { JComponent.class }, help = YourHelp.TEXT)
@Visualizer
public class DecompositionVisualizerPlugin {
	/**
	 * The plug-in variant that runs in any context and requires a parameters.
	 * 
	 * @param context The context to run in.
	 * @param input1 The first input.
	 * @param input2 The second input.
	 * @param parameters The parameters to use.
	 * @return The output.
	 */
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Moritz Gose", email = "moritz.gose@rwth-aachen.de")
	@PluginVariant(variantLabel = "Print RPST-Deomposition", requiredParameterLabels = { 0 })
	public JComponent run(PluginContext context, RPSTDecomposition decomposition) {
		return new RPSTDecompositionPanel(context, decomposition);
	}
}
	
	
	
	
//	// colors
//	final String SELECTED_COLOR = "#00ff00";
//	final String POSDIFF_COLOR = "#ff0000";
//	final String NEGDIFF_COLOR = "#00ff00";
//	final String INDECOMPOSITION_COLOR = "#ffd700";
//	final String DEFAULT_COLOR = "#ffffff";
//	final String TEXT_COLOR = "#000000";	
//
//	/**
//	 * The plug-in variant that runs in any context and requires a parameters.
//	 * 
//	 * @param context The context to run in.
//	 * @param input1 The first input.
//	 * @param input2 The second input.
//	 * @param parameters The parameters to use.
//	 * @return The output.
//	 */
//	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Moritz Gose", email = "moritz.gose@rwth-aachen.de")
//	@PluginVariant(variantLabel = "Print RPST-Deomposition", requiredParameterLabels = { 0 })
//	public JComponent run(PluginContext context, RPSTDecomposition decomposition) {
//		Dot dot = new Dot();
//		PetriNetRPST rpst = decomposition.getRpst();
//		Petrinet net = rpst.getNet().getNet();
//		HashMap<PetriNetRPSTNode, ImmutablePair<MeasurementValue, MeasurementValue>> measure_vals = decomposition.getMeasurement_values();
//		HashMap<PetriNetRPSTNode, Double> diffs = decomposition.getDifferences();
//		ArrayList<PetriNetRPSTNode> decomp_nodes = decomposition.getDecomposition();
//		
//		HashMap<PetriNetRPSTNode, DotNode> rpst_dot_map = new HashMap<PetriNetRPSTNode, DotNode>();
//	
//		// recursively create all nodes and edges in dot and add references to rpst_dot_map
//		create_node(dot, rpst_dot_map, rpst.getRoot(), rpst, decomp_nodes, measure_vals, diffs); 
//		
//		DotPanel rpst_panel = new DotPanel(dot);
//		ProMJGraphPanel net_panel = ProMJGraphVisualizer.instance().visualizeGraph(context, net);
//		ProMJGraph net_panel_jGraph = net_panel.getGraph();
//		ViewSpecificAttributeMap net_panel_map = net_panel_jGraph.getViewSpecificAttributes();
//		
//		// add listeners to dot nodes
//		for (Entry<PetriNetRPSTNode, DotNode> entry : rpst_dot_map.entrySet()) {
//			PetriNetRPSTNode node = entry.getKey();
//			DotNode dot_node = entry.getValue();
//			
//			// join the transition and place sets of the current rpst node
//			HashSet<Object> net_nodes = (new HashSet<Object> (node.getTrans()));
//			net_nodes.addAll(node.getPlaces());
//			
//			dot_node.addSelectionListener(new DotElementSelectionListener() {
//			@Override
//			public void selected(DotElement element, SVGDiagram img) {
//				context.log("Dot element " + element.getLabel() + " has been clicked");
//				
//				// change color of rpst node
//				set_rpst_color(rpst_panel, dot_node, SELECTED_COLOR);
//				
//				// change colors of petri net nodes
//				for (Object net_node : net_nodes) {
//					set_net_color(net_panel, net_node, SELECTED_COLOR);
//				}
//				
//				net_panel_jGraph.updateUI();
//				net_panel.updateUI();
//				rpst_panel.updateUI();
//			}
//			@Override
//			public void deselected(DotElement element, SVGDiagram img) {
//				context.log("Dot element " + element.getLabel() + " has been clicked");
//				
//				// change color of rpst node
//				// first determine the default color of the node by checking the diff entry
//				String color = null;
//				if (diffs.containsKey(node)) {
//					double diff = diffs.get(node);
////					if (diff < 0) {
////						color = NEGDIFF_COLOR;
////					}
////					else if (diff > 0) {
////						color = POSDIFF_COLOR;
////					}
////					else {
////						color = DEFAULT_COLOR;
////					}
//					color = ColorHelper.awtColorToHexString(ColorHelper.bwr(diff));
//				}
//				
//				// now set color
//				set_rpst_color(rpst_panel, dot_node, color);	
//				
//				// change colors of petri net nodes
//				for (Object net_node : net_nodes) {
//					set_net_color(net_panel, net_node, DEFAULT_COLOR);
//				}
//				
//				net_panel_jGraph.updateUI();
//				net_panel.updateUI();
//				rpst_panel.updateUI();
//			}
//		});			
//		}
//		
//		JComponent mainComponent = new JPanel();
//		// Create layout with two rows and one column.
//		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL, TableLayoutConstants.FILL } };
//		mainComponent.setLayout(new TableLayout(size));
//		mainComponent.add(rpst_panel, "0, 0"); // Upper left
//		mainComponent.add(net_panel, "0, 1"); // Lower left
//		
//		return mainComponent;
//	}
//	
//	
//	private DotNode create_node(Dot dot, HashMap<PetriNetRPSTNode, DotNode> rpst_dot_map, PetriNetRPSTNode node, PetriNetRPST rpst, ArrayList<PetriNetRPSTNode> decomp_nodes, 
//			HashMap<PetriNetRPSTNode, ImmutablePair<MeasurementValue, MeasurementValue>> measure_vals, HashMap<PetriNetRPSTNode, Double> diffs) {
//		String node_name = "";
//		Collection<PetriNetRPSTNode> children = rpst.getChildren(node);
//		if (children.isEmpty()) { // node is leaf
//			node_name = node.getEntry() + "\n->\n" + node.getExit();
//		}
//		else { // node is inner-node
//			node_name = node.toString();
//		}
//		// add node info
//		HashMap<String, String> options = new HashMap<String, String>();
//		String style = "filled"; // build style option: always contains filled but only sometimes bold
//		if (measure_vals.containsKey(node)) {
//			
//			
//			node_name += "\nAvg. 1: " + measure_vals.get(node).getLeft().getValueString();
//			node_name += "\nAvg. 2: " + measure_vals.get(node).getRight().getValueString();
//		}
//		if (diffs.containsKey(node)) {
//			String color = "";
//			double diff = diffs.get(node);
////			if (diff < 0) {
////				color = NEGDIFF_COLOR;
////			}
////			else if (diff > 0) {
////				color = POSDIFF_COLOR;
////			}
////			else {
////				color = DEFAULT_COLOR;
////			}
//			color = ColorHelper.awtColorToHexString(ColorHelper.bwr(diff));
//			options.put("fillcolor", color);
//		}
//		else {
//			options.put("fillcolor", "#A0A0A0");
//		}
//		if (decomp_nodes.contains(node)) {
//			//style += ", bold";
//			options.put("color", "gold");
//			options.put("penwidth", "5");
//		}
//		options.put("style", style);
//		
//		DotNode dot_node = dot.addNode(node_name, options);
//		dot.addNode(dot_node);
//		for (PetriNetRPSTNode child : children) {
//			DotEdge edge = dot.addEdge(dot_node, create_node(dot, rpst_dot_map, child, rpst, decomp_nodes, measure_vals, diffs));
//		}
//		
//		rpst_dot_map.put(node, dot_node);
//		return dot_node;
//	}
//	
//	
//	private void set_rpst_color(DotPanel rpst_panel, DotNode dot_node, String color) {
//		SVGElement svg_node = rpst_panel.getImage().getElement(dot_node.getId());
//		try {
//			if(svg_node.getChild(2).hasAttribute("fill", AnimationElement.AT_CSS)) {
//				svg_node.getChild(2).setAttribute("fill", AnimationElement.AT_CSS, TEXT_COLOR); // text color
//			}
//			else {
//				svg_node.getChild(2).addAttribute("fill", AnimationElement.AT_CSS, TEXT_COLOR);
//			}
//			if(svg_node.getChild(1).hasAttribute("fill", AnimationElement.AT_CSS)) {
//				svg_node.getChild(1).setAttribute("fill", AnimationElement.AT_CSS, color); // background color
//			}
//			else {
//				svg_node.getChild(1).addAttribute("fill", AnimationElement.AT_CSS, color);
//			}
//		} catch (SVGElementException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}	
//	}
//	
//	private void set_net_color(ProMJGraphPanel net_panel, Object net_node, String color) {
//		net_panel.getGraph().getViewSpecificAttributes().putViewSpecific((AttributeMapOwner) net_node, AttributeMap.FILLCOLOR, ColorHelper.hexStringToAWTColor(color));
//
//	}
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//}
