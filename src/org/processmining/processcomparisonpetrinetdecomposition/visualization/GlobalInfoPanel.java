package org.processmining.processcomparisonpetrinetdecomposition.visualization;

import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

import org.processmining.processcomparisonpetrinetdecomposition.models.DecompositionInfoProvider;
import org.processmining.processcomparisonpetrinetdecomposition.models.RPSTDecomposition;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.ui.widgets.ProMTable;
import org.processmining.framework.util.ui.widgets.WidgetColors;

import info.clearthought.layout.TableLayout;

public class GlobalInfoPanel extends JPanel{
	private DecompositionInfoProvider info;
	// info table
	protected ProMTable table;
	// statistic content
	protected DefaultTableModel tableModel;
	// static content for GUI of statistic panel
	protected Object[] columnIdentifier;	
	
	public GlobalInfoPanel(PluginContext context, RPSTDecomposition decomposition) {
		super();
		info = decomposition.getInfo();
		// create table
		columnIdentifier = new Object[] { "Property", "Value" };

		tableModel = new DefaultTableModel() {
			private static final long serialVersionUID = -4303950078200984098L;

			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		table = new ProMTable(tableModel);
		setInfo();
		// add GUI
		double[][] size = new double[][] { { 500 }, { TableLayout.FILL } };
		setLayout(new TableLayout(size));
		add(table, "0,0");
		setBackground(WidgetColors.PROPERTIES_BACKGROUND);
	}
	
	public void setInfo() {
		Object[][] values = new Object[4][2];; // result
		
		values[0] = new Object[] { "Log 1", info.getLogName(0) };
		values[1] = new Object[] { "Log 2", info.getLogName(1) };
		values[2] = new Object[] { "Fitness Log 1", info.getGlobalFitness(0) };
		values[3] = new Object[] { "Fitness Log 2", info.getGlobalFitness(1) };
		
		tableModel.setDataVector(values, columnIdentifier);
		table.doLayout();
	}
}
