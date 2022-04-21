/**
 * Java TCP sockets provide a reliable connected communication between the client and server(Similar to making a phone call)
 * TCP sockets uses streams and have the ability to send and receive objects.
 * The client receives input from the user and reads and writes to the streams established for communication.
 * The client ends the coversation by typing exit.
 * Run the server program first before running the client program.
 * ******************************************************
 */
package NetCode;
import Interface.GameWindow;
import Interface.Message;
import Interface.Message;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TCPClient 
{
    GameWindow gameWindow;
    boolean isConnected = false;
    Socket serverSocket = null;
    ObjectInputStream in = null;
    ObjectOutputStream out = null;
    //port number the process listens at.
    int serverPort = 8888;
    Connection connection;
    
    public TCPClient(String ipAddress) throws IOException
    {
            // tcp clinet socket assigned.
            serverSocket = new Socket(ipAddress, serverPort);
            isConnected = true;
            // input and output streams assigned and linked to the socket
            out = new ObjectOutputStream(serverSocket.getOutputStream());
            in = new ObjectInputStream(serverSocket.getInputStream());
            connection = new Connection(serverSocket);
            System.out.println("Client connected");
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
            System.out.println("Client sending message [" + message.getText() + "] to server");
            //send message
            out.writeObject(message);  
        }
        catch(IOException ex)
        {
            System.out.println("Message failed to send from Client:");
            ex.printStackTrace();
        } 
    }
    
    public boolean isConnected()
    {
        return isConnected;
    }
    
    public Socket getServerSocket()
    {
        return serverSocket;
    }
    
    public void closeConnection()
    {
        try
        {
            serverSocket.close();
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
        Socket serverSocket;

        public Connection (Socket aClientSocket)
        {
                serverSocket = aClientSocket;
                this.start();
        }

        public void run()
        {
            try 
            {
                //contine dealing with client requests
                while(true)
                {
                    // read the Message instance with text sent by server
                    Message m = (Message)in.readObject();
                    //interpret message
                    
                    System.out.println("Message " + m.getText() + " received from  server");
                                        
                    if(m!=null)
                    {
                        gameWindow.recieveMessage(m);
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
}
