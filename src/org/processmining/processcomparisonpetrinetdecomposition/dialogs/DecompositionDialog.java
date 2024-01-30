package org.processmining.processcomparisonpetrinetdecomposition.dialogs;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.processmining.processcomparisonpetrinetdecomposition.measurements.ActivitiyOutsideFragmentMeasurement;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.ElapsedTimeMeasurement;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.FitnessMeasurement;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.FragmentActivityDurationMeasurement;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.FragmentQueueMeasurement;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.FragmentWaitTimeMeasurement;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.IndependentReachedMeasurement;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.LoopIterationMeasurement;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.Measurement;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.ReachedMeasurement;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.TimeInFragmentMeasurement;
import org.processmining.processcomparisonpetrinetdecomposition.parameters.DecompositionParameters;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.ui.widgets.ProMList;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;

import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class DecompositionDialog extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -60087716353524468L;
	private static final Measurement[] measurement_options = {new ReachedMeasurement(), 
			new ElapsedTimeMeasurement(), new IndependentReachedMeasurement(), 
			new LoopIterationMeasurement(), new TimeInFragmentMeasurement(), 
			//new HybridFragmentActivityDurationMeasurement(),
			new FragmentWaitTimeMeasurement(), new FitnessMeasurement(), 
			new ActivitiyOutsideFragmentMeasurement(), 
			new FragmentQueueMeasurement(), new FragmentActivityDurationMeasurement()};

	/**
	 * The JPanel that allows the user to set (a subset of) the parameters.
	 */
	public DecompositionDialog(UIPluginContext context, Petrinet net, final DecompositionParameters parameters) {
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL, 30, 30 } };
		setLayout(new TableLayout(size));
		Set<String> values = new HashSet<String>();
		for (Measurement measurement : measurement_options) {
			values.add(measurement.getName());
		}
		
		DefaultListModel<String> listModel = new DefaultListModel<String>();
		for (String value: values) {
			listModel.addElement(value);
		}
		final ProMList<String> list = new ProMList<String>("Select measurement", listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		final Measurement defaultMeasurement = measurement_options[0];
		final String defaultValue = defaultMeasurement.getName();
		list.setSelection(defaultValue);
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<String> selected = list.getSelectedValuesList();
				if (selected.size() == 1) {
					String selected_measurement_name = selected.get(0);
					for (Measurement option : measurement_options) {
						if (option.getName().equals(selected_measurement_name)) {
							parameters.setMeasurement(option);
						}
					}
				} else {
					/*
					 * Nothing selected. Revert to selection of default classifier.
					 */
					list.setSelection(defaultValue);
					parameters.setMeasurement(defaultMeasurement);
				}
			}
		});
		list.setPreferredSize(new Dimension(100, 100));
		add(list, "0, 0");
		
		final int MIN_VALUE = 1;
		final int MAX_VALUE = net.getEdges().size();
		
		final NiceSlider integerSilder = SlickerFactory.instance().createNiceIntegerSlider("Select minimal fragment size ", MIN_VALUE,
				MAX_VALUE, parameters.getMinFragmentSize(), Orientation.HORIZONTAL);
		integerSilder.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				parameters.setMinFragmentSize(integerSilder.getSlider().getValue());
			}
		});
		add(integerSilder, "0, 1");

		final JCheckBox checkBox = SlickerFactory.instance().createCheckBox("Ignore long-term dependencies for decomposition", true);
		checkBox.setSelected(parameters.isIgnoreLongTermDependencies());
		checkBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				parameters.setIgnoreLongTermDependencies(checkBox.isSelected());
			}

		});
		checkBox.setOpaque(false);
		checkBox.setPreferredSize(new Dimension(100, 30));
		add(checkBox, "0, 2");
	}
}
