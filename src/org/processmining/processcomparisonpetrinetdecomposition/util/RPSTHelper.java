package org.processmining.processcomparisonpetrinetdecomposition.util;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.extension.std.XExtendedEvent;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.plugins.ConvertPetriNetToAcceptingPetriNetPlugin;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPST;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPSTNode;
import org.processmining.acceptingpetrinetdecomposer.parameters.sese.GenerateRPSTFromPetriNetParameters;
import org.processmining.acceptingpetrinetdecomposer.plugins.sese.GenerateRPSTFromPetriNetPlugin;
import org.processmining.processcomparisonpetrinetdecomposition.measurements.Measurement;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.log.utils.XLogBuilder;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.directed.DirectedGraphElement;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithILP;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.PNLogReplayer;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import edu.uci.ics.jung.graph.DirectedGraph;
import nl.tue.astar.AStarException;

public class RPSTHelper {
	
	/**
	 * Return the parent of the given fragment in the given RPST. Returns null if the node is the root. 
	 * @param node Node whose parent should be returned.
	 * @param rpst RPST containing the node.
	 * @return Parent fragment of node or null if node is the root.
	 */
	public static PetriNetRPSTNode getParent(PetriNetRPSTNode node, PetriNetRPST rpst) {
		Iterator<PetriNetRPSTNode> parent_iter = rpst.getTree().getPredecessors(node).iterator();
		return parent_iter.hasNext() ? parent_iter.next() : null;
	}
	
	/**
	 * Creates a name for the given fragment. If it is a inner node, it receives the name of the node object. 
	 * If it is a leaf, the name is the name is of the form "[entry-node]->[exit-node]". 
	 * @param node Fragment for which the name should be generated.
	 * @param rpst RPST the node belongs to. Used to check if the node is a leaf or an inner node.
	 * @return String name of the node.
	 */
	public static String createNodeName(PetriNetRPSTNode node, PetriNetRPST rpst) {
		Collection<PetriNetRPSTNode> children = rpst.getChildren(node);
		return children.isEmpty() ? node.getEntry() + "\n->\n" + node.getExit() : node.toString();
	}
	
	/**
	 * Creates fragment net for the given SESE-fragment, which contains all arcs from the fragment and adds an initial/ final place
	 * if the entry/ exit node of the fragments is not a place.
	 * 
	 * @param node SESE-fragment.
	 * @return Triple of a Petri net, an initial marking and a final marking.
	 */
	public static Triple<Petrinet,Marking,Marking> fragmentToNetWithMarking(/*PluginContext context, */PetriNetRPSTNode node) {
		Map<DirectedGraphElement, DirectedGraphElement> map = new HashMap<>();
	
		Petrinet net = PetrinetFactory.newPetrinet(node.getName());	
		for (Transition origT: node.getTrans()){
			Transition newT = net.addTransition(origT.getLabel());
			newT.setInvisible(origT.isInvisible());
			map.put(origT, newT);
		}
		for(Place origP: node.getPlaces()){
			Place newP = net.addPlace(origP.getLabel());
			map.put(origP, newP);
		}
		for(Arc origA: node.getArcs()){
			PetrinetNode source = (PetrinetNode) map.get(origA.getSource());
			PetrinetNode target = (PetrinetNode) map.get(origA.getTarget());
			if(source instanceof Transition){
				Arc newA = net.addArc((Transition)source, (Place)target, origA.getWeight());
				map.put(origA, newA);
			}
			else if (source instanceof Place){
				Arc newA = net.addArc((Place) source, (Transition) target, origA.getWeight());
				map.put(origA, newA);
			}
		}
		
		// add start and end places 
		Place netStart;
		if (node.getEntry() instanceof Transition) { // add initial place
			netStart = net.addPlace("start");
			Arc newStartArc = net.addArc(netStart, (Transition)map.get(node.getEntry()));
		}
		else {
			netStart = (Place)map.get(node.getEntry());
		}
		Place netEnd;
		if (node.getExit() instanceof Transition) { // add initial place
			netEnd = net.addPlace("end");
			Arc newEndArc = net.addArc((Transition)map.get(node.getExit()), netEnd);
		}
		else {
			netEnd = (Place)map.get(node.getExit());
		}
		
		// create initial and final marking
		
		Marking final_marking = new Marking();
		final_marking.add(netEnd);
		//context.addConnection(new FinalMarkingConnection(net, final_marking));
		
		Marking initial_marking = new Marking();
		initial_marking.add(netStart);
		//context.addConnection(new InitialMarkingConnection(net, initial_marking));

		
		GraphLayoutConnection layout = new GraphLayoutConnection(net); 
		//context.addConnection(layout);
		return new ImmutableTriple<Petrinet, Marking, Marking>(net, initial_marking, final_marking);
	}
	
