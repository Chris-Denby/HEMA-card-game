/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interface.Cards;

import Interface.AssetHelper;
import Interface.Constants;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.Serializable;
import javax.swing.JLabel;
import javax.swing.JPanel;
import Interface.Constants.CardLocation;
import Interface.Constants.CreatureEffect;
import Interface.InnerCardPanel;
import Interface.PlayArea;
import Interface.PlayerHand;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;



/**
 *
 * @author chris
 */

public class Card extends JPanel implements Serializable, Cloneable
{
    private int width;
    private int height;
    private int arcSize;
    private int strokeSize = 1;
    private Color shadowColor = Constants.shadowColor;
    private boolean dropShadow = true;
    private boolean highQuality = true;
    private int shadowGap = 4;
    private int shadowOffset = 4;
    private int shadowAlpha = 150; //transparency from (0-255)
    private Color cardBaseColor = Constants.cardBaseColor;
    private Color backgroundColor = cardBaseColor;
    private boolean isActivated = false;
    private String cardName;
    private int cardID;
    int playCost = 1;
    private CardLocation location;
    private boolean isFaceUp = false;
    PlayArea playArea;
    PlayerHand playerHand;
    Dimension dimension = new Dimension(width,height);
    int headingFontSize =8;
    int bodyFontSize = 8;
    Font headingFont = new Font("Arial",Font.BOLD,headingFontSize);
    Font bodyFont = new Font("Arial",Font.PLAIN,bodyFontSize);
    private boolean isSelected = false;
    int imageID;
    private CreatureEffect creatureEffect = CreatureEffect.NONE;
    private boolean isPlayable;
    JLabel cardNameLabel;
    JPanel topPanel;
    ImagePanel pictureBox;
    JPanel abilityPanel;
    JLabel playCostLabel;
    JPanel bodyBox;
    JTextPane textBox;
    InnerCardPanel innerPanel;
    Image cardBack;
    Image cardImage;
    boolean zoomed = false;
    int cardValue;
    JLabel abilityLabel;
    

    public Card(String cardName, int imageId)
    {
        this.cardName = cardName;
        this.imageID = imageID;
        
        innerPanel = new InnerCardPanel();
                
        
        topPanel = new JPanel();
        abilityLabel = new JLabel("Basic", SwingConstants.CENTER);
        abilityPanel = new JPanel();
        
        abilityPanel.setLayout(new GridLayout(1,1));
        abilityLabel.setHorizontalAlignment(SwingConstants.CENTER);
        abilityLabel.setFont(headingFont);
        //abilityLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        abilityLabel.setOpaque(false);
        abilityPanel.add(abilityLabel);
        bodyBox = new JPanel();
        textBox = new JTextPane();
        textBox.setEditable(false);
        textBox.setOpaque(false);
        setBodyText("Basic");
        

        bodyBox.add(textBox);
        pictureBox = new ImagePanel();

        topPanel.setBackground(new Color(0,0,0,100));
        abilityPanel.setBackground(new Color(255,255,255,255));
        //abilityPanel.setBackground(new Color(255,255,255,180));
        pictureBox.setBackground(new Color(255,255,255,0));
        bodyBox.setBackground(new Color(255,255,255,180));
        innerPanel.setBackground(new Color(0,102,102));
        
        topPanel.setVisible(isFaceUp);
        pictureBox.setVisible(isFaceUp);
        bodyBox.setVisible(isFaceUp);        
        abilityPanel.setVisible(isFaceUp);

        
        cardNameLabel = new JLabel(this.cardName,SwingConstants.LEFT);
        cardNameLabel.setForeground(Color.WHITE);
        playCostLabel = new JLabel(""+playCost,SwingConstants.RIGHT);
        playCostLabel.setForeground(Color.WHITE);
                

        

        innerPanel.setLayout(new BoxLayout(innerPanel,BoxLayout.Y_AXIS));

        this.add(innerPanel);

        topPanel.setLayout(new BorderLayout());
        topPanel.add(cardNameLabel,BorderLayout.WEST);
        topPanel.add(playCostLabel, BorderLayout.EAST);
        
        innerPanel.add(topPanel); 
        innerPanel.add(pictureBox);
        innerPanel.add(abilityPanel);
        innerPanel.add(bodyBox);


    }
    
    public PlayerHand getPlayerHand()
    {
        return playerHand;
    }

    public CreatureEffect getCreatureEffect()
    {
        return this.creatureEffect;
    }

    public void setCreatureEffect(CreatureEffect effect) {
        this.creatureEffect = effect;
        if(this.getCreatureEffect()!=CreatureEffect.NONE){
            setBodyText(this.getCreatureEffect().toString());
            abilityLabel.setText(this.getCreatureEffect().toString().replace('_', ' '));  
            }
    }
    
    public void setCardBack(Image img)
    {
        cardBack = img;
    }

    public int getCardValue() {
        return cardValue;
    }
    
    public void setImageID(int id)
    {
        imageID = id;
    }

    public void setCardID(int id)
    {
        cardID = id;
    }
    
