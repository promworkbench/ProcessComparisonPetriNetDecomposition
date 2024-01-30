package org.processmining.processcomparisonpetrinetdecomposition.plugins;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;

@Plugin(name = "Dot visualisation", returnLabels = { "Dot visualisation" }, returnTypes = { JComponent.class }, parameterLabels = { "Dot" }, userAccessible = true, level = PluginLevel.Regular)
@Visualizer
public class RPSTJComponentVisualizer {

	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, JComponent comp ) {

		return comp;
	}
}
