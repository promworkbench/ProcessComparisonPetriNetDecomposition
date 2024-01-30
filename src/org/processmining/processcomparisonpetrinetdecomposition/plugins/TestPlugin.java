package org.processmining.processcomparisonpetrinetdecomposition.plugins;

import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPST;
import org.processmining.processcomparisonpetrinetdecomposition.help.YourHelp;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;

@Plugin(name = "Test Plugin", parameterLabels = { "Model", "RPST", "alignments1", "log1", "alignments2", "log2", "parameters"}, 
returnLabels = { "Petri Net of fragment" }, returnTypes = { String.class }, help = YourHelp.TEXT)
public class TestPlugin {
	
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Moritz Gose", email = "moritz.gose@rwth-aachen.de")
	@PluginVariant(variantLabel = "Test", requiredParameterLabels = {0,1})
	public String runDefault(UIPluginContext context, Petrinet net, PetriNetRPST rpst) {

		return "";
	}
}



//@Plugin(name = "Test Plugin", parameterLabels = { "Model", "RPST", "alignments1", "log1", "alignments2", "log2", "parameters" }, 
//	    returnLabels = { "Petri Net of fragment" }, returnTypes = { JPanel.class }, help = YourHelp.TEXT)
//public class TestPlugin extends RPSTDecomposerAlgorithm {
//	final String column_key = "Values";
//	
//	public JPanel apply(PluginContext context, PetriNetRPST rpst, PNRepResult alignments1, XLog log1, PNRepResult alignments2, XLog log2, AssociationRulesParameters parameters) {
//		Map<PetriNetRPSTNode, Map<String, Integer>> fragment_attributes_map = new HashMap<>();
//		for (PetriNetRPSTNode fragment : rpst.getNodes()) {
//			Map<String, Integer> value_map = TestPlugin.attributesPerFragment(fragment, log2, alignments2, rpst.getNet().getNet(), "org:resource");
//			fragment_attributes_map.put(fragment, value_map);
//		}
//		CategoryDataset dataset = createCategoryDataset(fragment_attributes_map.get(rpst.getRoot()));
//		return createPanel(dataset);
//	}
//	
//	private CategoryDataset createCategoryDataset(Map<String, Integer> data) {
//		DefaultCategoryDataset dataset = new DefaultCategoryDataset();	
//		for (Entry<String, Integer> entry : data.entrySet()) {
//			dataset.addValue(entry.getValue(), entry.getKey(), column_key);
//		}
//		
//		return dataset;
//	}
//	
//	private JFreeChart createChart(CategoryDataset dataset) {
//		final String CHART_TITLE = "root attributes";
//		final String CATEGORY_AXIS_TITLE="attribute";
//		final String VALUE_AXIS_TITLE="number of occurrences";
//
//		JFreeChart chart = ChartFactory.createBarChart(
//		         CHART_TITLE,           
//		         CATEGORY_AXIS_TITLE,
//		         VALUE_AXIS_TITLE,
//		         dataset,        
//		         PlotOrientation.VERTICAL,           
//		         true, true, false);
//		return chart;
//	}
//	
//	private JPanel createPanel(CategoryDataset dataset) {
//		JPanel panel = new JPanel();
//		
//		JFreeChart chart = createChart(dataset);
//		
//        ChartPanel chartPanel = new ChartPanel(chart);
//        chartPanel.setMaximumDrawHeight(3000);
//        chartPanel.setMaximumDrawWidth(3000);
//        panel.add(chartPanel);
//
//        panel.setSize(2500, 600);
//        panel.setVisible(true);
//        return panel;
//	}
//	
//	private JPanel createPanel() {
//		JPanel panel = new JPanel();
//
//        double dataArray[] =
//        { 0.0, 1.0, 5.0, 5.0, 5.0, 8.0, 9.0, 9.0, 2.0, 3.0,
//            2.0, 0.0, 1.0, 5.0, 1.0, 0.0, 1.0, 0.0, 0.0, 1.0, 1.0, 2.0, 0.0,
//            0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
//            2.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0,
//            0.0, 0.0, 0.0, 0.0, 2.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0,
//            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
//            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
//            0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0,
//            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
//            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
//            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
//            0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
//            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
//            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
//            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
//            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
//            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
//            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
//            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
//            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
//            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
//            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
//            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
//            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
//            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
//            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
//            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
//            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
//            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
//            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
//            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
//            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
//            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
//            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 };
//		
//		JFreeChart chart = getHistogramChart("Test", dataArray);
//        ChartPanel chartPanel = new ChartPanel(chart);
//        chartPanel.setMaximumDrawHeight(3000);
//        chartPanel.setMaximumDrawWidth(3000);
//        panel.add(chartPanel);
//
//        panel.setSize(2500, 600);
//        panel.setVisible(true);
//        return panel;
//	}
//	
//	private JFreeChart getHistogramChart(String name, double[] dataArray)
//    {
//        String plotTitle = name;
//        String xAxisLabel = "Length of transaction";
//        String yAxis = "Frequency";
//        PlotOrientation orientation = PlotOrientation.VERTICAL;
//
//        DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
//        for (int i = 0; i < dataArray.length; i++)
//        {
//            dataSet.addValue(dataArray[i], (Integer) 0, (Integer) i);
//        }
//        boolean show = true;
//        boolean toolTips = false;
//        boolean urls = false;
//        JFreeChart chart = ChartFactory.createBarChart(plotTitle, xAxisLabel,
//            yAxis, dataSet, orientation, show, toolTips, urls);
//        chart.setBackgroundPaint(Color.WHITE);
//
//        // Set a very small font for the labels, and rotate them...
////        CategoryPlot plot = chart.getCategoryPlot();
////        CategoryAxis domainAxis = plot.getDomainAxis();
////        domainAxis.setTickLabelFont(new Font("Dialog", Font.PLAIN, 8));
////        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
//
//        return chart;
//    }
//	
//	
//	public static Map<String, Integer> attributesPerFragment(PetriNetRPSTNode fragment, XLog log, PNRepResult alignments, Petrinet net, String attribute) {
//		Map<String, Integer> values = new HashMap<>();
//		for (SyncReplayResult alignment : alignments) {
//			for (int trace_idx : alignment.getTraceIndex()) {
//				AlignmentReplayer replayer = new AlignmentReplayer(net, log.get(trace_idx), alignment, fragment);
//				while (replayer.hasNext()) {
//					replayer.next();
//					if (replayer.isInFragment() && replayer.curStepType() == Measurement.SYNC_MV &&
//							fragment.getTrans().contains(replayer.curModelStep())) {
//						if (replayer.curLogStep().getAttributes().keySet().contains(attribute)) {
//							incrementKey(values, replayer.curLogStep().getAttributes().get(attribute).toString());
//						}
//					}
//				}
//			}
//		}
//		return values;
//	}
//	
//	private static void incrementKey(Map<String, Integer> map, String key) {
//		if (map.containsKey(key)) {
//			map.put(key, map.get(key) + 1);
//		}
//		else {
//			map.put(key, 1);
//		}
//	}
//	
//	
//	
//	
//	
//	
//	
//	
//	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Moritz Gose", email = "moritz.gose@rwth-aachen.de")
//	@PluginVariant(variantLabel = "Test Plugin", requiredParameterLabels = { 0,1,2, 3, 4, 6 })
//	public JPanel runDefault(PluginContext context, PetriNetRPST rpst, PNRepResult alignments1, XLog log1, PNRepResult alignments2, XLog log2, AssociationRulesParameters parameters) {
//		return runConnections(context, rpst, alignments1, log1, alignments2, log2, parameters);
//	}	
//
//	/**
//	 * The standarad plugin variant to be used by the user in an UIPluginContext.
//	 * 
//	 * @param context The context to run in.
//	 * @param net Petri net over which the processes should be compared.
//	 * @param log1 First process for comparison.
//	 * @param log2 Second process for comparison.
//	 * @return RPSTDecomposition enriched with measurement information according to the measurement specified in the parameters.
//	 */
//	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Moritz Gose", email = "moritz.gose@rwth-aachen.de")
//	@PluginVariant(variantLabel = "Test Plugin", requiredParameterLabels = { 2, 4, 5 })
//	public JPanel runUI(UIPluginContext context, Petrinet net, XLog log1, XLog log2) {
//		AssociationRulesParameters parameters = createParameters(context, net, log1, log2);
//		if (parameters == null) {
//			return null;
//		}
//		return runUI(context, net, log1, log2, parameters);
//	}
//	
//	/**
//	 * The standard plugin variant in an UIPluginContext with already defined parameters
//	 * 
//	 * @param context The context to run in.
//	 * @param net Petri net over which the processes should be compared.
//	 * @param log1 First process for comparison.
//	 * @param log2 Second process for comparison.
//	 * @param parameters Decomposition parameters, need at least the transition event mappings for both logs and the model.
//	 * @return RPSTDecomposition enriched with measurement information according to the measurement specified in the parameters.
//	 */
//	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Moritz Gose", email = "moritz.gose@rwth-aachen.de")
//	@PluginVariant(variantLabel = "Test Plugin", requiredParameterLabels = { 2, 4, 5, 6 })
//	public JPanel runUI(UIPluginContext context, Petrinet net, XLog log1, XLog log2, AssociationRulesParameters parameters) {
//		// create Alignments
//		PNRepResult alignments1 = null;
//		PNRepResult alignments2 = null;
//		try {
//			alignments1 = (new PNLogReplayer()).replayLogGUI( context, net, log1);
//			alignments2 = (new PNLogReplayer()).replayLogGUI( context, net, log2);
//		} catch (ConnectionCannotBeObtained e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (AStarException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		// create RPST
//		PetriNetRPST rpst = RPSTHelper.generateRPST(context, net, parameters.isIgnoreLongTermDependencies(), log1, alignments1, log2, alignments2);
//		return runDefault(context, rpst, alignments1, log1, alignments2, log2, parameters);
//	}
//	
//	/**
//	 * Retrieves parameters by user input using a dialog.
//	 * @param context The context to run in.
//	 * @param net Petri net over which the processes should be compared.
//	 * @param log1 First process for comparison.
//	 * @param log2 Second process for comparison.
//	 * @return Null if dialog was cancelled, else the created parameters.
//	 */
//	private AssociationRulesParameters createParameters(UIPluginContext context, Petrinet net, XLog log1, XLog log2) {
//		// Get transition event mapping
//		// check connection in order to determine whether mapping step is needed or not
//		EvClassLogPetrinetConnection conn1;
//		EvClassLogPetrinetConnection conn2;
//		try {
//			// connection is found, no need for mapping step
//			// connection is not found, another plugin to create such connection is automatically executed!!!
//			conn1 = context.getConnectionManager().getFirstConnection(EvClassLogPetrinetConnection.class, context, net, log1);
//			conn2 = context.getConnectionManager().getFirstConnection(EvClassLogPetrinetConnection.class, context, net, log2);
//		} catch (Exception e) {
//			JOptionPane.showMessageDialog(new JPanel(), "No mapping can be constructed between the net and the log");
//			return null;
//		}
//		TransEvClassMapping mapping1 = conn1.getObjectWithRole(EvClassLogPetrinetConnection.TRANS2EVCLASSMAPPING);
//		TransEvClassMapping mapping2 = conn2.getObjectWithRole(EvClassLogPetrinetConnection.TRANS2EVCLASSMAPPING);
//		// Get the default parameters.
//		AssociationRulesParameters parameters = new AssociationRulesParameters(mapping1, mapping2);
//	    // Get a dialog for this parameters.
////	    AssociationRulesDialog dialog = new AssociationRulesDialog(context, net, parameters);
////	    // Show the dialog. User can now change the parameters.
////	    InteractionResult result = context.showWizard("Set Parameters", true, true, dialog);
////	    // User has close the dialog.
////	    if (result == InteractionResult.FINISHED) {
////			// Apply the algorithm depending on whether a connection already exists.
////	    	return parameters;
////	    }
////	    else {
////	    	return null;
////	    }
//		return parameters;
//	}	
//	
//	private JPanel runConnections(PluginContext context, PetriNetRPST rpst, PNRepResult alignments1, XLog log1, PNRepResult alignments2, XLog log2, AssociationRulesParameters parameters) {
//		return apply(context, rpst, alignments1, log1, alignments2, log2, parameters);
//	}
//}
