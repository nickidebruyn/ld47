/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.co.bruynhuis.ld47.game;

import com.bruynhuis.galago.util.Debug;
import com.bruynhuis.galago.util.Timer;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author NideBruyn
 */
public class PlayerRecorder extends AbstractControl {
    
    private boolean record = false;
    private Timer recordTimer = new Timer(60);
    private List<RecordEntry> entries;
    private long timeInMilli;

    @Override
    protected void controlUpdate(float tpf) {
        
        if (record) {
            
            recordTimer.update(tpf);
            
            if (recordTimer.finished()) {
                recordEntry();
                recordTimer.reset();
            }
            
        }
        
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        
    }

    private void recordEntry() {        
        RecordEntry recordEntry = new RecordEntry();
//        if (entries.size() == 0) {
//            recordEntry.setLocation(spatial.getWorldTranslation().clone().add(FastMath.nextRandomInt(-10, 10)*0.2f, 0, 0));
//        } else {
            recordEntry.setLocation(spatial.getWorldTranslation().clone());
//        }
        
        recordEntry.setRotation(spatial.getWorldRotation().clone());
        float ellapsedTime = System.currentTimeMillis() - timeInMilli;
        timeInMilli = System.currentTimeMillis();
        recordEntry.setEllapsedTime(ellapsedTime/1000);
        entries.add(recordEntry);
        
//        Debug.log("Record entry: " + recordEntry.toString());
        
    }
    
    public void start() {
        entries = new ArrayList<>();        
        record = true;
        recordTimer.start();
        timeInMilli = System.currentTimeMillis();
        recordEntry();
    }
    
    public void stop() {
        recordEntry();
        record = false;
        recordTimer.stop();
    }

    public List<RecordEntry> getEntries() {
        return entries;
    }
    
}
