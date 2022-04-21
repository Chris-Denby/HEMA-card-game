/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interface;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;


/**
 *
 * @author chris
 */
public class ApplicationWindow extends JFrame
{
    JTabbedPane tabbedPane;
    
    //constructor
    public ApplicationWindow()
    {
        tabbedPane = new JTabbedPane();
        StartGameWindow startGameWindow = new StartGameWindow(tabbedPane,this);
        tabbedPane.addTab("Game Type", startGameWindow);
        this.add(tabbedPane);
        
    
        //SET JFRAME PARAMETERS
        setTitle("Card Game");
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        Dimension dimensions = new Dimension(Constants.windowWidth,Constants.windowHeight);
        this.setMinimumSize(dimensions);
        
        //ADD LISTENERS

        this.addComponentListener(new ComponentAdapter() 
        {  
                public void componentResized(ComponentEvent evt) {
                    repaintGlass();
                }
        });
        
        //MAKE THE JFRAME VISIBLE
        pack();
        setVisible(true);        
    }
    
    private void repaintGlass()
    {   
        /**
        try
        {
            GameWindow window = (GameWindow)tabbedPane.getTabComponentAt(tabbedPane.indexOfTab("GAME"));
            if(this.getGlassPane().isVisible())
                window.drawPointer();
        }
        catch(IndexOutOfBoundsException e)
        {}
        **/    
    }  
}
