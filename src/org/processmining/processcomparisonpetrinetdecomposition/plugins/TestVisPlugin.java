package org.processmining.processcomparisonpetrinetdecomposition.plugins;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.plugins.VisualizeAcceptingPetriNetPlugin;
import org.processmining.processcomparisonpetrinetdecomposition.help.YourHelp;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.AttributeMapOwner;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.processmining.plugins.graphviz.dot.Dot;
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

@Plugin(name = "Test Vis", parameterLabels = { "net" }, 
	    returnLabels = { "Dot Representation of the Decomposition" }, returnTypes = { JComponent.class }, help = YourHelp.TEXT)
public class TestVisPlugin {

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
	@PluginVariant(variantLabel = "TestViz", requiredParameterLabels = { 0 })
	public JComponent run(PluginContext context, AcceptingPetriNet net) {
		Dot dot = new Dot();
		DotNode dot_node1 = dot.addNode("node 1");
		dot.addNode(dot_node1);
		DotNode dot_node2 = dot.addNode("node 2");
		dot.addNode(dot_node2);
		DotPanel panel = new DotPanel(dot);
		
		SVGDiagram image = panel.getImage();
		
		
		ProMJGraphPanel panel2 = (ProMJGraphPanel) VisualizeAcceptingPetriNetPlugin.visualize(context, net);
		ProMJGraph jGraph = panel2.getGraph();
		ViewSpecificAttributeMap map = jGraph.getViewSpecificAttributes();

		dot_node1.addSelectionListener(new DotElementSelectionListener() {
			@Override
			public void selected(DotElement element, SVGDiagram img) {
				context.log("Dot element " + element.getLabel() + " has been clicked");
				//DotPanel.setCSSAttributeOf(img, element, "fill", SELECTED_COLOR); 
				SVGElement node1 = panel.getImage().getElement(dot_node1.getId());
				try {
					if(node1.getChild(2).hasAttribute("fill", AnimationElement.AT_CSS)) {
						node1.getChild(2).setAttribute("fill", AnimationElement.AT_CSS, "#000000");
					}
					else {
						node1.getChild(2).addAttribute("fill", AnimationElement.AT_CSS, "#000000");
					}
				} catch (SVGElementException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					if(node1.getChild(1).hasAttribute("fill", AnimationElement.AT_CSS)) {
						node1.getChild(1).setAttribute("fill", AnimationElement.AT_CSS, SELECTED_COLOR);
					}
					else {
						node1.getChild(1).addAttribute("fill", AnimationElement.AT_CSS, SELECTED_COLOR);
					}
				} catch (SVGElementException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				for (AttributeMapOwner owner : net.getNet().getTransitions()) {
					context.log(owner.toString());	
					map.putViewSpecific(owner, AttributeMap.FILLCOLOR, new Color(255, 0, 0));
				}
				jGraph.updateUI();
				panel2.updateUI();
				
				panel.updateUI();
			}
			@Override
			public void deselected(DotElement element, SVGDiagram img) {
				context.log("Dot element " + element.getLabel() + " has been clicked");
				//DotPanel.setCSSAttributeOf(img, element, "fill", DESELECTED_COLOR); 
				
				SVGElement node1 = panel.getImage().getElement(dot_node1.getId());
				try {
					if(node1.getChild(2).hasAttribute("fill", AnimationElement.AT_CSS)) {
						node1.getChild(2).setAttribute("fill", AnimationElement.AT_CSS, "#000000");
					}
					else {
						node1.getChild(2).addAttribute("fill", AnimationElement.AT_CSS, "#000000");
					}
				} catch (SVGElementException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					if(node1.getChild(1).hasAttribute("fill", AnimationElement.AT_CSS)) {
						node1.getChild(1).setAttribute("fill", AnimationElement.AT_CSS, DESELECTED_COLOR);
					}
					else {
						node1.getChild(1).addAttribute("fill", AnimationElement.AT_CSS, DESELECTED_COLOR);
					}
				} catch (SVGElementException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				for (AttributeMapOwner owner : net.getNet().getEdges()) {
					context.log(owner.toString());	
					map.putViewSpecific(owner, AttributeMap.FILLCOLOR, new Color(255, 255, 255));
				}
				jGraph.updateUI();
				panel2.updateUI();
				
				panel.updateUI();
			}
		});
		
		// display both components within the same window using a table layout:
		JComponent mainComponent = new JPanel();
		// Create layout with two rows and one column.
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL, TableLayoutConstants.FILL } };
		mainComponent.setLayout(new TableLayout(size));
		mainComponent.add(panel, "0, 0"); // Upper left
		mainComponent.add(panel2, "0, 1"); // Lower left
		
		return mainComponent;
	}
	
	final String SELECTED_COLOR = "#FF0000";
	final String DESELECTED_COLOR = "#FFFFFF";
	
//	new SVGElementManipulator<Group>() {
//
//		@Override
//		public void applySVGManipulation(Group svgElement) {
//			Text nodeLabel = (Text) svgElement.getChild(2);
//			StyleAttribute attrib = new StyleAttribute("x");
//			try {
//				// By default, the test anchor is middle bottom
//				nodeLabel.getStyle(attrib);
//				float xText = attrib.getFloatValue();
//				attrib = new StyleAttribute("y");
//				nodeLabel.getStyle(attrib);
//				float yText = attrib.getFloatValue();
//				
//				float x = xText - (pic.getLayoutInfo().getW() / 2f);
//				float y = yText + 5;
//
//				Group g = new Group();
//				g.addAttribute("transform", AnimationElement.AT_XML, 
//						String.format(Locale.ROOT, "translate(%f, %f)", x, y));
//				svgElement.loaderAddChild(null, g);
//				pic.add2SVG(g);
//			} catch (SVGElementException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (SVGException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	};
	
//	protected void fireTransitionSelected(Transition t) {
//		TransitionSelectedEvent transSelectEvent = null;
//		 // Guaranteed to return a non-null array
//		 Object[] listeners = listenerList.getListenerList();
//		 // Process the listeners last to first, notifying
//		 // those that are interested in this event
//		 for (int i = listeners.length-2; i >= 0; i-=2) {
//			 if (listeners[i] == TransitionClickListener.class) {
//				 // Lazily create the event:
//				 if (transSelectEvent == null)
//					 transSelectEvent = new TransitionSelectedEventImpl(t);
//				 ((TransitionClickListener) listeners[i+1]).transitionSelected(transSelectEvent);
//			 }
//		 }
//	 }

}
