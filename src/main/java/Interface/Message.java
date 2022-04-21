/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interface;

import Interface.Cards.Card;
import java.io.Serializable;
import org.json.simple.JSONObject;

/**
 *
 * @author chris
 */

// Should implement serializable for instances of this class to be written to streams.
public class Message implements Serializable
 {
    private String text;
    private String reply;
    private JSONObject jsonCard;

    public Message()
    {

    }
    
    public Message(String message)
    {
        setText(message);        
    }

    public String getText() 
    {
        return text;
    }

    public void setText(String text) 
    {
        this.text=text;
    }

    public String getReply() 
    {
        return reply;
    }

    public void setReply(String text) 
    {
        this.reply=text;
    }

    public JSONObject getJsonCard() {
        return jsonCard;
    }

    public void setJsonCard(JSONObject jsonCard) {
        this.jsonCard = jsonCard;
    }
        
        
  }

