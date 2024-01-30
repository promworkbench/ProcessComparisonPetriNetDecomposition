package org.processmining.processcomparisonpetrinetdecomposition.visualization;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.deckfour.xes.model.XLog;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPST;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPSTNode;
import org.processmining.processcomparisonpetrinetdecomposition.algorithms.AttributesAnalysisAlgorithm;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.ui.widgets.WidgetColors;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;

public class AttributesInfoPanel extends JPanel{
	// Constants
	private final String COLUMN_KEY = "Value";
	private final int NUMBER_BINS = 20;
	private final String CATEGORY_AXIS_TITLE="attribute";
	private final String VALUE_AXIS_TITLE="number of occurrences";
	
	//
	private Map<PetriNetRPSTNode, Map<String, Integer>> fragment_attributes_map1;
	private Map<PetriNetRPSTNode, Map<String, Integer>> fragment_attributes_map2;
	private long total_events1;
	private long total_events2;
	
	// GUI
	JComboBox selection;
	JPanel panel;
	private ChartPanel chart_panel1;
	private ChartPanel chart_panel2;
	private PetriNetRPSTNode curVisualizedFragment;
	Object[] items;

	// Base data for computations
	private PetriNetRPST rpst;
	private PNRepResult alignments1;
	private XLog log1;
	private PNRepResult alignments2;
	private XLog log2;
	private String name_log1;
	private String name_log2;
	
	AttributesAnalysisAlgorithm algo;
	
	public AttributesInfoPanel(PluginContext context, PetriNetRPST rpst, PNRepResult alignments1, XLog log1, PNRepResult alignments2,
			XLog log2, String name_log1, String name_log2) {
		super();
		this.rpst = rpst;
		this.alignments1 = alignments1;
		this.log1 = log1;
		this.alignments2 = alignments2;
		this.log2 = log2;
		this.algo = new AttributesAnalysisAlgorithm();
		this.curVisualizedFragment = null;
		this.total_events1 = algo.countTotalEvents(log1);
		this.total_events2 = algo.countTotalEvents(log2);
		this.name_log1 = name_log1;
		this.name_log2 = name_log2;
				
		// create panel to represent charts
		chart_panel1 = new ChartPanel(createBarChart(new DefaultCategoryDataset(), name_log1));
		chart_panel2 = new ChartPanel(createBarChart(new DefaultCategoryDataset(), name_log2));
		
		// create dropdown attribute selection
		SlickerFactory factory = SlickerFactory.instance();
		items = algo.getAttributeNames(log1, log2);
		selection = factory.createComboBox(items);
		selection.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String attribute = (String) items[selection.getSelectedIndex()];
				fragment_attributes_map1 = algo.apply(context, rpst, alignments1, log1, attribute);
				fragment_attributes_map2 = algo.apply(context, rpst, alignments2, log2, attribute);
				showFragStats(curVisualizedFragment);
			}

		});
//		selection.setSelectedIndex(0);		
		
		// add GUI
		panel = new JPanel();
		panel.setPreferredSize(new Dimension(800,700));
				
		double[][] size = new double[][] { { 800 }, { 30, 300, 300, 30 } };
		panel.setLayout(new TableLayout(size));
		panel.add(selection, "0,0");
		panel.add(chart_panel1, "0,1");
		panel.add(chart_panel2, "0,2");
		panel.add(new JLabel("  * Click a fragment to see its stats"),
				"0,3");
		panel.revalidate();
		
		JScrollPane scrollpane = new JScrollPane(panel);
		//scrollpane.add(panel);
		scrollpane.setPreferredSize(new Dimension(800,500));

		add(scrollpane);
		setBackground(WidgetColors.PROPERTIES_BACKGROUND);
	}

	protected void showFragStats(PetriNetRPSTNode fragment) {
		curVisualizedFragment = fragment;
		if (fragment == null || fragment_attributes_map1 == null || fragment_attributes_map2 == null) {
			showDefault();
		}
		else {
			chart_panel1.setChart(createChart(fragment_attributes_map1.get(fragment), name_log1));
			chart_panel2.setChart(createChart(fragment_attributes_map2.get(fragment), name_log2));
		}
		panel.revalidate();
	}
	
	protected void showDefault() {
		chart_panel1.setChart(createBarChart(new DefaultCategoryDataset(), name_log1));
		chart_panel2.setChart(createBarChart(new DefaultCategoryDataset(), name_log2));
	}
	
	
	private JFreeChart createChart(Map<String, Integer> data, String log_name) {
		if (data.size() == 0) return createBarChart(new DefaultCategoryDataset(), name_log1);
		
		boolean all_numeric = true;
		for (String key : data.keySet()) {
			if (!key.matches("-?\\d+(\\.\\d+)?")) {
				all_numeric = false;
				break;
			}
		}
		
		JFreeChart chart;
		if (all_numeric) { // create histogram
			chart = createHistogram(createHistgramDataset(data), log_name);
		}
		else { // create barchart
			chart = createBarChart(createCategoryDataset(data), log_name);
		}
		
		return chart;
	}
	
	private HistogramDataset createHistgramDataset(Map<String, Integer> data) {
		int size = 0;
		for (Integer value : data.values()) {
			size += value;
		}
		
		double[] values = new double[size];
		int index = 0;
		for (Entry<String, Integer> entry : data.entrySet()) {
			double value = Double.parseDouble(entry.getKey());
			int amount = entry.getValue();
			for (int i = 0; i < amount; i++) {
				values[index] = value;
				index += 1;
			}
		}
		
		HistogramDataset dataset = new HistogramDataset();
		dataset.addSeries(COLUMN_KEY, values, NUMBER_BINS);
		
		return dataset;
	}
	
	private JFreeChart createHistogram(HistogramDataset dataset, String log_name) {
		JFreeChart chart = ChartFactory.createHistogram(log_name, CATEGORY_AXIS_TITLE, VALUE_AXIS_TITLE, dataset, PlotOrientation.VERTICAL, true, true, false);
		return chart;
	}
	
	private CategoryDataset createCategoryDataset(Map<String, Integer> data) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();	
		for (Entry<String, Integer> entry : data.entrySet()) {
			dataset.addValue(entry.getValue(), entry.getKey(), COLUMN_KEY);
		}
		
		return dataset;
	}
	
	private JFreeChart createBarChart(CategoryDataset dataset, String log_name) {
		JFreeChart chart = ChartFactory.createBarChart(
		         log_name,           
		         CATEGORY_AXIS_TITLE,
		         VALUE_AXIS_TITLE,
		         dataset,        
		         PlotOrientation.VERTICAL,           
		         true, true, false);
		return chart;
	}
}