    public int getCardID()
    {
        return cardID;
    }
    
    public int getImageID()
    {
        return imageID;
    }
        
    public String getName()
    {
        return cardName;
    }
    
    public void setName(String name)
    {
        cardName = name;
        cardNameLabel.setText(cardName);
    }
    
    public void setFaceUp(boolean is)
    {
        this.isFaceUp = is;
        if(isFaceUp){
            this.backgroundColor =  cardBaseColor;
        }
        else{
            this.backgroundColor = Color.GRAY;
        }

        innerPanel.setVisible(is);
        topPanel.setVisible(isFaceUp);
        pictureBox.setVisible(isFaceUp);
        bodyBox.setVisible(isFaceUp);
        abilityPanel.setVisible(isFaceUp);
    }
    
    public void applySize(int h)
    {
        //parameter 'h' is the container height of hte card.
        
        //SET RECTANGLE 
        this.height = (int) Math.round(h*0.75); //resize the card to be a fraction of its container height
        this.setOpaque(false); //makes this panel transparent
        this.width = (int) Math.round(height*Constants.cardAspectRatio);
        arcSize = (int) Math.round(height/20);
        this.setMinimumSize(new Dimension(width,height));
        this.setPreferredSize(new Dimension(width,height));
        this.setSize(new Dimension(width,height));
        
        this.setLayout(null);

        innerPanel.setBounds(
                arcSize,
                arcSize,
                width-shadowGap-arcSize-arcSize,    //width
                height-shadowGap-arcSize-arcSize);  //height
        
        int innerWidth = innerPanel.getBounds().width;
        int innerHeight = innerPanel.getBounds().height;
        
        if(cardImage!=null){
            //cardImage = cardImage.getScaledInstance(innerWidth, innerHeight, Image.SCALE_DEFAULT);
            this.setImage(cardImage);
        }
        
        
        topPanel.setPreferredSize(new Dimension(innerWidth,(int) Math.round((innerHeight/10)*1)));
        pictureBox.setPreferredSize(new Dimension(innerWidth,Math.round((innerHeight/10)*4)));
        //bodyBox.setPreferredSize(new Dimension(innerWidth,(int) Math.round((innerHeight/10)*3.5)));
        abilityPanel.setPreferredSize(new Dimension(innerWidth,(Math.round(innerHeight/10)*1)+4));
        
                
        if(zoomed){
            headingFont = new Font("Arial",Font.BOLD,20);
            bodyFont = new Font("Arial",Font.PLAIN,20); 
        }
        
        cardNameLabel.setFont(headingFont);
        playCostLabel.setFont(headingFont);
        bodyBox.setFont(bodyFont);
        
        repaint();
     
    }      

    public void setIsSelected(boolean is)
    {
        if(is)
        backgroundColor = Color.orange;
        else
            backgroundColor = cardBaseColor;
        
        isSelected = is;
        repaint();
        revalidate();
    }
    
    public void setIsActivated(boolean is)
    {
        if(is)
        backgroundColor = Color.GRAY;
        else
            backgroundColor = cardBaseColor;
        
        isActivated = is;
        repaint();
        revalidate();
    }
    
    public boolean getIsActivated()       
    {
        return isActivated;
    }
    
    public boolean getIsSelected()
    {
        return isSelected;
    }
    
    public void setPlayCost(int cost)
    {
        playCost = cost;
        playCostLabel.setText(cost+"");
    }
    
    public int getPlayCost()
    {
        return playCost;
    }
    
    public void setCardLocation(CardLocation l)
    {
        location = l;
        if(location==CardLocation.PLAYER_PLAY_AREA | location==CardLocation.OPPONENT_PLAY_AREA)
        {
            this.remove(topPanel);
        }
        revalidate(); 
        repaint();
    }
    
    public void setPlayArea(PlayArea area)
    {
        this.playArea = area;
    }
    
    public void setPlayerHand(PlayerHand hand)
    {
        this.playerHand = hand;
    }
    
    public void removeFromPlayerHand()
    {
        playerHand.removeCard(this);
    }
    
    public void removeFromPlayArea()
    {
        playArea.removeCard(this);
    }
    
    public void setImage(Image img)
    {
        cardImage = img;
        innerPanel.setImage(cardImage);
    }
    
    public void setBodyText(String text)    
    {
                

        SimpleAttributeSet attribs = new SimpleAttributeSet();
        StyleConstants.setAlignment(attribs, StyleConstants.ALIGN_CENTER);
        StyleConstants.setFontFamily(attribs, "SansSerif");
        StyleConstants.setFontSize(attribs, bodyFontSize);
        textBox.setParagraphAttributes(attribs, true);

        if(text.equals("Basic"))
        {
            textBox.setText(text);
            return;
        }
        
        if(textBox.getText().equals("Basic"))
            textBox.setText("");
            
        String textToAdd = text.replace('_', ' ');
        StringBuilder sb = new StringBuilder(textBox.getText());
        if(!textBox.getText().equals(""))
            sb.append("\n");
        
        sb.append(textToAdd);
 
        textBox.setText(sb.toString());
        
        /**

        StyledDocument doc = (StyledDocument) bodyBox.getDocument();
        Style style = doc.addStyle("style", null);
        
        StyleConstants.setFontSize(style , bodyFontSize);
        StyleConstants.setAlignment(style, StyleConstants.ALIGN_CENTER);
        
        //doc.setParagraphAttributes(0, doc.getLength(), set , true);
        //doc.setCharacterAttributes(0, doc.getLength(), set, false);
        
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException ex) {
            Logger.getLogger(Card.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        **/
    }
    
