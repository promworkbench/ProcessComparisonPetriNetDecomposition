package org.processmining.processcomparisonpetrinetdecomposition.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour
.xes.model.XTrace;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPST;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPSTNode;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.Measurement;
import org.processmining.processcomparisonpetrinetdecomposition.util.AlignmentReplayer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public class AttributesAnalysisAlgorithm {
	private final String column_key = "Values";
	
	public AttributesAnalysisAlgorithm() {
		
	}
	
	/**
	 * Creates a map that maps each fragment to a map which measures the occurrences of different values of the given 
	 * attribute inside the fragment. This map maps the each String attribute value occurring in synchronous events of
	 * the fragment to the number of every synchronous events belonging to the fragment that have this value.
	 *   
	 * @param context Plugin context.
	 * @param rpst RPST specifying the decomposition.
	 * @param alignments Alignment of the given event log.
	 * @param log Considered event log.
	 * @param attribute Name of the attribute including prefixes, i.e. "org:resource", "concept:name".
	 * @return Map with fragments as keys and maps, which map attribute values to their number of occurrences, as values.
	 */
	public Map<PetriNetRPSTNode, Map<String, Integer>> apply(PluginContext context, PetriNetRPST rpst, PNRepResult alignments, XLog log, String attribute) {
		Map<PetriNetRPSTNode, Map<String, Integer>> fragment_attributes_map = new HashMap<>();
		for (PetriNetRPSTNode fragment : rpst.getNodes()) {
			Map<String, Integer> value_map = attributesPerFragment(fragment, log, alignments, rpst.getNet().getNet(), attribute);
			fragment_attributes_map.put(fragment, value_map);
		}
		return fragment_attributes_map;
	}
	
	public CategoryDataset createCategoryDataset(Map<String, Integer> data) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();	
		for (Entry<String, Integer> entry : data.entrySet()) {
			dataset.addValue(entry.getValue(), entry.getKey(), column_key);
		}
		
		return dataset;
	}
	
	/*
	 * Currently only considers synchronous events, yet asynchronous events might be relevant as well.
	 */
	private Map<String, Integer> attributesPerFragment(PetriNetRPSTNode fragment, XLog log, PNRepResult alignments, Petrinet net, String attribute) {
		Map<String, Integer> values = new HashMap<>();
		for (SyncReplayResult alignment : alignments) {
			for (int trace_idx : alignment.getTraceIndex()) {
				AlignmentReplayer replayer = new AlignmentReplayer(net, log.get(trace_idx), alignment, fragment);
				while (replayer.hasNext()) {
					replayer.next();
					if (replayer.curStepType() == Measurement.SYNC_MV && fragment.getTrans().contains(replayer.curModelStep())) {
						if (replayer.curLogStep().getAttributes().keySet().contains(attribute)) {
							String value = replayer.curLogStep().getAttributes().get(attribute).toString();
							incrementKey(values, value);
						}
					}
				}
			}
		}
		return values;
	}
	
	/**
	 * If the key is already in the map increase its value by 1. Else add it to the map and set its value to 1.
	 * @param map Map is changed in-place.
	 * @param key Key whose value should be incremented.
	 */
	private void incrementKey(Map<String, Integer> map, String key) {
		if (map.containsKey(key)) {
			map.put(key, map.get(key) + 1);
		}
		else {
			map.put(key, 1);
		}
	}

	/**
	 * Get the names of all event attributes from both event logs.
	 * @param log1
	 * @param log2
	 * @return Object array in which every value is a String.
	 */
	public Object[] getAttributeNames(XLog log1, XLog log2) {
		Set<Object> names = new HashSet<>();
		XLog[] logs = {log1, log2};
		
		for (XLog log : logs) {
			for (XTrace trace : log) {
				for (XEvent event : trace) {
					names.addAll(event.getAttributes().keySet());
				}
			}
		}
		
		return names.toArray();
	}

	public long countTotalEvents(XLog log) {
		long count = 0;
		for (XTrace trace : log) {
			count += trace.size();
		}
		return count;
	}
}
