package org.processmining.processcomparisonpetrinetdecomposition.visualization;

import java.util.Map;

import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPST;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPSTNode;
import org.processmining.framework.plugin.PluginContext;

public class RPSTVisualizerPanel extends BaseRPSTPanel {
	public RPSTVisualizerPanel(PluginContext context, PetriNetRPST rpst) {
		super(context, rpst);
	}

	protected void setup(PluginContext context, PetriNetRPST rpst, Object[] args) {
		// noop
	}	
	
	protected void onNodeSelected(PetriNetRPSTNode node) {
		// noop
	}

	protected void onDeSelected(PetriNetRPSTNode node) {
		// noop
	}

	protected String getDefaultNodeColor(PetriNetRPSTNode node) {
		return DEFAULT_COLOR;
	}
	
	protected void addDotOptions(PetriNetRPSTNode node, Map<String, String> options) {
		// noop
	}

	protected String createNodeNameSuffix(PetriNetRPSTNode node) {
		return "";
	}



	
}
