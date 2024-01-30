package org.processmining.processcomparisonpetrinetdecomposition.visualization;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.framework.util.ui.widgets.WidgetColors;

public class ToolDescriptionPanel extends JPanel {
  
  public ToolDescriptionPanel() {
    this.setLayout(new GridLayout());
		setBackground(WidgetColors.PROPERTIES_BACKGROUND);
		
		JLabel descriptionLabel = new JLabel("<html><p>Select a vertex in the decomposition<br>"
		    + "and all adjacent nodes are highlighted.</p>"
		    + "<p>Press 'm' to collapse the selected vertex.</p></html>");
		
		this.add(descriptionLabel);
  }

}
