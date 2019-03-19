import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Color;

public class JPanelTesting extends JPanel {
	
	JPanelTesting(int w, int h) {
		setPreferredSize(new Dimension(w, h));
		setBackground(Color.BLUE);	
	}
	
}