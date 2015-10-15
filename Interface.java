package imgurParseWin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import sun.awt.image.ImageFormatException;



	/*
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */



public class Interface extends JFrame {
	
	JTextField tfs[];
	MouseListener l;
    Polygon poly;
    JPanel p;
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == tfs[0]){
			tfs[0].setText("");
		}
	}
	
	
	protected void paintComponent(Graphics g) {
		int xPoly[] = {150, 250, 325, 375, 450, 275, 100};
        int yPoly[] = {150, 100, 125, 225, 250, 375, 300};
        poly = new Polygon(xPoly, yPoly, xPoly.length);
        
        super.paintComponents(g);
        g.setColor(Color.BLUE);
        g.drawPolygon(poly);
    }
    
	
	   private void initComponents() {

	        
	        setResizable(false);

	        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

	        int xPoly[] = {50, 250, 250, 540,  540,  /* 260,  260, 530, 530,*/ 50};
	        int yPoly[] = {50,  50,  10,  10,  80/*210*/,  /* 210,  170, 170,  80,*/ 80 };

	        poly = new Polygon(xPoly, yPoly, xPoly.length);
	        p = new JPanel() {
	            @Override
	            protected void paintComponent(Graphics g) {
	                super.paintComponent(g);
	                g.setColor(new Color(130,155,155));
	                g.fillPolygon(poly);
	            }

	            @Override
	            public Dimension getPreferredSize() {
	                return new Dimension(550,220);
	            }
	            
	        };
	        p.setBackground(new Color(255,0,0));
	        add(p);
	        pack();
	        setVisible(true);

	    }
	
	
	public Interface() throws IOException, InterruptedException, ImageFormatException {
		imgurParseWin.skipConsole=true;
		initComponents();
	    p.setLayout(new GridBagLayout());
	    GridBagConstraints c = new GridBagConstraints();
	    c.insets = new Insets(5, 5, 5, 5);
	    JButton btns[] = new JButton[2];
	    JCheckBox chboxes[] = new JCheckBox[5];
	    JLabel jls[] = new JLabel[5];
//	    JTextField tfs[] = new JTextField[5];
	    tfs = new JTextField[5];
	    
	    // background polygon code
	    
	    c.gridy = 0;
	    c.gridx = 0;
	    for(int i = 0;i<btns.length;i++){
	    	if(i==0){
	    		btns[i] = new JButton(""+"run custom scrape");
	    		btns[i].addMouseListener(new MouseAdapter(){
	    			@Override
	    			public void mouseClicked(MouseEvent e){

	    				System.out.print("Clicked custom button, ");
	    				if(chboxes[4].isSelected()){
	    					System.out.println("quick mode enabled");	
	    				}
	    				if(!chboxes[4].isSelected()){
	    					System.out.println("quick mode not enabled");	
	    				}
	    				
	    				String url = tfs[0].getText();
	    				imgurParseWin.article=url;
	    				imgurParseWin.saveTo=(tfs[1].getText());
	    				imgurParseWin.strtpg=Integer.parseInt(tfs[2].getText());
	    				imgurParseWin.pageLimit=Integer.parseInt(tfs[3].getText());
	    				imgurParseWin.quickMode=chboxes[4].isSelected();
	    				try {
							imgurParseWin.scrape();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (ImageFormatException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
	    				
	    			}
	    		});
	    	}
	    	if(i==1){
	    		btns[i] = new JButton(""+"run simple scrape (grey)");
	    		btns[i].addMouseListener(new MouseAdapter(){
	    			@Override
	    			public void mouseClicked(MouseEvent e){
	    				System.out.print("Clicked simple button, ");
	    				if(chboxes[4].isSelected()){
	    					System.out.println("quick mode enabled");	
	    				}
	    				String url = tfs[0].getText();
	    				imgurParseWin.article=url;
	    				try {
							imgurParseWin.scrape();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (ImageFormatException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
	    			}
	    		});
	    	}
	    	else if(i>1)
	    		btns[i] = new JButton(""+i);
	        //getContentPane().add(btns[i],c);
	    	p.add(btns[i], c);
	        c.gridx++;
	    }

	    c.gridy = 1;
	    c.gridx = 0;
	    for(int i = 0;i<chboxes.length;i++){
	    	tfs[i]=new JTextField();
	    	jls[i]=new JLabel();
	    	if(i==0){
	    		
	    		jls[i].setText("URL ===>");
	    		tfs[i].setText("                  enter Imgur URL                                        ");
	    		tfs[i].addMouseListener(new MouseAdapter(){
		            @Override
		            public void mouseClicked(MouseEvent e){
		            	tfs[0].setText("");
		            }
		        });
	    		
	    		c.gridx=1;
	    	//	getContentPane().add(tfs[i],c);
	    		p.add(tfs[i], c);
	    		c.gridx=0;
	    	//	getContentPane().add(jls[i],c);
	    		p.add(jls[i], c);
	    		c.gridx=1;
	    		
	    	}else if(i==1){
	    		jls[i].setText("local directory ===>");
	    		tfs[1].setText("                     enter local directory                                  ");
	    		tfs[1].addMouseListener(new MouseAdapter(){
		            @Override
		            public void mouseClicked(MouseEvent e){
		            	tfs[1].setText("");
		            }
		        });
	    		
	    		c.gridx=1;
	    		//getContentPane().add(tfs[i],c);
	    		
	    		p.add(tfs[i], c);
	    		c.gridx=0;
	    		//getContentPane().add(jls[i],c);
	    		p.add(jls[i], c);
	    		c.gridx=1;
	    	}
	    	else if(i==2){
	    		jls[i].setText("start page===>");
	    		tfs[i].setText("                  Start page (Default is 0)                               ");
	    		tfs[i].addMouseListener(new MouseAdapter(){
		            @Override
		            public void mouseClicked(MouseEvent e){
		            	tfs[2].setText("");
		            }
		        });
	    		c.gridx=1;
	    		//getContentPane().add(tfs[i],c);
	    		p.add(tfs[i], c);
	    		c.gridx=0;
	    		//getContentPane().add(jls[i],c);
	    		p.add(jls[i], c);
	    		c.gridx=1;
	    		
	    	} else if(i==3){
	    		jls[i].setText("page depth ===>");
	    		tfs[i].setText("   Page depth (0 is infinite, 1 page is approximately 60 images) ");
	    		tfs[i].addMouseListener(new MouseAdapter(){
		            @Override
		            public void mouseClicked(MouseEvent e){
		            	tfs[3].setText("");
		            }
		        });
	    		c.gridx=1;
	    		//getContentPane().add(tfs[i],c);
	    		p.add(tfs[i], c);
	    		c.gridx=0;
	    		//getContentPane().add(jls[i],c);
	    		p.add(jls[i], c);
	    		c.gridx=1;
	    	} else if (i==4){
	    		chboxes[i]=new JCheckBox();
	    		chboxes[i].setText("Quick mode (jpg only)");
	    		p.add(chboxes[i],c);
	    	//	getContentPane().add(chboxes[i],c);
	    	}
	    	
	        c.gridy++;
	    }
	    
	    pack();
	    setVisible(true);
	}


	

    	
    	

   
}
