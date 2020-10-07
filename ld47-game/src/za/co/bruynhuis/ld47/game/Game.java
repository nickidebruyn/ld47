/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.co.bruynhuis.ld47.game;

import com.bruynhuis.galago.app.Base3DApplication;
import com.bruynhuis.galago.control.RotationControl;
import com.bruynhuis.galago.games.physics.PhysicsGame;
import static com.bruynhuis.galago.games.physics.PhysicsGame.TYPE_OBSTACLE;
import static com.bruynhuis.galago.games.physics.PhysicsGame.TYPE_STATIC;
import com.bruynhuis.galago.games.physics.PhysicsPlayer;
import com.bruynhuis.galago.util.SpatialUtils;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Torus;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import za.co.bruynhuis.ld47.control.PickupControl;

/**
 *
 * @author NideBruyn
 */
public class Game extends PhysicsGame {

    private static final String TYPE = "type";
    private static final String MARKER = "marker";
    private static final String PICKUP = "pickup";
    private static final String TRACK = "track";
    private static final String START = "start";
    private static final String TEXT = "text";

    private Node sceneNode;
    private Stack<Vector3f> waypoints;
    private Stack<Vector3f> pickuppoints;
    private int laps = 10;
    private Spatial playerTracker;
    private int pickupCount = 0;

    public Game(Base3DApplication baseApplication, Node rootNode) {
        super(baseApplication, rootNode);
    }

    @Override
    public void init() {

        sceneNode = (Node) baseApplication.getAssetManager().loadModel("Scenes/level1.j3o");
        levelNode.attachChild(sceneNode);

        waypoints = new Stack<>();
        pickuppoints = new Stack<>();
        parseScene();
        Collections.reverse(waypoints);
        Collections.reverse(pickuppoints);

        playerTracker = SpatialUtils.addBox(levelNode, 0.2f, 0.05f, 0.075f);
        playerTracker.setName(TYPE_PLAYER);
        playerTracker.setCullHint(Spatial.CullHint.Always);
        SpatialUtils.addColor(playerTracker, ColorRGBA.Orange, true);
        RigidBodyControl rbc = SpatialUtils.addMass(playerTracker, 1);
        rbc.setSleepingThresholds(0, 0);
        rbc.setAngularFactor(new Vector3f(0, 1, 0));

        loadPickups();

//        startPosition = new Vector3f(0, -5, 0);
//        initLight(ColorRGBA.DarkGray, new Vector3f(-0.085938168f, -0.8627f, 0.9911459f));
    }

    public void loadPickups() {
        
        if (pickupCount > 0) {
            return;
        }
        
        if (pickuppoints.size() > 3) {

            //Load 3 pickups at a time
            for (int i = 0; i < 3; i++) {
                Vector3f pickup = pickuppoints.get(FastMath.nextRandomInt(0, pickuppoints.size() - 1));

                Torus t = new Torus(40, 40, 0.05f, 0.25f);
                Geometry p = new Geometry(TYPE_PICKUP, t);
                p.setMaterial(baseApplication.getAssetManager().loadMaterial("Materials/ring.j3m"));
                p.setLocalTranslation(pickup.clone().add(0, 0.5f, 0));
                p.addControl(new RotationControl(new Vector3f(0, 200, 0)));
                p.addControl(new PickupControl(this));

                createPickup(p);
                pickupCount ++;
            }

        }

    }
    
    public void removePickup() {
        pickupCount --;
    }

    private void parseScene() {
        SceneGraphVisitor sgv = new SceneGraphVisitor() {
            public void visit(Spatial spatial) {
                if (spatial.getUserData(TYPE) != null && spatial.getUserData(TYPE).equals(MARKER)) {
//                    log("Waypoint found: " + spatial.getWorldTranslation());
                    waypoints.push(spatial.getWorldTranslation().clone());
                    spatial.removeFromParent();

                } else if (spatial.getUserData(TYPE) != null && spatial.getUserData(TYPE).equals(PICKUP)) {
//                    log("Pickup found: " + spatial.getWorldTranslation());
                    pickuppoints.push(spatial.getWorldTranslation().clone());
                    spatial.removeFromParent();

                } else if (spatial.getUserData(TYPE) != null && spatial.getUserData(TYPE).equals(TYPE_OBSTACLE)) {
//                    log("Obstacle found");
                    RigidBodyControl trackRigidBodyControl = new RigidBodyControl(CollisionShapeFactory.createMeshShape(spatial), 0);
                    spatial.addControl(trackRigidBodyControl);
                    baseApplication.getBulletAppState().getPhysicsSpace().add(trackRigidBodyControl);
                    trackRigidBodyControl.setFriction(0);
                    trackRigidBodyControl.getSpatial().setName(TYPE_OBSTACLE);

                } else if (spatial.getUserData(TYPE) != null && spatial.getUserData(TYPE).equals(TYPE_STATIC)) {
//                    log("Static found");
                    RigidBodyControl trackRigidBodyControl = new RigidBodyControl(CollisionShapeFactory.createMeshShape(spatial), 0);
                    spatial.addControl(trackRigidBodyControl);
                    baseApplication.getBulletAppState().getPhysicsSpace().add(trackRigidBodyControl);
                    trackRigidBodyControl.setFriction(0);
                    trackRigidBodyControl.getSpatial().setName(TYPE_STATIC);

                } else if (spatial.getUserData(TYPE) != null && spatial.getUserData(TYPE).equals(TEXT)) {
                    //Add text
                    BitmapText text = new BitmapText(baseApplication.getBitmapFont());
                    text.setBox(new Rectangle(-2, -0.1f, 4, 0.2f));
                    text.setAlignment(BitmapFont.Align.Center);
                    text.setText("" + spatial.getUserData(TEXT));
                    text.setSize(0.5f);
                    text.setColor(new ColorRGBA(0.1f, 0.1f, 0.1f, 1));
                    text.setLocalTranslation(0.f, 0.5f, -0.1f);
                    text.setQueueBucket(RenderQueue.Bucket.Translucent);
                    text.setShadowMode(RenderQueue.ShadowMode.Off);
                    SpatialUtils.rotateTo(text, 0, 180, 0);

//                    BillboardControl billboardControl = new BillboardControl();
//                    billboardControl.setAlignment(BillboardControl.Alignment.Screen);
//                    text.addControl(billboardControl);
                    ((Node) spatial).attachChild(text);
                }

            }
        };

        rootNode.depthFirstTraversal(sgv);
    }

    public Stack<Vector3f> getCopyOfWaypoints() {
        //TODO: We need to make a copy of the waypoints

        return (Stack<Vector3f>) waypoints.clone();
    }

    public Node getSceneNode() {
        return sceneNode;
    }

    public int getTotalLaps() {
        return laps;
    }

    @Override
    public void start(PhysicsPlayer physicsPlayer) {
        super.start(physicsPlayer); //To change body of generated methods, choose Tools | Templates.

        playerTracker.getControl(RigidBodyControl.class).setPhysicsLocation(player.getPosition());
    }

    public Spatial getPlayerTracker() {
        return playerTracker;
    }

    @Override
    public void close() {
        super.close(); //To change body of generated methods, choose Tools | Templates.

    }

}
