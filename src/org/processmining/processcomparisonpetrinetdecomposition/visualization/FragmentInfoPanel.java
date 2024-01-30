package org.processmining.processcomparisonpetrinetdecomposition.visualization;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPSTNode;
import org.processmining.processcomparisonpetrinetdecomposition.models.DecompositionInfoProvider;
import org.processmining.processcomparisonpetrinetdecomposition.models.RPSTDecomposition;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.ui.widgets.ProMTable;
import org.processmining.framework.util.ui.widgets.WidgetColors;

import info.clearthought.layout.TableLayout;

public class FragmentInfoPanel extends JPanel{
	private DecompositionInfoProvider info;
	// info table
	protected ProMTable table;
	// statistic content
	protected DefaultTableModel tableModel;
	// static content for GUI of statistic panel
	protected Object[] columnIdentifier;	
	
	public FragmentInfoPanel(PluginContext context, RPSTDecomposition decomposition) {
		super();
		info = decomposition.getInfo();
		// create table
		columnIdentifier = new Object[] { "Property", "Log 1", "Log 2" };

		tableModel = new DefaultTableModel() {
			private static final long serialVersionUID = -4303950078200984098L;

			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		table = new ProMTable(tableModel);
		showDefault();
		// add GUI
		double[][] size = new double[][] { { 500 }, { TableLayout.FILL, 30 } };
		setLayout(new TableLayout(size));
		add(table, "0,0");
		add(new JLabel("  * Click a fragment to see its stats"),
				"0,1");
		setBackground(WidgetColors.PROPERTIES_BACKGROUND);
	}
	
	protected void showFragStats(PetriNetRPSTNode fragment) {
		if (fragment == null) {
			showDefault();
		}
		else {
			Object[][] values = new Object[9][3];; // result
			
			values[0] = new Object[] { "Score", info.getScore(fragment), "-" };
			values[1] = new Object[] { "Difference", info.getDifference(fragment), "-" };
			values[2] = new Object[] { "p-Value", info.getPValue(fragment), "-" };
			values[3] = new Object[] { "Cohen's d", info.getDIndex(fragment), "-" };
			values[4] = new Object[] { ' ', ' ' };
			values[5] = new Object[] { "Measurement Averages", info.getAverage(fragment, 0).getValueString(), info.getAverage(fragment, 1).getValueString() };
			values[6] = new Object[] { "Measurement Medians", info.getMedian(fragment, 0).getValueString(), info.getMedian(fragment, 1).getValueString() };
			values[7] = new Object[] { "Fragment Fitness", info.getFitness(fragment, 0).getValueString(), info.getFitness(fragment, 1).getValueString() };
			values[8] = new Object[] { "Reached Rate", info.getReachedRate(fragment, 0).getValueString(), info.getReachedRate(fragment, 1).getValueString() };
			
			tableModel.setDataVector(values, columnIdentifier);
			table.doLayout();
		}
	}
	
	protected void showDefault() {
		Object[][] values = new Object[9][3];; // result
		
		values[0] = new Object[] { "Score", "", "" };
		values[1] = new Object[] { "Difference", "", "" };
		values[2] = new Object[] { "p-Value", "", "" };
		values[3] = new Object[] { "p-Value", "", "" };
		values[4] = new Object[] { ' ', ' ' };
		values[5] = new Object[] { "Measurement Averages", "", "" };
		values[6] = new Object[] { "Measurement Medians", "", "" };
		values[7] = new Object[] { "Fragment Fitness", "", "" };
		values[8] = new Object[] { "Times Reached", "", "" };
		
		tableModel.setDataVector(values, columnIdentifier);
		table.doLayout();
	}
}