	/**
	 * Calculates the shortest path through the fragment net for the given SESE-fragment
	 * 
	 * @param fragment SESE-fragment.
	 * @return double value of the alignment cost of an empty trace on the fragment net.
	 */
	public static double shortestPathThroughModelLength(PetriNetRPSTNode fragment) {
		Triple<Petrinet, Marking, Marking> net_and_markings = RPSTHelper.fragmentToNetWithMarking(fragment);
		Petrinet cur_net = net_and_markings.getLeft();
		Marking initial_marking = net_and_markings.getMiddle();
		Marking final_marking = net_and_markings.getRight();
		return shortestPathThroughModelLength(cur_net, initial_marking, final_marking);
	}
	
	/**
	 * Calculates the shortest path through the given Petri net
	 * 
	 * @param net Petri net.
	 * @param initial_marking Initial marking of the given Petri net.
	 * @param final_marking Final marking of the given Petri net.
	 * @return double value of the alignment cost of an empty trace on the Petri net.
	 */
	public static double shortestPathThroughModelLength(Petrinet net, Marking initial_marking, Marking final_marking) {
		// create empty dummy log
		XLogBuilder logBuilder = XLogBuilder.newInstance().startLog("Log left");
		logBuilder.addTrace("L1");
		XLog log = logBuilder.build();
		
		// align log to net
		PNRepResult alignment = null;
		try {
			alignment = executeAlignments(log, net, initial_marking, final_marking);
		} catch (AStarException e) {
			// TODO Auto-generated catch block
		}
		
		double fitness = (double) alignment.getInfo().get(PNRepResult.RAWFITNESSCOST);
		return fitness;
	}
	
	/**
	 * Manually calculates the alignment between the given log and model.
	 * 
	 * @param log Event log that should be aligned.
	 * @param net Petri net the log should be aligned with.
	 * @param initialMarking Initial marking of the Petri net.
	 * @param finalMarking Final marking of the Petri net.
	 * @return PNRepResult containing the resulting alignment for every trace in the log.
	 * @throws AStarException
	 */
	public static PNRepResult executeAlignments(XLog log, PetrinetGraph net,Marking initialMarking, Marking finalMarking) throws AStarException {
			XEventClass evClassDummy = new XEventClass("DUMMY", -1);
			TransEvClassMapping mapping = new
			TransEvClassMapping(XLogInfoImpl.STANDARD_CLASSIFIER, evClassDummy);
			XLogInfo logInfo = XLogInfoFactory.createLogInfo(log);
			for (XEventClass ec : logInfo.getEventClasses().getClasses()) {
				for (Transition t : net.getTransitions()) {
					if (t.getLabel().equals(ec.toString().substring(0,ec.toString().length()-1))) {
						mapping.put(t, ec);
					}
				}
			}
			CostBasedCompleteParam parameter = 
					new CostBasedCompleteParam(logInfo.getEventClasses().getClasses(),evClassDummy, net.getTransitions(), 1, 1);
			parameter.setGUIMode(false);
			parameter.setCreateConn(false);
			parameter.setInitialMarking(initialMarking);
			parameter.setFinalMarkings(finalMarking);
			parameter.setMaxNumOfStates(200000);
			PluginContext context = null;
			PetrinetReplayerWithILP replWithoutILP = new PetrinetReplayerWithILP();
			PNLogReplayer replayer = new PNLogReplayer();
			PNRepResult pnRepResult = replayer.replayLog(context, net, log, mapping, replWithoutILP, parameter);
			return pnRepResult;
	}
	
