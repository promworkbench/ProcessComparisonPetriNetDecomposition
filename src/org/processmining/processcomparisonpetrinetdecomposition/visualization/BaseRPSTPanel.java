package org.processmining.processcomparisonpetrinetdecomposition.visualization;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPST;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPSTNode;
import org.processmining.processcomparisonpetrinetdecomposition.util.ColorHelper;
import org.processmining.processcomparisonpetrinetdecomposition.util.RPSTHelper;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.ui.widgets.InspectorPanel;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.AttributeMapOwner;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotElement;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.graphviz.visualisation.listeners.DotElementSelectionListener;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGElementException;
import com.kitfox.svg.animation.AnimationElement;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public abstract class BaseRPSTPanel extends InspectorPanel{
	// colors
		final String SELECTED_COLOR = "#00ff00";
		final String POSDIFF_COLOR = "#ff0000";
		final String NEGDIFF_COLOR = "#00ff00";
		final String INDECOMPOSITION_COLOR = "#ffd700";
		final String DEFAULT_COLOR = "#ffffff";
		final String TEXT_COLOR = "#000000";	
		
		
		/**
		 * Component that wraps the Petri net and RPST
		 */
		private JComponent componentRPSTNetWrapper;
		
		/**
		 * Handle to the panel containing the RPST
		 */
		private DotPanel rpstPanel;
		
		/**
		 * Handle to the Panel containing the Petri net
		 */
		private ProMJGraphPanel netPanel;
		
		/**
		 * The currently selected node in the RPST
		 */
		private Optional<PetriNetRPSTNode> selectedRPSTNode;
		
		/**
		 * Currently minimized RPST Nodes
		 */
		private Set<PetriNetRPSTNode> minimizedRPSTNodes;
		
		private PluginContext context;
		
		private PetriNetRPST rpst;
		
		/**
		 * Creates and initializes a BaseRPSTPanel. It first calls setup() to setup subclass functionality and then initializes the panel.
		 * @param context Application context.
		 * @param rpst RPST for the visualization.
		 * @param args Optional arguments for subclasses.
		 */
		public BaseRPSTPanel(PluginContext context, PetriNetRPST rpst, Object... args) {
			super(context);
			this.context = context;
			this.rpst = rpst;
			this.selectedRPSTNode = Optional.empty();
			this.minimizedRPSTNodes = new HashSet<>();

			setup(context, rpst, args);
			// Initialize the Petri net panel
			this.netPanel = createPanelPetriNet(context, rpst.getNet().getNet());
			this.rpstPanel = createConnectPanelRPST(context, netPanel, rpst);
			addMinimizationListener(rpstPanel);
			this.componentRPSTNetWrapper = createPanelWrapperRPSTPetriNet(netPanel, rpstPanel);
			
			setLayout(new BorderLayout());
			add(componentRPSTNetWrapper);
		}
		
		// ------------- METHODS TO OVERWRITE -------------
		
		/**
		 * Called in constructor before initialize. Any global variable that is used should be initialized here.
		 * @param context PluginContext of application.
		 * @param rpst RPST for visualization.
		 * @param args Optional visualization specific parameters.
		 */
		protected abstract void setup(PluginContext context, PetriNetRPST rpst, Object[] args);
		
		/**
		 * Actions that should happen when an RPST node is selected (apart from changing the nodes color).
		 * @param node Selected node.
		 */
		protected abstract void onNodeSelected(PetriNetRPSTNode node);

		/**
		 * Actions that should happen when an RPST node is deselected (apart from changing the nodes color).
		 * @param node Deselected node.
		 */
		protected abstract void onDeSelected(PetriNetRPSTNode node);

		/**
		 * The default color for the given node that it has while it is not selected.
		 * @param node Considered RPST node.
		 * @return Hexstring of the color.
		 */
		protected abstract String getDefaultNodeColor(PetriNetRPSTNode node);

		/**
		 * Suffix that will be added to the node name inside the RPST. This can contain additional information regarding the node.
		 * Suffix starts in a new line, i.e. no \n is needed at the beginning.
		 * @param node Considered RPST node.
		 * @return String suffix.
		 */
		protected abstract String createNodeNameSuffix(PetriNetRPSTNode node);
		
		/**
		 * Add additional options for the dot visualization of the given node.
		 * @param node Node belonging to the new dot node.
		 * @param options Options map, by default it already includes the field "fill_color".
		 */
		protected abstract void addDotOptions(PetriNetRPSTNode node, Map<String, String> options);
		
		/**
		 * The style for the new dot node representing the given RPST node.
		 * @param node Visualized RPST node.
		 * @return Dot style String.
		 */
		private String createDotNodeStyle(PetriNetRPSTNode node) {
			return "filled";
		}
		
		// ----------- END METHODS TO OVERWRITE -----------
		private ProMJGraphPanel createPanelPetriNet(PluginContext context, Petrinet net) {
			return ProMJGraphVisualizer.instance().visualizeGraph(context, net);
		}
		
		private DotPanel createConnectPanelRPST(PluginContext context, ProMJGraphPanel netPanel, PetriNetRPST rpst) {
			Dot dot = new Dot();
			Petrinet net = rpst.getNet().getNet();
			
			HashMap<PetriNetRPSTNode, DotNode> rpstToDot = new HashMap<PetriNetRPSTNode, DotNode>();
			// Recursively create all nodes and edges in dot and add references to rpst_dot_map
			createDotNodesRec(dot, rpstToDot, rpst, rpst.getRoot()); 
			DotPanel rpstPanel = new DotPanel(dot);
			
			////////////////////////////////////////
			// Connect RPST and Net
			////////////////////////////////////////
			// add listeners to dot nodes
			for (Entry<PetriNetRPSTNode, DotNode> entry : rpstToDot.entrySet()) {
				PetriNetRPSTNode rpstNode = entry.getKey();
				DotNode dotNode = entry.getValue();
				
				// join the transition and place sets of the current rpst node
				HashSet<Object> net_nodes = (new HashSet<Object> (rpstNode.getTrans()));
				net_nodes.addAll(rpstNode.getPlaces());
				
				dotNode.addSelectionListener(new DotElementSelectionListener() {
					@Override
					public void selected(DotElement element, SVGDiagram img) {
						context.log("Dot element " + element.getLabel() + " has been clicked");
						
						selectedRPSTNode = Optional.of(rpstNode);
						// change color of rpst node
						set_rpst_color(rpstPanel, dotNode, SELECTED_COLOR);
						
						// change colors of petri net nodes
						for (Object net_node : net_nodes) {
							set_net_color(netPanel, net_node, SELECTED_COLOR);
						}
						
						// set info table to fragment
						onNodeSelected(rpstNode);
						
						netPanel.getGraph().updateUI();
						netPanel.updateUI();
						rpstPanel.updateUI();
					}

					@Override
					public void deselected(DotElement element, SVGDiagram img) {
						context.log("Dot element " + element.getLabel() + " has been clicked");

						selectedRPSTNode = Optional.empty();
						// change color of rpst node
						// first determine the default color of the node by checking the diff entry
						String color = getDefaultNodeColor(rpstNode);						
						// now set color
						set_rpst_color(rpstPanel, dotNode, color);	
						
						// change colors of petri net nodes
						for (Object net_node : net_nodes) {
							set_net_color(netPanel, net_node, DEFAULT_COLOR);
						}
						
						// reset info table
						onDeSelected(rpstNode);
						
						netPanel.getGraph().updateUI();
						netPanel.updateUI();
						rpstPanel.updateUI();
					}
				});			
			}
			
			return rpstPanel;
		}
		
		private JComponent createPanelWrapperRPSTPetriNet(ProMJGraphPanel netPanel, DotPanel rpstPanel) {
			JComponent mainComponent = new JPanel();
			// Create layout with two rows and one column.
			double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL, TableLayoutConstants.FILL } };
			mainComponent.setLayout(new TableLayout(size));
			mainComponent.add(rpstPanel, "0, 0"); // Upper left
			mainComponent.add(netPanel, "0, 1"); // Lower left
			return mainComponent;
		}
		
		private DotNode createDotNodesRec(Dot dot, HashMap<PetriNetRPSTNode, DotNode> rpstDotMap, 
				PetriNetRPST rpst, PetriNetRPSTNode root) {
			String node_name = "";
			Collection<PetriNetRPSTNode> children = rpst.getChildren(root);
			node_name = RPSTHelper.createNodeName(root, rpst);
			// add node text
			HashMap<String, String> options = new HashMap<String, String>();
			String style = createDotNodeStyle(root); // build style option: always contains filled but only sometimes bold
			node_name += "\n";
			node_name += createNodeNameSuffix(root);
			// set node colour
			String color = "";
			color = getDefaultNodeColor(root);
			options.put("fillcolor", color);
			addDotOptions(root, options);
			if (this.minimizedRPSTNodes.contains(root)) {
				options.put("color", "gold");
				options.put("penwidth", "5");
			}
			// set outline colour (if inside decomposition)
			options.put("style", style);
			
			DotNode dotNode = dot.addNode(node_name, options);
			dot.addNode(dotNode);
			rpstDotMap.put(root, dotNode);
			// Recursively add children if not minimized
			if (!this.minimizedRPSTNodes.contains(root)) {
				for (PetriNetRPSTNode child : children) {
					DotNode dotNodeChild = createDotNodesRec(dot, rpstDotMap, rpst, child);
					DotEdge edge = dot.addEdge(dotNode, dotNodeChild);
				}
			}
			
			return dotNode;
		}

		private void set_rpst_color(DotPanel rpst_panel, DotNode dot_node, String color) {
			SVGElement svg_node = rpst_panel.getImage().getElement(dot_node.getId());
			try {
				if(svg_node.getChild(2).hasAttribute("fill", AnimationElement.AT_CSS)) {
					svg_node.getChild(2).setAttribute("fill", AnimationElement.AT_CSS, TEXT_COLOR); // text color
				}
				else {
					svg_node.getChild(2).addAttribute("fill", AnimationElement.AT_CSS, TEXT_COLOR);
				}
				if(svg_node.getChild(1).hasAttribute("fill", AnimationElement.AT_CSS)) {
					svg_node.getChild(1).setAttribute("fill", AnimationElement.AT_CSS, color); // background color
				}
				else {
					svg_node.getChild(1).addAttribute("fill", AnimationElement.AT_CSS, color);
				}
			} catch (SVGElementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
		private void set_net_color(ProMJGraphPanel net_panel, Object net_node, String color) {
			net_panel.getGraph().getViewSpecificAttributes().putViewSpecific((AttributeMapOwner) net_node, AttributeMap.FILLCOLOR, ColorHelper.hexStringToAWTColor(color));

		}
		
		private void addMinimizationListener(DotPanel rpstPanel) {
			rpstPanel.addKeyListener(new KeyListener() {
				
				public void keyTyped(KeyEvent e) {
				}
				
				public void keyReleased(KeyEvent e) {
					// key M pressed
					if (e.getKeyCode() == 77 ) {
						// If node selected
						if (selectedRPSTNode.isPresent()) {
							// Add or remove from minimized nodes
							if (minimizedRPSTNodes.contains(selectedRPSTNode.get())) {
								minimizedRPSTNodes.remove(selectedRPSTNode.get());
							}
							else {
								minimizedRPSTNodes.add(selectedRPSTNode.get());
							}
							
							// Re-create Dot Panel
							DotPanel updateRpstPanel = createConnectPanelRPST(context, netPanel, rpst);
							addMinimizationListener(updateRpstPanel);
							
							// Remove old
							componentRPSTNetWrapper.remove(rpstPanel);
							// Add new
							componentRPSTNetWrapper.add(updateRpstPanel, "0, 0"); // Upper left
							componentRPSTNetWrapper.updateUI();
						}
						
					}
				}
				
				public void keyPressed(KeyEvent e) {
					// TODO Auto-generated method stub
					
				}
			});
		}
}


