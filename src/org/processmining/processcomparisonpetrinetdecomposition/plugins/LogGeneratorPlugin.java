package org.processmining.processcomparisonpetrinetdecomposition.plugins;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.model.XLog;
import org.processmining.processcomparisonpetrinetdecomposition.help.YourHelp;
import org.processmining.processcomparisonpetrinetdecomposition.util.TestLogProvider;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;



@Plugin(name = "Generate Example Event Logs", parameterLabels = { }, 
returnLabels = { "Example Event Log 1", "Example Event Log 2" }, returnTypes = { XLog.class, XLog.class }, help = YourHelp.TEXT)
public class LogGeneratorPlugin {
	/**
	 * The plug-in variant that runs in any context and requires a parameters.
	 * 
	 * @param context The context to run in.
	 * @param input1 The first input.
	 * @return The output.
	 */
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Moritz Gose", email = "moritz.gose@rwth-aachen.de")
	@PluginVariant(variantLabel = "Generate Example Event Log", requiredParameterLabels = { })
	public Object[] run(PluginContext context) {
		Pair<XLog, XLog> log_pair = TestLogProvider.createArtificalLogsConditional();
		return new Object[] {log_pair.getLeft(), log_pair.getRight()};
	}
}




