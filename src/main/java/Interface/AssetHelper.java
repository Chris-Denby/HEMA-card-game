/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interface;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author chris
 */
public class AssetHelper 
{
    private static HashMap<Integer,Image> imageCache = new HashMap<Integer,Image>();
    
    public static Image getImageFromCache(int imageID)
    {
        return imageCache.get(imageID);
    }
    
    public static void setImageCache(HashMap<Integer,Image> map)
    {
        imageCache = map;
    }
    
    
    public void playSound(String soundFileName)
    {
        AudioInputStream audioInputStream = null;
        final Clip clip;
        try {
            String soundName = "sounds/"+soundFileName+".wav";
            audioInputStream = AudioSystem.getAudioInputStream(new File(soundName).getAbsoluteFile());
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
            
            clip.addLineListener(new LineListener() {
                public void update(LineEvent myLineEvent) {
                    if(myLineEvent.getType() == LineEvent.Type.STOP){
                        clip.close();
                    }
                }
            });
            
        } 
        catch (UnsupportedAudioFileException ex) {
            Logger.getLogger(PlayArea.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PlayArea.class.getName()).log(Level.SEVERE, null, ex);
        } catch (LineUnavailableException ex) {
            Logger.getLogger(PlayArea.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                audioInputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(PlayArea.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }    
}
