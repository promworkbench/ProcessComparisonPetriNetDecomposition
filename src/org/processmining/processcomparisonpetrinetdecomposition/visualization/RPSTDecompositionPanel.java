package org.processmining.processcomparisonpetrinetdecomposition.visualization;

import java.util.ArrayList;
import java.util.Map;

import javax.swing.JPanel;

import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPST;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPSTNode;
import org.processmining.processcomparisonpetrinetdecomposition.models.DecompositionInfoProvider;
import org.processmining.processcomparisonpetrinetdecomposition.models.RPSTDecomposition;
import org.processmining.processcomparisonpetrinetdecomposition.util.ColorHelper;
import org.processmining.framework.plugin.PluginContext;

public class RPSTDecompositionPanel extends BaseRPSTPanel {

	private FragmentInfoPanel fragInfoPanel;
	private GlobalInfoPanel globalInfoPanel;
	private AttributesInfoPanel attrInfoPanel;
	private ArrayList<PetriNetRPSTNode> decomp_nodes;
	private DecompositionInfoProvider info;
	
	public RPSTDecompositionPanel(PluginContext context, RPSTDecomposition decomposition) {
		super(context, decomposition.getRpst(), decomposition);
	}
	
	protected void setup(PluginContext context, PetriNetRPST rpst, Object[] args) {
		RPSTDecomposition decomposition = (RPSTDecomposition) args[0];
		info = decomposition.getInfo();
		decomp_nodes = decomposition.getDecomposition();
		
		globalInfoPanel = new GlobalInfoPanel(context, decomposition);
 		fragInfoPanel = new FragmentInfoPanel(context, decomposition);
 		attrInfoPanel = new AttributesInfoPanel(context, rpst, info.getAlignments1(), info.getLog1(), info.getAlignments2(), info.getLog2(), info.getLogName(0), info.getLogName(1));
		addInfo("Global Info", globalInfoPanel);
		addInfo("Fragment Info", fragInfoPanel);
		
		// add attributes info panel as new tab
		JPanel attributesTab = getInspector().addTab("Attributes");
		getInspector().addGroup(attributesTab, "Attributes", attrInfoPanel);
		
		// Information about the tool
		JPanel toolDescPanel = new ToolDescriptionPanel();
		JPanel toolInfoTab = getInspector().addTab("Tool Info");
		toolInfoTab.add(toolDescPanel);
		//getInspector().addGroup(toolInfoTab, "Tool Info", toolDescPanel);
	}

	protected void onNodeSelected(PetriNetRPSTNode node) {
		fragInfoPanel.showFragStats(node);
		attrInfoPanel.showFragStats(node);
	}

	protected void onDeSelected(PetriNetRPSTNode node) {
		fragInfoPanel.showDefault();
		attrInfoPanel.showDefault();
	}

	protected String getDefaultNodeColor(PetriNetRPSTNode node) {
		double d_index = info.getDIndex(node) > 1 ? 1 : info.getDIndex(node); // limit d to 1
		double signed_d_index = d_index * Math.signum(info.getDifference(node));
		return ColorHelper.awtColorToHexString(ColorHelper.bwr(signed_d_index));
	}

	protected void addDotOptions(PetriNetRPSTNode node, Map<String, String> options) {
		if (decomp_nodes.contains(node)) {
			//style += ", bold";
			//options.put("color", "gold");
			//options.put("penwidth", "5");
		}
//		if (1 - Math.abs(info.getDifference(node)) <= 0.05) {
//			options.put("color", "gold");
//			options.put("penwidth", "5");
//		}
	}
	
	protected String createNodeNameSuffix(PetriNetRPSTNode node) {
		String suffix = "";
		suffix += "Avg. 1: " + info.getAverage(node, 0).getValueString();
		suffix += "\nAvg. 2: " + info.getAverage(node, 1).getValueString();
		return suffix;
	}
	
}
