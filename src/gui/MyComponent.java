package gui;

import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

/*
 * @author Pablo Brubeck
 */
public class MyComponent{
    public static class MyRadioMenuItem extends JRadioButtonMenuItem{
        public MyRadioMenuItem(String label, String keyStroke, Object target, String fieldName){
            super(new AbstractAction(label){
                {
                    if(keyStroke==null? false: !keyStroke.isEmpty()){
                        putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getAWTKeyStroke(keyStroke));
                    }
                }
                @Override
                public void actionPerformed(ActionEvent e){
                    try{
                        Field f=target.getClass().getDeclaredField(fieldName);
                        f.setBoolean(target, !f.getBoolean(target));
                        synchronized(target){
                            target.notifyAll();
                        }
                    }catch(NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex){
                        Logger.getLogger(target.getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            try {
                setSelected(target.getClass().getDeclaredField(fieldName).getBoolean(target));
            } catch (NoSuchFieldException | SecurityException | IllegalAccessException ex) {
                Logger.getLogger(target.getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public static class MyMenuItem extends JMenuItem{
        public MyMenuItem(String label, String keyStroke, Runnable r){
            super(new AbstractAction(label){
                {
                    if(keyStroke==null? false: !keyStroke.isEmpty()){
                        putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getAWTKeyStroke(keyStroke));
                    }
                }
                @Override
                public void actionPerformed(ActionEvent e) {
                    r.run();
                }
            });
        }
    }
    public static class MyMenu extends JMenu{
        public MyMenu(String s, JMenuItem... options) {
            super(s);
            for(JMenuItem option : options){
                add(option);
            }
        }
    }
    public static class MyMenuBar extends JMenuBar {
        public MyMenuBar(String[] labels, JMenuItem[][] items){
            for(int i=0; i<labels.length; i++){
                add(new MyMenu(labels[i], items[i]));
            }
        }
    }
}