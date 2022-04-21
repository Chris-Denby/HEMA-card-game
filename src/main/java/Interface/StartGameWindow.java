/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interface;

import Database.JSONHelper;
import NetCode.TCPServer;
import NetCode.TCPClient;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;


/**
 *
 * @author chris
 */
public class StartGameWindow extends JPanel
{
    StartGameWindow self;
    JTabbedPane parentTabbedPane;
    private TCPClient netClient;
    private TCPServer netServer;
    private JFrame applicationWindow;
    
    HashMap<Integer,Image> imageCache = new HashMap<Integer,Image>();
    JSONHelper jsonHelper = new JSONHelper();
    
    JButton startLocalButton = new JButton("Start local game");
    JButton startClientButton = new JButton("Join net game");
    JButton startServerButton = new JButton("Host net game");
    
    public StartGameWindow(JTabbedPane pane, JFrame appWindow)
    {
        applicationWindow = appWindow;
        self = this;
        parentTabbedPane = pane;

        Dimension verticalPanelDimensions = new Dimension(this.getWidth(),(Constants.windowHeight/3)-20);
        Dimension horizontalPanelDimensions = new Dimension((Constants.windowWidth/3)-20,this.getHeight());
        this.setLayout(new BorderLayout());
        JPanel topPanel = new JPanel();
        JPanel bottomPanel = new JPanel();
        JPanel middlePanel = new JPanel();
        JPanel leftPanel = new JPanel();
        JPanel rightPanel = new JPanel();
        topPanel.setPreferredSize(verticalPanelDimensions);
        bottomPanel.setPreferredSize(verticalPanelDimensions);
        middlePanel.setPreferredSize(verticalPanelDimensions);
        leftPanel.setPreferredSize(horizontalPanelDimensions);
        rightPanel.setPreferredSize(horizontalPanelDimensions);

        JLabel titleLabel = new JLabel("Mage Ick That Gather Rings");
        Font font = new Font("Arial",Font.BOLD,48);
        titleLabel.setFont(font);

        this.add(topPanel,BorderLayout.NORTH);
        this.add(bottomPanel,BorderLayout.SOUTH);
        this.add(middlePanel,BorderLayout.CENTER);
        this.add(leftPanel,BorderLayout.WEST);
        this.add(rightPanel,BorderLayout.EAST);

        topPanel.setLayout(new GridBagLayout());
        //topPanel.add(titleLabel,SwingConstants.CENTER);

        middlePanel.setLayout(new GridLayout(2,1,0,20));
        middlePanel.add(startClientButton);
        middlePanel.add(startServerButton);

        JSONHelper jh = new JSONHelper();  
        
        loadImageCache();
        
        startClientButton.addActionListener((new ActionListener() 
        {
            @Override
            public void actionPerformed(ActionEvent e) 
            {
                showIPAddressDialog();
                
            }
        }));   
        
        startServerButton.addActionListener((new ActionListener() 
        {
            @Override
            public void actionPerformed(ActionEvent e) 
            {
               startServer(); 
               startServerButton.setText("Server Started");               
            }
        }));
        
        startLocalButton.addActionListener((new ActionListener() 
        {
            @Override
            public void actionPerformed(ActionEvent e) 
            {
               startGame(); 
            }
        }));
    }
    
    private void startServer()
    {
        SwingWorker workerThread = new SwingWorker<TCPServer,Void>() 
        {
            
            @Override
            protected TCPServer doInBackground() throws Exception 
            {
                netServer = new TCPServer();
                return netServer;
            }
            
            
            @Override
            public void done()
            {
                try 
                {
                    netServer = (TCPServer) get();
                    if(netServer.isConnected())
                    {
                        startGame(netServer);
                        applicationWindow.setTitle(applicationWindow.getTitle() + " - SERVER");
                    }
                } 
                catch (InterruptedException ex) 
                {
                    Logger.getLogger(StartGameWindow.class.getName()).log(Level.SEVERE, null, ex);
                } 
                catch (ExecutionException ex) 
                {
                    Logger.getLogger(StartGameWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
            } 
            
        };
        workerThread.execute();
    }
    
    private void startClient(String ipAddress)
    {
        SwingWorker workerThread = new SwingWorker<TCPClient,Void>()
        {
            @Override
            protected TCPClient doInBackground() throws Exception 
            {
                netClient = new TCPClient(ipAddress); 
                return netClient;
            }
            
            @Override
            public void done()
            {
                try 
                {
                    netClient = (TCPClient) get();
                    if(netClient.isConnected())
                    {
                        startGame(netClient);
                        applicationWindow.setTitle(applicationWindow.getTitle() + " - CLIENT");
                    } 
                } 
                catch (InterruptedException ex) 
                {
                    Logger.getLogger(StartGameWindow.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(self,"Failed to connect: " + ex.getMessage(),"Failed to connect",JOptionPane.ERROR_MESSAGE);
                } 
                catch (ExecutionException ex) 
                {
                    Logger.getLogger(StartGameWindow.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(self,"Failed to connect: " + ex.getMessage(),"Failed to connect",JOptionPane.ERROR_MESSAGE);
                }
            }
        };  
        workerThread.execute();
    }
    
    private void startGame()
    {
        parentTabbedPane.addTab("Game", new GameWindow(parentTabbedPane,this));
        parentTabbedPane.setSelectedIndex(1);
    }
    
    public void stopNetworkGame(int clientType)
    {
        //0 = client
        //1 = server
        
        if(clientType==0)
            netClient.closeConnection();    
        else
            netServer.closeConnection();
    }
    
    private void startGame(TCPClient client)
    {
        parentTabbedPane.addTab("Game", new GameWindow(parentTabbedPane, client,this));
        parentTabbedPane.setSelectedIndex(1);    
    }
    
    private void startGame(TCPServer server)
    {
        parentTabbedPane.addTab("Game", new GameWindow(parentTabbedPane, server,this));
        parentTabbedPane.setSelectedIndex(1);    
    }
    
    public void showIPAddressDialog()
    {
        //String ipAddress = (String) JOptionPane.showInputDialog(this, "Enter IP Address","xxxx:8888");
        String ipAddress = (String) JOptionPane.showInputDialog(this, "Enter IP Address","localhost");
        startClient(ipAddress);
        
    }
    
     public void loadImageCache()
    {         
        Image img = null;
        File imageFolder = new File("images");
        
        for(File file:imageFolder.listFiles())
        {
             try{
                String filename = file.getName().substring(0, file.getName().length()-4);
                img = ImageIO.read(file);
                imageCache.put(Integer.parseInt(filename), img);
            } catch (IOException ex) {
                Logger.getLogger(StartGameWindow.class.getName()).log(Level.SEVERE, null, ex);
            }  
        }
        img.flush();
    }
    
    public Image getImageFromCache(int imageID)
    {
        return imageCache.get(imageID);
    }
    
    
}