    public void setIsPlayable(boolean is)
    {
        isPlayable = is; 
        
        if(is && isFaceUp)
            backgroundColor = Color.GREEN;
        else if (!is)
        {
            if(isFaceUp)
                this.setFaceUp(true);
            else
                setFaceUp(false);
        }
        repaint();
        revalidate();
    }

    public PlayArea getPlayArea() {
        return playArea;
    }
    
    public void setZoomed(boolean is)
    {
        zoomed = is;
        if(is)
        {
            SimpleAttributeSet attribs = new SimpleAttributeSet();
            StyleConstants.setAlignment(attribs, StyleConstants.ALIGN_CENTER);
            StyleConstants.setFontFamily(attribs, "SansSerif");
            StyleConstants.setFontSize(attribs, bodyFontSize);
            textBox.setParagraphAttributes(attribs, true);
            
            
            String effectString = creatureEffect.toString().replace('_', ' ');
            String effectDescription;
            StringBuilder sb = new StringBuilder();
            
            //ETB EFFECT
            if(creatureEffect==CreatureEffect.Buff_Power)
            {
                int buffValue = Math.round(getPlayCost()/Constants.buffModifier);
                if(buffValue<1)
                    buffValue = 1;
                
                effectDescription = "Increases the power of the two left minions by " + buffValue + " while in play";
                sb.append(effectString);
                sb.append("\n");
                sb.append(effectDescription);
                sb.append("\n");
                sb.append("\n");

            }
            else if(creatureEffect==CreatureEffect.Taunt)
            {
                effectDescription = "You cannot target minions without taunt while in play";
                sb.append(effectString);
                sb.append("\n");
                sb.append(effectDescription);
                sb.append("\n");
                sb.append("\n");
            }

            //DEATH EFFECT
            if(creatureEffect==CreatureEffect.Gain_Life)
            {
                effectDescription = "When destroyed, gain " + getPlayCost() + " life";
                sb.append(effectString);
                sb.append("\n");
                sb.append(effectDescription);
            }
            
            if(creatureEffect==CreatureEffect.NONE && creatureEffect == CreatureEffect.NONE)
            {
                sb.append("Basic Minion");
                sb.append("\n");
                sb.append("This minion has no abilities");    
            }
            textBox.setText(sb.toString());
        }
    }
     
    @Override
    public void paintComponent(Graphics g) 
    {
        super.paintComponent(g);
        Graphics2D graphics = (Graphics2D) g;
        
        this.setForeground(Color.black);
        Color strokeColor = getForeground();
        
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
       
        //draw shadow
        if(dropShadow){

            graphics.setColor(shadowColor);
            graphics.fillRoundRect(shadowOffset,shadowOffset,width-strokeSize-shadowOffset,height-strokeSize-shadowOffset,arcSize,arcSize);
        } 
        
        //draw inside
        graphics.setColor(backgroundColor);
        graphics.fillRoundRect(0,0,width-shadowGap,height-shadowGap,arcSize,arcSize);
        
        //draw outline
        graphics.setColor(strokeColor);
        graphics.setStroke(new BasicStroke(strokeSize));
        graphics.drawRoundRect(0,0,width-shadowGap,height-shadowGap,arcSize,arcSize);
    }
    
    @Override
    public boolean equals(Object object)
    {
        if(object instanceof Card)
        {
            Card other = (Card) object;
            if(this.cardID == other.getCardID()) {
                return true;
            }
        }
        return false;
    }
        
    public CardLocation getCardLocation()
    {
        return location;
    }   
    
    public void getCardSize()
    {
        int w = width-shadowGap;
        int h = height-shadowGap;
        
        System.out.println(innerPanel.getHeight() + ", " + innerPanel.getWidth());
    }
    
    public void showTauntSymbol()
    {
        //bodyBox.setOpaque(false);
        bodyBox.remove(textBox);
        JLabel iconLabel = new JLabel();
        iconLabel.setIcon(new ImageIcon(
                playerHand.getGameWindow().getImageFromCache(9966)
                        //.getScaledInstance(bodyBox.getHeight()-5, bodyBox.getHeight()-5, Image.SCALE_DEFAULT)
                ));

        
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run()
            {
                bodyBox.add(iconLabel);  
                playerHand.getGameWindow().playSound("tauntActivated");
                repaint();
                revalidate();
            }
        };
        timer.schedule(task, 1000); 
    }
    
    
    
    
    
    
}
