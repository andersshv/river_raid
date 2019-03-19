import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Graphics;

public class CanvasTesting extends Canvas {
	
	private JFrameTesting model;
	
	public CanvasTesting(JFrameTesting model, int w, int h) {
		this.model = model;
		setPreferredSize(new Dimension(w, h));
		setBackground(Color.BLUE);
	}
	
	public void paint(Graphics g) {
		createBufferStrategy(3);
		g.setColor(Color.GREEN);
		g.drawRect(model.x, model.y, 199, 30);
	}

}