	/**
	 * Returns the set of nodes that have an arc to the given node inside the given Petri net.
	 * 
	 * @param net Petri net that contains the node.
	 * @param node Node for which predecessors should be searched.
	 * @return Set of predecessors of the given node.
	 */
	public static Set<PetrinetNode> getPredecessors(Petrinet net, PetrinetNode node) {
		HashSet<PetrinetNode> predecessors = new HashSet<>();
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getInEdges(node)) {
			predecessors.add(edge.getSource());
		}
		return predecessors;
	}
	
	/**
	 * Returns the set of nodes to which the given node has an arc inside the given Petri net.
	 * 
	 * @param net Petri net that contains the node.
	 * @param node Node for which successors should be searched.
	 * @return Set of successors of the given node.
	 */
	public static Set<PetrinetNode> getSuccessors(Petrinet net, PetrinetNode node) {
		HashSet<PetrinetNode> successors = new HashSet<>();
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getOutEdges(node)) {
			successors.add(edge.getTarget());
		}	
		return successors;
	}
	
	/**
	 * Calculates the number of the that would be added to the given fragment when executing the given transition
	 * inside the given Petri net. Does not consider the tokens the transition might consume from the fragment.
	 * 
	 * @param frag Fragment of net.
	 * @param net Petri net containing the fragment and the transition.
	 * @param trans Transition for which the number of tokens should be calculated.
	 * @return int value of the number of tokens.
	 */
	public static int fragTokenAdded(PetriNetRPSTNode frag, Petrinet net, Transition trans) {
		Set<PetrinetNode> successorsInFrag = getSuccessors(net, trans);
		successorsInFrag.retainAll(frag.getPlaces());
		return successorsInFrag.size();
	}
	
	/**
	 * Calculates the number of the that would be consumed from the given fragment when executing the given transition
	 * inside the given Petri net. Does not consider the tokens the transition might add to the fragment.
	 * 
	 * @param frag Fragment of net.
	 * @param net Petri net containing the fragment and the transition.
	 * @param trans Transition for which the number of tokens should be calculated.
	 * @return int value of the number of tokens.
	 */
	public static int fragTokenConsumed(PetriNetRPSTNode frag, Petrinet net, Transition trans) {
		Set<PetrinetNode> predecessorsInFrag = getPredecessors(net, trans);
		predecessorsInFrag.retainAll(frag.getPlaces());
		return predecessorsInFrag.size();
	}
	
	/**
	 * Calculates the change in the number of tokens inside the given fragment when executing the given transition,
	 * i.e. the differences of the number of tokens added to the fragment by the transition and the number of tokens
	 * the transition consumes from the fragment.
	 * 
	 * @param frag Fragment of net.
	 * @param net Petri net containing the fragment and the transition.
	 * @param trans Transition for which the number of tokens should be calculated.
	 * @return int value of the number of tokens.
	 */
	public static int fragTokenDifference(PetriNetRPSTNode frag, Petrinet net, Transition trans) {
		int added = fragTokenAdded(frag, net, trans);
		int consumed = fragTokenConsumed(frag, net, trans);
		return added - consumed;
	}
	
	/**
	 * Returns true if the inital place of the Petri net is inside the fragment.
	 * @param fragment Considered fragment.
	 * @param net Considered Petri net.
	 * @return True if initial place is in fragment, else false.
	 */
	public static boolean isInitialPlaceInFragment(PetriNetRPSTNode fragment, Petrinet net) {
		for (Place place : fragment.getPlaces()) {
			if (RPSTHelper.getPredecessors(net, place).size() == 0) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if the final place of the Petri net is inside the fragment.
	 * @param fragment Considered fragment.
	 * @param net Considered Petri net.
	 * @return True if final place is in fragment, else false.
	 */
	public static boolean isFinalPlaceInFragment(PetriNetRPSTNode fragment, Petrinet net) {
		for (Place place : fragment.getPlaces()) {
			if (RPSTHelper.getSuccessors(net, place).size() == 0) {
				return true;
			}
		}
		return false;
	}
	// ------------------ functions for creating RPST -------------------------
	/**
	 * Generates an RPST for the given Petri net using the GenerateRPSTFromPetriNetPlugin from the acceptingpetrinetdecomposer package.
	 *
	 * @param context Plugin context
	 * @param net Petri net for which the RPST is created
	 * @param ignore_long_term if true, long-term dependencies are filtered out when creating the RPST
	 * @param log1 Event log used to calculate sojourn times of places, to determine places for removal. CANNOT BE NULL!
	 * @param alignments1 Alignments used to calculate sojourn times of places, to determine places for removal. CANNOT BE NULL!
	 * @param log2 Second event log used to calculate sojourn times of places, to determine places for removal. Can be null!
	 * @param alignments2 Second alignments used to calculate sojourn times of places, to determine places for removal. Can be null!
	 * @return RPST of the given Petri net
	 */
	public static PetriNetRPST generateRPST(PluginContext context, Petrinet net, boolean ignore_long_term, XLog log1, PNRepResult alignments1, XLog log2, PNRepResult alignments2) {
		GenerateRPSTFromPetriNetParameters parameters = new GenerateRPSTFromPetriNetParameters();
		parameters.setTryConnections(false);
		
		if (!ignore_long_term) {
			AcceptingPetriNet accepting_net = (new ConvertPetriNetToAcceptingPetriNetPlugin()).runDefault(context, net);
			PetriNetRPST rpst = (new GenerateRPSTFromPetriNetPlugin()).run(context, accepting_net, parameters);
			return rpst;
		}
		else {
			return rpstWithoutLongterm(context, net, log1, alignments1, log2, alignments2);
		}
		
//		int no_inner_nodes = countInnerNodes(rpst);  // baseline
//		Collection<Triple<String, Collection<PetrinetNode>,	Collection<PetrinetNode>>> removed_places = new ArrayList<>();
//		Collection<Place> original_places = new ArrayList<>(net.getPlaces());
//		for (Place place : original_places) {
//			if(canRemove(net, place)) {
//				Collection<PetrinetNode> predecessors = RPSTHelper.getPredecessors(net, place);
//				Collection<PetrinetNode> successors = RPSTHelper.getSuccessors(net, place);
//				String label = place.getLabel();
//				
//				net.removePlace(place);
//				AcceptingPetriNet temp_accepting_net = (new ConvertPetriNetToAcceptingPetriNetPlugin()).runDefault(context, net);
//				PetriNetRPST temp_rpst = (new GenerateRPSTFromPetriNetPlugin()).run(context, temp_accepting_net, parameters);
//				int temp_no_inner_nodes = countInnerNodes(temp_rpst);
//				System.out.println("place: " + label + ", " + temp_no_inner_nodes  + ", " + no_inner_nodes);
//				if (temp_no_inner_nodes > no_inner_nodes) {
//					no_inner_nodes = temp_no_inner_nodes;
//					rpst = temp_rpst;
//					Triple<String, Collection<PetrinetNode>, Collection<PetrinetNode>> removed_place = new ImmutableTriple<>(label, predecessors, successors);
//					removed_places.add(removed_place);
//				}
//				else {
//					addPlace(net, label, predecessors, successors);
//				}
//			}
//		}
//		
//		// add back removed places and add related fragments
//		for (Triple<String, Collection<PetrinetNode>, Collection<PetrinetNode>> triple : removed_places) {
//			Place new_place = addPlace(net, triple.getLeft(), triple.getMiddle(), triple.getRight());
//			// add fragments
//			createFragmentsForLongtermPlace(rpst, new_place);			
//		}
//		
//		// disable already created layout of the Petri net to force the creation of a new layout to properly handle the places that were removed and were now added back
//		try {
//			GraphLayoutConnection connection = context.getConnectionManager().getFirstConnection(GraphLayoutConnection.class, context, net);
//			connection.setLayedOut(false);
//		} catch (ConnectionCannotBeObtained e) {
//			// noop, since our goal would be to deactivate the connection
//		}
//		return rpst;
	}
	
	private static PetriNetRPST rpstWithoutLongterm(PluginContext context, Petrinet net, XLog log1, PNRepResult alignments1, XLog log2, PNRepResult alignments2) {
		GenerateRPSTFromPetriNetParameters parameters = new GenerateRPSTFromPetriNetParameters();
		parameters.setTryConnections(false);
						
		Map<Place, Duration> sojourn_times = getAverageSojournTimes(net, log1, alignments1, log2, alignments2);
		List<Place> places = new ArrayList<>(net.getPlaces());
		if (sojourn_times != null) {
			places.sort(new Comparator<Place>() {
				public int compare(Place o1, Place o2) {
					return sojourn_times.get(o1).compareTo(sojourn_times.get(o2)); 
				}
			});
		}
		Collection<Triple<String, Collection<PetrinetNode>,	Collection<PetrinetNode>>> removed_places = new ArrayList<>();
		ListIterator<Place> iterator = places.listIterator(places.size());
		while(iterator.hasPrevious()) {
			Place place = iterator.previous();
			
			//TODO finish
			
			// test if place is removable
			if (canRemove(net, place)) {
			// if yes: remove, else: continue
				Collection<PetrinetNode> predecessors = RPSTHelper.getPredecessors(net, place);
				Collection<PetrinetNode> successors = RPSTHelper.getSuccessors(net, place);
				String label = place.getLabel();
				net.removePlace(place);
				Triple<String, Collection<PetrinetNode>, Collection<PetrinetNode>> removed_place = new ImmutableTriple<>(label, predecessors, successors);
				removed_places.add(removed_place);
				// maybe check rpst quality
			}
		}
		
		AcceptingPetriNet accepting_net = (new ConvertPetriNetToAcceptingPetriNetPlugin()).runDefault(context, net);
		PetriNetRPST rpst = (new GenerateRPSTFromPetriNetPlugin()).run(context, accepting_net, parameters);
		
		// add back removed places and add related fragments
		for (Triple<String, Collection<PetrinetNode>, Collection<PetrinetNode>> triple : removed_places) {
			Place new_place = addPlace(net, triple.getLeft(), triple.getMiddle(), triple.getRight());
			// add fragments
			createFragmentsForLongtermPlace(rpst, new_place);			
		}
		
		// disable already created layout of the Petri net to force the creation of a new layout to properly handle the places that were removed and were now added back
		try {
			GraphLayoutConnection connection = context.getConnectionManager().getFirstConnection(GraphLayoutConnection.class, context, net);
			connection.setLayedOut(false);
		} catch (ConnectionCannotBeObtained e) {
			// noop, since our goal would be to deactivate the connection
		}
		return rpst;
	}
	
	private static void createFragmentsForLongtermPlace(PetriNetRPST rpst, Place place) {
		String base_id = place.getId().toString();
		// create 3 fragments: one containing both edges adjacent to the place and the two trivial fragments for the two edges as children
		Petrinet net = rpst.getNet().getNet();
		PetriNetRPSTNode root = rpst.getRoot();
		DirectedGraph<PetriNetRPSTNode, String> tree = rpst.getTree();
		
		////////////////////////////////////////
		// Create Parent Fragment
		// - contains all adjacent transitions
		////////////////////////////////////////
		String id = base_id;
		String name = "Non-skeletal " + base_id;
		// Arcs
		Set<Arc> arcs = new HashSet<>();
		net.getInEdges(place).stream().map(e -> (Arc) e).forEach(a -> arcs.add(a));
		net.getOutEdges(place).stream().map(e -> (Arc) e).forEach(a -> arcs.add(a));
		// Places
		Set<Place> places = new HashSet<>();
		places.add(place);
		// Transitions
		// In
		Set<Transition> transitions = new HashSet<>();
		Set<Transition> transitionsIn = net.getInEdges(place).stream()
			.map(PetrinetEdge::getSource)
			.filter(t -> t instanceof Transition)
			.map(t -> (Transition) t)
			.collect(Collectors.toSet());
		transitions.addAll(transitionsIn);
		// Out
		Set<Transition> transitionsOut = net.getOutEdges(place).stream()
			.map(PetrinetEdge::getTarget)
			.filter(t -> t instanceof Transition)
			.map(t -> (Transition) t)
			.collect(Collectors.toSet());
		transitions.addAll(transitionsOut);
		// TODO Proper Handling of entry and exit nodes
		// In this tool, the entry and exit nodes are used to compute the shortest path through the model to compute a fragments fitness
		// As an easy hack, select silent transitions for the entry and exit node 
		// Otherwise, pick a random transition
		Optional<Transition> transEntry = transitionsIn.stream().filter(Transition::isInvisible).findFirst();
		Transition entry = transEntry.orElse(transitionsIn.stream().findAny().get()); // Will raise an error if there is no ingoing transition
		Optional<Transition> transExit = transitionsOut.stream().filter(Transition::isInvisible).findFirst();
		Transition exit = transExit.orElse(transitionsOut.stream().findAny().get()); // Will raise an error if there is no outgoing transition
		PetriNetRPSTNode parent_node = new PetriNetRPSTNode(id, name, name, transitions, places, arcs, entry, exit);
		tree.addVertex(parent_node);
		tree.addEdge(root.getId() + "=>" + parent_node.getId(),root, parent_node);

		//////////////////////////////
		// Add Children
		// Each adjacent edge will become a child
		//////////////////////////////
		// In
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e : net.getInEdges(place)) {
			Arc a = (Arc) e;
			addSingleEdgeFragment(parent_node, tree, a);
		}
		// Out
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e : net.getOutEdges(place)) {
			Arc a = (Arc) e;
			addSingleEdgeFragment(parent_node, tree, a);
		}
	}
	
	public static void addSingleEdgeFragment(PetriNetRPSTNode parent, DirectedGraph<PetriNetRPSTNode, String> tree, 
			Arc edge) {
		
		String id = edge.getSource().getId().toString() + "--" + edge.getTarget().getId().toString();
		String fragName = edge.getSource().getLabel() + " -> " + edge.getTarget().getLabel();
		String fragDesc = fragName;
		
		// Arcs
		Set<Arc> arcs = new HashSet<>();
		arcs.add(edge);
		// Places
		Set<Place> places = new HashSet<>();
		if (edge.getSource() instanceof Place) {
			places.add((Place) edge.getSource());
		}
		if (edge.getTarget() instanceof Place) {
			places.add((Place) edge.getTarget());
		}
		// Transitions
		Set<Transition> transitions = new HashSet<>();
		if (edge.getSource() instanceof Transition) {
			transitions.add((Transition) edge.getSource());
		}
		if (edge.getTarget() instanceof Transition) {
			transitions.add((Transition) edge.getTarget());
		}
		PetriNetRPSTNode rpstNode = new PetriNetRPSTNode(id, fragName, fragDesc, transitions, places, arcs, 
				edge.getSource(), edge.getTarget());
		tree.addVertex(rpstNode);
		tree.addEdge(parent.getId() + "=>" + rpstNode.getId(), parent, rpstNode);
	}

	
	/**
	 * Adds the 3 fragments for the given place that belongs to a long term dependency to the given RPST. 
	 * One fragment that contains both arcs connected to the fragment and two trivial fragments for the two arcs.
	 * 
	 * @param rpst RPST the fragments should be added to
	 * @param place Place for which the fragments should be added
	 */
	private static void createFragmentsForLongtermPlaceOld(PetriNetRPST rpst, Place place) {
		String base_id = place.getId().toString();
		// create 3 fragments: one containing both edges adjacent to the place and the two trivial fragments for the two edges as children
		Petrinet net = rpst.getNet().getNet();
		PetriNetRPSTNode root = rpst.getRoot();
		DirectedGraph<PetriNetRPSTNode, String> tree = rpst.getTree();
		Transition pred = (Transition)RPSTHelper.getPredecessors(net, place).iterator().next();
		Transition succ = (Transition)RPSTHelper.getSuccessors(net, place).iterator().next();
		// add parent fragment
		String id = base_id;
		String name = "longterm place " + base_id;
		String desc = name;
		Set<Arc> arcs = new HashSet<>();
		arcs.add(net.getArc(pred,place));
		arcs.add(net.getArc(place, succ));
		Set<Place> places = new HashSet<>();
		places.add(place);
		Set<Transition> transitions = new HashSet<>();
		transitions.add(pred);
		transitions.add(succ);
		PetrinetNode entry = pred;
		PetrinetNode exit = succ;
		PetriNetRPSTNode parent_node = new PetriNetRPSTNode(id, name, desc, transitions, places, arcs, entry, exit);
		tree.addVertex(parent_node);
		tree.addEdge(root.getId() + "=>" + parent_node.getId(),root, parent_node);
		// left child
		id = base_id + "_left";
		name = "longterm place " + id;
		arcs = new HashSet<>();
		arcs.add(net.getArc(pred,place));
		places = new HashSet<>();
		places.add(place);
		transitions = new HashSet<>();
		transitions.add(pred);
		entry = pred;
		exit = place;
		PetriNetRPSTNode left_child_node = new PetriNetRPSTNode(id, name, desc, transitions, places, arcs, entry, exit);
		tree.addVertex(left_child_node);
		tree.addEdge(parent_node.getId() + "=>" + left_child_node.getId(), parent_node, left_child_node);
		// right child		
		id = base_id + "_right";
		name = "longterm place " + id;
		arcs = new HashSet<>();
		arcs.add(net.getArc(place, succ));
		places = new HashSet<>();
		places.add(place);
		transitions = new HashSet<>();
		transitions.add(succ);
		entry = place;
		exit = succ;
		PetriNetRPSTNode right_child_node = new PetriNetRPSTNode(id, name, desc, transitions, places, arcs, entry, exit);
		tree.addVertex(right_child_node);
		tree.addEdge(parent_node.getId() + "=>" + right_child_node.getId(), parent_node, right_child_node);
	}
	
	/**
	 * Adds a new place with the given label to the Petri net and creates arcs from the predecessors to the new place and from the place to all successors.
	 * 
	 * @param net Petri net to which the place should be added.
	 * @param label Label of the new place.
	 * @param predecessors Collection of predecessors of the new place.
	 * @param successors Collection of successors of the new place.
	 * @return Pointer to the created place.
	 */
	public static Place addPlace(Petrinet net, String label, Collection<PetrinetNode> predecessors, Collection<PetrinetNode> successors) {
		Place new_place = net.addPlace(label);
		for (PetrinetNode predecessor : predecessors) {
			net.addArc((Transition)predecessor, new_place);
		}
		for (PetrinetNode successor : successors) {
			net.addArc(new_place, (Transition)successor);
		}
		return new_place;
	}
	
	/**
	 * Checks if the given place can be removed from the given Petri net without creating transitions that do not 
	 * have at least one in-going and one out-going arc.
	 * @param net Petri net from which the place should be removed.
	 * @param place Place that should be removed.
	 * @return True if the place can be removed, else false.
	 */
	private static boolean canRemove(Petrinet net, Place place) {
		boolean has_predecessor = false;
		for (PetrinetNode predecessor : RPSTHelper.getPredecessors(net, place)) {
			has_predecessor = true;
			if (RPSTHelper.getSuccessors(net, predecessor).size() == 1) {
				return false;
			}
		}
		if (!has_predecessor) return false;
		boolean has_successor = false;
		for (PetrinetNode successor : RPSTHelper.getSuccessors(net, place)) {
			has_successor = true;
			if (RPSTHelper.getPredecessors(net, successor).size() == 1) {
				return false;
			}
		}
		if (!has_successor) return false;
		return true;
	}
	
	/**
	 * Counts the number of inner nodes in the given RPST.
	 * 
	 * @param rpst RPST.
	 * @return int value of number of inner nodes in RPST.
	 */
	private static int countInnerNodes(PetriNetRPST rpst) {
		int no_inner_nodes = 0;
		
		for (PetriNetRPSTNode node : rpst.getNodes()) {
			if (rpst.getChildren(node).size() > 0) {
				no_inner_nodes += 1;
			}
		}
		
		return no_inner_nodes;
	}
	
	// ------------------------------ Event timestamps ---------------------------------------------
	public static Date copy_timestamp(Date timestamp) {
		return new Date(timestamp.getTime());
	}
	
	public static Date get_event_timestamp(XEvent event) {
		return copy_timestamp(XExtendedEvent.wrap(event).getTimestamp());
	}
	
	// ------------------------ Calculate sojourn times ------------------------
	/**
	 * 
	 * @param net
	 * @param log1
	 * @param alignments1
	 * @param log2
	 * @param alignments2
	 * @return Might return null, if no place if no synchronous moves occur
	 */
	public static Map<Place, Duration> getAverageSojournTimes(Petrinet net, XLog log1, PNRepResult alignments1, XLog log2, PNRepResult alignments2) {
		Map<Place, Duration> sojourn_times = new HashMap<>();
		
		for (Place place : net.getPlaces()) {
			Duration sum = Duration.ZERO;
			int occurrences = 0;
			for (SyncReplayResult alignment : alignments1) {
				for (Integer trace_idx : alignment.getTraceIndex()) {
					List<Duration> durations = getTraceSojournTimes(net, log1.get(trace_idx), alignment, place);
					occurrences += durations.size();
					for (Duration duration : durations) {
						sum = sum.plus(duration);
					}
				}
			}
			if (log2 != null && alignments2 != null) {
				for (SyncReplayResult alignment : alignments2) {
					for (Integer trace_idx : alignment.getTraceIndex()) {
						List<Duration> durations = getTraceSojournTimes(net, log2.get(trace_idx), alignment, place);
						occurrences += durations.size();
						for (Duration duration : durations) {
							sum = sum.plus(duration);
						}
					}
				}
			}
			Duration average;
			if (occurrences > 0) {
				average = sum.dividedBy(occurrences);
			}
			else { 
				average = null;
			}
			sojourn_times.put(place, average);
		}
		// all places with sojourn time of null, never occur, hence they are the first that should be considered for deletion. Therefore, we set the sojourn time for those places to be slightly larger than the maximum sojourn time.
		Duration max_time = null;
		for (Duration time : sojourn_times.values()) {
			if (max_time == null || max_time.compareTo(time) < 0) {
				max_time = time;
			}
		}
		System.out.println("Max time: " + max_time);
		if (max_time == null) {
			return null;
		}
		
		max_time = max_time.plusHours(1);
		for (Place key : sojourn_times.keySet()) {
			if (sojourn_times.get(key) == null) {
				sojourn_times.replace(key, max_time);
			}
		}
		
		return sojourn_times;
	}
	
	private static List<Duration> getTraceSojournTimes(Petrinet net, XTrace trace, SyncReplayResult alignment, Place place) {
		Set<Place> place_set = new HashSet<>();
		List<Duration> durations = new ArrayList<>();
		place_set.add(place);
		AlignmentReplayer replayer = new AlignmentReplayer(net, trace, alignment, new PetriNetRPSTNode(null, null, null, null, place_set, null, place, place));
		if (replayer.isInFragment()) { // place is start place, i.e. no sojourn time
			return durations;
		}
		while (replayer.hasNext()) {
			replayer.next();
			boolean tracking = false;
			Date entry = new Date();
			
			if (!tracking) {
				if (replayer.hasEntered() && replayer.curStepType() == Measurement.SYNC_MV) {
					tracking = true;
					entry = get_event_timestamp(replayer.curLogStep());
				}
			}
			else if (replayer.hasExited() && replayer.curStepType() == Measurement.SYNC_MV) {
				tracking = false;
				Date exit = get_event_timestamp(replayer.curLogStep());
				durations.add(Duration.between(entry.toInstant(), exit.toInstant()));
			}
		}
		
		return durations;
	}
}
