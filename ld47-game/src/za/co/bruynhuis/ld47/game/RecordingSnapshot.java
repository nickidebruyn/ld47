/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.co.bruynhuis.ld47.game;

import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author NideBruyn
 */
public class RecordingSnapshot {
    
    private int retryNumber;
    private List<RecordEntry> entries = new ArrayList<>();
    private Spatial vehicle;

    public int getRetryNumber() {
        return retryNumber;
    }

    public void setRetryNumber(int retryNumber) {
        this.retryNumber = retryNumber;
    }

    public List<RecordEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<RecordEntry> entries) {
        this.entries = entries;
    }

    public Spatial getVehicle() {
        return vehicle;
    }

    public void setVehicle(Spatial vehicle) {
        this.vehicle = vehicle;
    }
    
    
    
}
