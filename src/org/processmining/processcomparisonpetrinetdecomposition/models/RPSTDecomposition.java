package org.processmining.processcomparisonpetrinetdecomposition.models;

import java.util.ArrayList;

import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPST;
import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPSTNode;

public class RPSTDecomposition {
	final private ArrayList<PetriNetRPSTNode> decomposition;
	final private PetriNetRPST rpst;
	final private DecompositionInfoProvider info;
	

	public RPSTDecomposition(ArrayList<PetriNetRPSTNode> decomposition, PetriNetRPST rpst, DecompositionInfoProvider info) {
		this.decomposition = decomposition;
		this.rpst = rpst;
		this.info = info;
	}
	
//	public boolean isValid() {
//		// Decomposition is valid if all fragments (sets of arcs) are disjoint and all arcs are in at least one fragment 
//		for (PetriNetRPSTNode fragment_a : decomposition) {
//			Set<Arc> arcs_a = fragment_a.getArcs();
//			for (PetriNetRPSTNode fragment_b : decomposition) {
//				if (!fragment_a.equals(fragment_b)) {
//					Set<Arc> arcs_b = fragment_b.getArcs();
//					if (!Collections.disjoint(arcs_a, arcs_b)) {
//						return false;
//					}
//				}
//			}
//		}
//		return true;
//	}
	
	public ArrayList<PetriNetRPSTNode> zoomIn(PetriNetRPSTNode node, boolean inPlace) {
		return null;
	}
	
	public ArrayList<PetriNetRPSTNode> zoomOut(PetriNetRPSTNode node, boolean inPlace) {
		return null;
	} 
	
	public ArrayList<PetriNetRPSTNode> getDecomposition() {
		return decomposition;
	}

	public PetriNetRPST getRpst() {
		return rpst;
	}
	
	public DecompositionInfoProvider getInfo() {
		return info;
	}
}
