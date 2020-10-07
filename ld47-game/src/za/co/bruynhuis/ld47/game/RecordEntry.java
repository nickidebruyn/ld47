/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.co.bruynhuis.ld47.game;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

/**
 *
 * @author NideBruyn
 */
public class RecordEntry {
    
    private Vector3f location;
    private Quaternion rotation;
    private float ellapsedTime;

    public Vector3f getLocation() {
        return location;
    }

    public void setLocation(Vector3f location) {
        this.location = location;
    }

    public Quaternion getRotation() {
        return rotation;
    }

    public void setRotation(Quaternion rotation) {
        this.rotation = rotation;
    }

    public float getEllapsedTime() {
        return ellapsedTime;
    }

    public void setEllapsedTime(float ellapsedTime) {
        this.ellapsedTime = ellapsedTime;
    }

    @Override
    public String toString() {
        return "RecordEntry{" + "location=" + location + ", rotation=" + rotation + ", ellapsedTime=" + ellapsedTime + '}';
    }
    
    
    
}
