import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog.ModalityType;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Draw implements MouseListener, MouseMotionListener{
	private JPanel gui = null;
	private BufferedImage img = null;
	private BufferedImage resizedImg = null;
	private JLabel imageLabel = null;
	private Color color = Color.WHITE;
	
	private int activeTool = 0;
	public static final int BRUSH_TOOL = 0;
	public static final int LINE_TOOL = 1;
    public static final int RECT_TOOL = 2;
    public static final int CIRC_TOOL = 3;
    public static final int TEXT_TOOL = 4;
    public static final int PAINT_TOOL = 5;
   
    
    private int x = 0, y = 0, x2 = 0, y2 = 0;
    private int width = 420, height = 235;
    
    private JLabel mssg = new JLabel("Hello!");
    private String  outputText = "";
    private boolean hasPainted = false;
    
    private BufferedImage colorSample = new BufferedImage(
    		16, 16, BufferedImage.TYPE_INT_RGB);
    
    final SpinnerNumberModel textModel = new SpinnerNumberModel(10, 8, 30, 2);
    final SpinnerNumberModel strokeModel = new SpinnerNumberModel(8, 1, 20, 1);
    private Stroke stroke = new BasicStroke(
    		8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    
    public Draw(){
    	try {
			UIManager.setLookAndFeel(
			        UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(gui, "Something went wrong! "
					+ "Check the console output!");
			e.printStackTrace();
		} 
    	JFrame frame = new JFrame("DoDoodle!");
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.setContentPane(this.getGui());
    	frame.setJMenuBar(this.getMenuBar());
    	frame.setLocationByPlatform(true);
    	frame.pack();
    	frame.setMinimumSize(frame.getSize());
    	frame.setVisible(true);
    }
    private JComponent getGui() {
		this.setImg(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB));
		gui = new JPanel(new BorderLayout(4,4));
		gui.setBorder(new EmptyBorder(5,3,5,3));
	
		JScrollPane imgScroll = new JScrollPane(this.getImgViewPanel());
		
		imageLabel.addMouseListener(this);
		imageLabel.addMouseMotionListener(this);
		
		gui.add(imgScroll, BorderLayout.CENTER);
		gui.add(this.getToolBar(), BorderLayout.PAGE_START);
		gui.add(this.getTools(), BorderLayout.LINE_END);
		gui.add(mssg, BorderLayout.PAGE_END);
		
		clean(colorSample);
		clean(img);
		
		return gui;
	}
    private JPanel getImgViewPanel(){
    	JPanel imgView = new JPanel(new GridBagLayout());
		imageLabel = new JLabel(new ImageIcon(img));
		imageLabel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		imageLabel.setBorder(BorderFactory.createDashedBorder(
				Color.GRAY, 5.0f, 5.0f));
		imgView.add(imageLabel);
		
		return imgView;
    }
    private JToolBar getToolBar(){
    	JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		JButton colorButton = new JButton("Color");
		colorButton.setToolTipText("Choose a color");
		
		ActionListener colorListener = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0){
				Color c = JColorChooser.showDialog(gui, "Choose a color", color);
				if(c!=null){
					setColor(c);
				}
				
			}
		};
		colorButton.addActionListener(colorListener);
		colorButton.setIcon(new ImageIcon(colorSample));
		
		toolBar.add(colorButton);
		toolBar.addSeparator();
		
		JSpinner strokeSize = new JSpinner(strokeModel);
		JLabel strokeLabel = new JLabel("Stroke");
        strokeLabel.setLabelFor(strokeSize);
        
		ChangeListener strokeListener = new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e){
				int i = (int) strokeModel.getValue();
				stroke = new BasicStroke(
						i,
						BasicStroke.CAP_ROUND, 
						BasicStroke.JOIN_ROUND, 
						1.7f);
			}
		};
		strokeSize.addChangeListener(strokeListener);
		strokeSize.setMaximumSize(strokeSize.getPreferredSize());
       
        toolBar.add(strokeLabel);
        toolBar.add(strokeSize);
        toolBar.addSeparator();
        
        JSpinner textSize = new JSpinner(textModel);
        textSize.setMaximumSize(textSize.getPreferredSize());
        JLabel textLabel = new JLabel("Text Size");
        textLabel.setLabelFor(textSize);
        
        toolBar.add(textLabel);
        toolBar.add(textSize);
        toolBar.addSeparator();
       
		JButton clearButton = new JButton("Clear");
		
		 ActionListener clearListener = new ActionListener(){
	        	@Override
				public void actionPerformed(ActionEvent arg0){
					int result = JOptionPane.YES_NO_OPTION;
					result = JOptionPane.showConfirmDialog(gui, 
							"Erase the painting?");
					if(result == JOptionPane.YES_OPTION){
						clear();
					}
				}
			};
		clearButton.addActionListener(clearListener);
		
		toolBar.add(clearButton);
		
		return toolBar;
    }
    private JToolBar getTools(){
    	JToolBar tools = new JToolBar(JToolBar.VERTICAL);
		tools.setFloatable(false);
		
		final JRadioButton brush = new JRadioButton("Brush", true);
		final JRadioButton line = new JRadioButton("Line");
		final JRadioButton rect = new JRadioButton("Rectangle");
		final JRadioButton circle = new JRadioButton("Circle");
		final JRadioButton text = new JRadioButton("Text");
		final JRadioButton paint = new JRadioButton("Paint Background");
		
		tools.add(brush);
		tools.add(line);
		tools.add(rect);
		tools.add(circle);
		tools.add(text);
		tools.add(paint);
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(brush);
		bg.add(line);
		bg.add(rect);
		bg.add(circle);
		bg.add(text);
		bg.add(paint);
		
		ActionListener toolGroupListener = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae) {
				// TODO Auto-generated method stub
				if(ae.getSource() == brush){
					activeTool = BRUSH_TOOL;
				}else if(ae.getSource() == line){
					activeTool = LINE_TOOL;
				}else if(ae.getSource() == rect){
					activeTool = RECT_TOOL;
				}else if(ae.getSource() == circle){
					activeTool = CIRC_TOOL;
				}else if(ae.getSource() == text){
					activeTool = TEXT_TOOL;
				}else if(ae.getSource() == paint){
					activeTool = PAINT_TOOL;
				}
			}
		};
		brush.addActionListener(toolGroupListener);
		line.addActionListener(toolGroupListener);
		rect.addActionListener(toolGroupListener);
		circle.addActionListener(toolGroupListener);
		text.addActionListener(toolGroupListener);
		paint.addActionListener(toolGroupListener);
		
		return tools;
    }
    private void clean(BufferedImage bi){
    	int w = bi.getWidth();
    	int h = bi.getHeight();
    	
    	Graphics2D g = bi.createGraphics();
    	g.setColor(color);
    	g.fillRect(0, 0, w, h);
    	
    	g.dispose();
    	imageLabel.repaint();
    }  
    private void clear(){
    	Graphics2D g = img.createGraphics();
    	g.setColor(Color.WHITE);
    	g.fillRect(0, 0, img.getWidth(), img.getHeight());
    	
    	hasPainted = false;
    	g.dispose();
    	imageLabel.repaint();
    }
   private void setImg(BufferedImage bi){
    	int w = bi.getWidth();
    	int h = bi.getHeight();
    	
    	this.img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    	Graphics2D g = img.createGraphics();
    	
    	g.drawImage(bi, 0, 0, gui);
    	g.dispose();
    	
    	if (this.imageLabel!=null) {
             imageLabel.setIcon(new ImageIcon(img));
             this.imageLabel.repaint();
         }
         if (gui!=null) {
             gui.invalidate();
         }
    }
    private void setColor(Color c){
    	this.color = c;
    	clean(colorSample);
    }
    private JMenuBar getMenuBar(){
    	JMenuBar mb = new JMenuBar();
    	JMenu file = new JMenu("File");
    	
    	JMenuItem newImageItem = new JMenuItem("New");
    	ActionListener newImageListener = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae) {
				// TODO Auto-generated method stub
				int result = JOptionPane.YES_NO_CANCEL_OPTION;
				if(hasPainted == true){
					result = JOptionPane.showConfirmDialog(gui, 
							"Do you want to save the image first?");
					if(result == JOptionPane.YES_OPTION){
						int isTrue = saveImg();
						if(isTrue == -1){
							return;
						}
						
					}else if(result == JOptionPane.CANCEL_OPTION
							|| result == JOptionPane.CLOSED_OPTION){
						return;
					}
				}
				clear();
			}
    	};
    	newImageItem.addActionListener(newImageListener);
    	
    	file.add(newImageItem);
    	file.addSeparator();
    	
    	JMenuItem openImageItem = new JMenuItem("Open");
    	ActionListener openListener = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				int result = JOptionPane.YES_NO_CANCEL_OPTION;
				if(hasPainted == true){
					result = JOptionPane.showConfirmDialog(gui,
							"Do you want to save the image first?");
					if(result == JOptionPane.YES_OPTION){
						int isTrue = saveImg();
						if(isTrue == -1){
							return;
						}
					}else if(result == JOptionPane.CANCEL_OPTION
							|| result == JOptionPane.CLOSED_OPTION){
						return;
					}
				}
				JFileChooser ch = new JFileChooser();
				int result2 = ch.showOpenDialog(gui);
				if(result2 == JFileChooser.APPROVE_OPTION){
					try {
						BufferedImage bi = ImageIO.read(ch.getSelectedFile());
						setImg(bi);
						hasPainted = true;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(gui, "Something went wrong! "
								+ "Check the console output!");
						e.printStackTrace();
					}
				}
			}
    	};
    	openImageItem.addActionListener(openListener);
    	
    	file.add(openImageItem);
    	
    	JMenuItem saveImageItem = new JMenuItem("Save");
    	ActionListener saveListener = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				saveImg();
			}
    	};
    	saveImageItem.addActionListener(saveListener);
    	
    	file.add(saveImageItem);
    	file.addSeparator();
    	
    	JMenuItem exitItem = new JMenuItem("Exit");
    	ActionListener exitListener = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				int result = JOptionPane.YES_NO_CANCEL_OPTION;
				if(hasPainted == true){
					result = JOptionPane.showConfirmDialog(gui,
							"Do you want to save the image first?");
					if(result == JOptionPane.YES_OPTION){
						int isTrue = saveImg();
						if(isTrue == -1){
							return;
						}
					}else if(result == JOptionPane.CANCEL_OPTION
							|| result == JOptionPane.CLOSED_OPTION){
						return;
					}
				}
				System.exit(0);
			}
    	};
    	exitItem.addActionListener(exitListener);
    	
    	file.add(exitItem);
    	
    	JMenu edit = new JMenu("Edit");
    	
    	JMenuItem resizeImageItem = new JMenuItem("Resize");
    	ActionListener resizeListener = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				showResizeFrame();
			}
    	};
    	resizeImageItem.addActionListener(resizeListener);
    	
    	edit.add(resizeImageItem);
    	
    	JMenu about = new JMenu("About");
    	
    	JMenuItem aboutItem = new JMenuItem("About Draw");
    	ActionListener aboutListener = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				Icon icon = new ImageIcon("drawIcon.png");
				JOptionPane.showMessageDialog(
						gui, 
						"Author: Musa Aliev\n"
						+ "email: aliev95m@gmail.com\n"
						+ "Draw v0.0.1", 
						"About Draw", 
						JOptionPane.INFORMATION_MESSAGE,
						icon);
			} 		
    	};
    	aboutItem.addActionListener(aboutListener);
    	
    	about.add(aboutItem);
    	
    	mb.add(file);
    	mb.add(edit);
    	mb.add(about);
    	
    	return mb;
    }
    private int saveImg(){
    	File f = new File("newimage.png");
    	JFileChooser ch = new JFileChooser();
    	ch.setSelectedFile(f);
    	
    	int result = ch.showSaveDialog(gui);
    	if(result == JFileChooser.APPROVE_OPTION){
    		f = ch.getSelectedFile();		
    		try {
				ImageIO.write(img, "png", f);
				hasPainted = false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(gui, "Something went wrong! "
						+ "Check the console output!");
				e.printStackTrace();
			}
    	}else{
    		return -1; 
    	}
    	return 1;
    }
    private void setStartPoint(int x, int y){
    	this.x = x;
    	this.y = y;
    }
    private void setEndPoint(int x2, int y2){
    	this.x2 = x2;
    	this.y2 = y2;
    }
    private void writeText(int x, int y){
    	String text = JOptionPane.showInputDialog(gui, "Text to add:", "Text");
    	Font f = new Font("Arial", Font.BOLD, (int) textModel.getValue());
    	
    	if(text != null){
    		Graphics2D g = img.createGraphics();
    		
    		g.setColor(color);
    		g.setFont(f);
    		
    		g.drawString(text, x, y);
    		
    		imageLabel.repaint();
    		g.dispose();
    	}
    }
    private void drawRectangle(int x, int y, int x2, int y2){
    	Graphics2D g = img.createGraphics();
    	
    	g.setColor(color);
    	int strokeSize = (int) strokeModel.getValue();
    	g.setStroke(new BasicStroke(
    		    		strokeSize, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
    	
    	int px = Math.min(x, x2);
        int py = Math.min(y, y2);
        int pw = Math.abs(x - x2);
        int ph = Math.abs(y - y2);
    	
    	g.drawRect(px, py, pw, ph);
    	
    	imageLabel.repaint();
    	g.dispose();
    }  
    private void drawCircle(int x, int y, int x2, int y2){
    	Graphics2D g = img.createGraphics();
    	
    	g.setColor(color);
    	g.setStroke(stroke);

    	int px = Math.min(x,x2);
    	int py = Math.min(y,y2);
    	int pw=Math.abs(x-x2);
    	int ph=Math.abs(y-y2);
    	
    	g.drawOval(px, py, pw, ph);
 
    	imageLabel.repaint();
    	g.dispose();
    	
    }
    private void drawLine(int x, int y, int x2, int y2){
    	Graphics2D g = img.createGraphics();
    	
    	g.setColor(color);
    	int strokeSize = (int) strokeModel.getValue();
    	g.setStroke(new BasicStroke(
    			strokeSize, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
    	
    	g.drawLine(x, y, x2, y2);
    	
    	imageLabel.repaint();
    	g.dispose();
    }
    private void draw(int x, int y){
    	Graphics2D g = img.createGraphics();
    	
    	g.setColor(color);
    	g.setStroke(stroke);
    	
    	g.drawLine(x, y, x, y );
    	
    	imageLabel.repaint();
    	g.dispose();
    }
	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		if(activeTool == Draw.BRUSH_TOOL || activeTool == Draw.TEXT_TOOL 
				|| activeTool == Draw.PAINT_TOOL){
			outputText = "X: " + e.getX() + ", Y: " + e.getY();
			mssg.setText(outputText);
		}else if(activeTool == Draw.RECT_TOOL || activeTool == Draw.CIRC_TOOL ||
				activeTool == Draw.LINE_TOOL){
			outputText = "Start point: " + e.getX() + ", "+ e.getY();
			mssg.setText(outputText);
		}
	} 
	@Override
	public void mouseDragged(MouseEvent e){
		// TODO Auto-generated method stub
		if(activeTool == Draw.BRUSH_TOOL){
			draw(e.getX(), e.getY()); 
		}else if(activeTool == Draw.RECT_TOOL || activeTool == Draw.CIRC_TOOL ||
				activeTool == Draw.LINE_TOOL){
					String outputText2 = "";
					outputText2 = ", End point: " + e.getX() + ", " + e.getY();
					mssg.setText(outputText + outputText2);
		}
		this.hasPainted = true;  	
    }
	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		if(activeTool == Draw.BRUSH_TOOL){
			draw(e.getX(), e.getY());
		}else if(activeTool == Draw.RECT_TOOL || activeTool == Draw.CIRC_TOOL ||
				activeTool == Draw.LINE_TOOL){
					setStartPoint(e.getX(), e.getY());
					outputText = "Start point: " + e.getX() + ", "+ e.getY();
					mssg.setText(outputText);
		}else if(activeTool == Draw.TEXT_TOOL){
			writeText(e.getX(), e.getY());
		}else if(activeTool == Draw.PAINT_TOOL){
			clean(img);
		}
		this.hasPainted = true;
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		if(activeTool == Draw.LINE_TOOL){
			setEndPoint(e.getX(), e.getY());
			drawLine(x, y, x2, y2);
			e.consume();
		}else if(activeTool == Draw.RECT_TOOL){
			setEndPoint(e.getX(), e.getY());
			drawRectangle(x, y, x2, y2);
			e.consume();
		}else if(activeTool == Draw.CIRC_TOOL){
			setEndPoint(e.getX(), e.getY());
			drawCircle(x, y, x2, y2);
			e.consume();
		}
		this.hasPainted = true;
	}
	@Override
	public void mouseClicked(MouseEvent e) {}
	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}
	private void showResizeFrame(){
	    	JDialog dialogR = new JDialog();
	    	dialogR.setSize(225, 200);
	    	dialogR.setLocationByPlatform(true);
	    	dialogR.setResizable(false);
	    	dialogR.setAlwaysOnTop(true);
	    	dialogR.setModal(true);
	    	dialogR.setModalityType(ModalityType.APPLICATION_MODAL);
	    		
	    	JLabel widthLabel = new JLabel("Width:");
	    	widthLabel.setBounds(20, 10, 50, 30);
	    	JTextField widthTF = new JTextField();
	    	widthTF.setBounds(150, 18, 50, 20);
	    	widthTF.setToolTipText("input number from 1 to 9999");
	    	
	    	JLabel heightLabel = new JLabel("Height:");
	    	heightLabel.setBounds(20, 40, 50, 30);
	    	JTextField heightTF = new JTextField();
	    	heightTF.setBounds(150, 48, 50, 20);
	    	heightTF.setToolTipText("input number from 1 to 9999");
	    	
	    	JCheckBox checkBox = new JCheckBox("Maintain aspect ratio");
	    	checkBox.setBounds(15, 70, 150, 30);
	    	
	    	checkBox.addItemListener(new ItemListener(){
				@Override
				public void itemStateChanged(ItemEvent ie) {
					// TODO Auto-generated method stub
					if(ie.getStateChange() == 1){
						heightTF.setEditable(false);
						height = (int) (Integer.parseInt(widthTF.getText()) * 0.561660f);
						heightTF.setText(height + "");
					}else{
						heightTF.setEditable(true);
					}
				}
	    	}); 
	    	KeyListener keyL = new KeyListener(){
				@Override
				public void keyReleased(KeyEvent ke) {
					// TODO Auto-generated method stub
					if(ke.getKeyCode() < KeyEvent.VK_0 || ke.getKeyCode() > KeyEvent.VK_9
							&& ke.getKeyCode() < KeyEvent.VK_NUMPAD0 || ke.getKeyCode() > KeyEvent.VK_NUMPAD9){
						if(!checkKeys(ke.getKeyCode())){
							JOptionPane.showMessageDialog(dialogR, 
								"Input positive number", 
								"Alert", 
								JOptionPane.WARNING_MESSAGE);
							if(ke.getSource() == widthTF){
								widthTF.setText(null);
							}else{
								heightTF.setText(null);
							}
						}
					}					
					if(checkBox.isSelected()){
						height = (int) (Integer.parseInt(widthTF.getText()) * 0.561660f);
						heightTF.setText(height + "");
					}
				}
				@Override
				public void keyTyped(KeyEvent ke){}
				@Override
				public void keyPressed(KeyEvent ke) {}
	    	};
	    	widthTF.addKeyListener(keyL);
	    	heightTF.addKeyListener(keyL);
	    	
	    	JButton okButton = new JButton("OK");
	    	okButton.setBounds(150, 130, 50, 25);
	    	
	    	okButton.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					width = Integer.parseInt(widthTF.getText());
					height = Integer.parseInt(heightTF.getText());
					if((width > 0 && width < 10000) 
							&& (height > 0 && height < 10000)){
						Image image = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
						resizedImg = new BufferedImage(
								width, 
								height, 
								Image.SCALE_REPLICATE);
						resizedImg.getGraphics().drawImage(image, 0, 0, gui);
						
						setImg(resizedImg);
						dialogR.dispose();
					}else{
						JOptionPane.showMessageDialog(dialogR, 
							"Write numbers between 1 and 9999!", 
							"Alert", 
							JOptionPane.WARNING_MESSAGE);	
					}
				}
	    	});
	    	dialogR.add(widthLabel);
	    	dialogR.add(widthTF);
	    	dialogR.add(heightLabel);
	    	dialogR.add(heightTF);
	    	dialogR.add(checkBox);
	    	dialogR.add(okButton);
	    	
	    	dialogR.setLayout(null);
	    	dialogR.setVisible(true);	    	
	}
	private boolean checkKeys(int key){
		switch(key){
			case KeyEvent.VK_DOWN : return true;
			case KeyEvent.VK_UP : return true;
			case KeyEvent.VK_LEFT : return true;
			case KeyEvent.VK_RIGHT : return true;
			case KeyEvent.VK_ENTER : return true;
			case KeyEvent.VK_BACK_SPACE : return true;
		}
		 return false;
	 }	
	public static void main(String[] args){
   	 new Draw();	
   }
}
