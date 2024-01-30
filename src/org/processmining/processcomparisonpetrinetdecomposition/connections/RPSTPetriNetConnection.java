package org.processmining.processcomparisonpetrinetdecomposition.connections;

import org.processmining.acceptingpetrinetdecomposer.models.rpst.PetriNetRPST;
import org.processmining.acceptingpetrinetdecomposer.parameters.sese.GenerateRPSTFromPetriNetParameters;
import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;

public class RPSTPetriNetConnection  extends AbstractConnection {

	public final static String NET = "Accepting Petri Net";
	public final static String RPST = "Petri Net RPST";
	
	private GenerateRPSTFromPetriNetParameters parameters;
	
	public RPSTPetriNetConnection(Petrinet net,
			PetriNetRPST rpst, GenerateRPSTFromPetriNetParameters parameters) {
		super("Generate RPST from Accepting Petri Net Connection");
		put(NET, net);
		put(RPST, rpst);
		this.parameters = parameters;
	}
	
	public GenerateRPSTFromPetriNetParameters getParameters() {
		return parameters;
	}
	
}
