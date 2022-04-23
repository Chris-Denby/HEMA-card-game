/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package NetCode;

/**
 *
 * @author chris
 */
/**
 * Java TCP sockets provide a reliable connected communication between the client and server.(Similar to making a phone call)
 * TCP establishes a connection before the client and server can begin to communicate.
 * Threading is required for concurrency to handle mutilple clients at the same time.
 * Server sits in an infnite loop waiting for client to connect and delegates a thread to handle each connection.(Thread-per-connection)
 * Run the server program first before running the client program.
 * ******************************************************
 */
import Interface.GameWindow;
import Interface.Message;
import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TCPServer
{
    GameWindow gameWindow;
    Socket clientSocket;
    ServerSocket listenSocket;
    boolean isConnected = false;
    ObjectInputStream in;
    ObjectOutputStream out;
    // the port number the process listens at.
    int serverPort = 8888;
    Connection connection;
    
    public TCPServer()
    {
        try
        {
            // TCP server socket assigned to the port number
            listenSocket = new ServerSocket(serverPort);
            System.out.println("TCP Server running...");
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //Server sitting in an infinite loop waiting for clients to connect.
        while(!isConnected)
        {
            try
            {
            //connecting with the client established
            clientSocket = listenSocket.accept();
            isConnected = true;
            in = new ObjectInputStream( clientSocket.getInputStream());
            out =new ObjectOutputStream( clientSocket.getOutputStream());
            connection = new Connection(clientSocket);
            }
            catch(IOException e) 
            {
                System.out.println("Connection:"+e.getMessage());
            }
        }
    }
    
    public boolean isConnected()
    {
        return isConnected;
    }
    
    public Socket getClientSocket()
    {
        return clientSocket;
    }
    
    public void setGameWindow(GameWindow window)
    {
        gameWindow = window;
    }
    
    public GameWindow getGameWindow()
    {
        return gameWindow;
    }
    
    public void sendMessage(Message message)
    {
        try
        {
            //send connection handshake to check if server responds
            System.out.println("Server sending message [" + message.getText() + "] to client");
            //send message
            out.writeObject(message);
        }
        catch(IOException ex)
        {
            System.out.println("Message failed to send from Server: " + ex.getMessage());
        } 
    }
    
    public void closeConnection()
    {
        try
        {
            listenSocket.close();
            clientSocket.close();
            in.close();
            out.close();  
            isConnected = false;
        }
        catch(IOException ex)
        {
            
        }
    }
    
    //seperate class to handle each connection with thread capability.
    class Connection extends Thread
    {
        Socket clientSocket;

        public Connection (Socket aClientSocket)
        {
                clientSocket = aClientSocket;
                this.start();
        }

        public void run()
        {
            try 
            {
                //contine dealing with client requests
                while(true)
                {
                    // read the Message instance with text sent by client
                    Message m = (Message)in.readObject();
                    //interpret message
                    
                    System.out.println("Message" + m.getText() + "received from  client");
                    
                    if(m!=null)
                    {
                        gameWindow.receiveMessage(m);
                    }
                }//end of while
            }// end of try
            catch (EOFException e)
            {
                System.out.println("EOF:"+e.getMessage());
            }
            catch(IOException e)
            {
                System.out.println("readline:"+e.getMessage());
            }
            catch(ClassNotFoundException ex)
            {
                ex.printStackTrace();
            }
        }
    }
} //end of TCPServer

