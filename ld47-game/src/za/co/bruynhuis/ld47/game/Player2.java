/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.co.bruynhuis.ld47.game;

import com.bruynhuis.galago.games.physics.PhysicsGame;
import com.bruynhuis.galago.games.physics.PhysicsPlayer;
import com.bruynhuis.galago.util.Debug;
import com.bruynhuis.galago.util.SpatialUtils;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import java.util.Stack;

/**
 *
 * @author NideBruyn
 */
public class Player2 extends PhysicsPlayer implements PhysicsTickListener, PhysicsCollisionListener {

    private Spatial model;
    private RigidBodyControl rbc;
    private boolean accelarate = false;
    private boolean left = false;
    private boolean right = false;
    private Stack<Vector3f> waypoints;
    private float waypointReachDistance = 1f;
    private Vector3f direction;
    private Vector3f nextPosition = new Vector3f(0, 0, 0);
    private float speed = 0;
    private float accel = 0.1f;
    private float targetSpeed = 10;
    private float maxSpeed = 15;
    private float turnSpeed = 0.16f;
    private Node tempNode;
    private int lapCount = 1;
    private boolean gameEnded = false;
    private float playerHeight = 0.5f;
    private float sideMoveSpeed = 2;
    private boolean crashed = false;

    public Player2(PhysicsGame physicsGame) {
        super(physicsGame);
    }

    @Override
    protected void init() {

        model = game.getBaseApplication().getAssetManager().loadModel("Models/player/ship1.j3o");
        playerNode.attachChild(model);

//        followPoint = new Node("follow-point");
//        SpatialUtils.addSphere(followPoint, 20, 20, 1);
//        SpatialUtils.addColor(followPoint, ColorRGBA.Green, true);
//        playerNode.attachChild(followPoint);
//        followPoint.move(0, 5, -10);
//        rbc = new RigidBodyControl(CollisionShapeFactory.createMeshShape(model), 10);
        rbc = new RigidBodyControl(new BoxCollisionShape(new Vector3f(0.3f, 0.06f, 0.1f)), 1);
        playerNode.addControl(rbc);

        game.getBaseApplication().getBulletAppState().getPhysicsSpace().add(rbc);
        game.getBaseApplication().getBulletAppState().getPhysicsSpace().addTickListener(this);
        game.getBaseApplication().getBulletAppState().getPhysicsSpace().addCollisionListener(this);

        tempNode = new Node();

        waypoints = ((Game) game).getCopyOfWaypoints();
        rbc.setPhysicsLocation(waypoints.peek().add(0, playerHeight, 0));
        SpatialUtils.rotate(playerNode, 0, -90, 0);

//        tempNode.setLocalTranslation(getPosition().clone());
//        tempNode.setLocalRotation(rbc.getPhysicsRotation().clone());
//        tempNode.lookAt(waypoints.peek().add(0, playerHeight, 0), Vector3f.UNIT_Y);        
//        rbc.setPhysicsRotation(tempNode.getLocalRotation().clone());
//        rbc.lookAt(waypoints.get(waypoints.size() - 2), Vector3f.UNIT_Y);
        playerNode.addControl(new AbstractControl() {
            @Override
            protected void controlUpdate(float tpf) {

                if ((game.isStarted() && !game.isPaused()) || gameEnded) {

                    //Adjust the speed
                    if (speed < targetSpeed) {
                        speed += accel;

                    } else if (speed > targetSpeed) {
                        speed -= accel;

                    }

                    if (gameEnded && waypoints.isEmpty()) {
                        waypoints = ((Game) game).getCopyOfWaypoints();
                    }

                    if (!crashed) {
                        moveShip(tpf);
                    }
                    
                }

            }

            @Override
            protected void controlRender(RenderManager rm, ViewPort vp) {

            }
        });

    }

    private void moveShip(float tpf) {
        if (getPosition().distance(waypoints.peek().add(0, playerHeight, 0)) > waypointReachDistance) {

            tempNode.setLocalTranslation(getPosition().clone());
            tempNode.setLocalRotation(rbc.getPhysicsRotation().clone());
            tempNode.lookAt(waypoints.peek().add(0, playerHeight, 0), Vector3f.UNIT_Y);

            Quaternion q = rbc.getPhysicsRotation().clone();
            q.slerp(tempNode.getLocalRotation(), turnSpeed);
            rbc.setPhysicsRotation(q);

            //Move in the direction the player is facing
            direction = rbc.getPhysicsRotation().getRotationColumn(2).normalize();
            nextPosition = rbc.getPhysicsLocation().add(direction.mult(tpf * speed));

            direction = rbc.getPhysicsRotation().getRotationColumn(0).normalize();

            if (left) {
                log("Move left");
                nextPosition = nextPosition.add(direction.mult(tpf * sideMoveSpeed));
            } else if (right) {
                log("Move right");
                nextPosition = nextPosition.add(direction.mult(tpf * -sideMoveSpeed));
            }

            rbc.setPhysicsLocation(nextPosition);

        } else {
            waypoints.pop();
//            Debug.log("Waypoint reached, next point = " + waypoints.peek());

            if (waypoints.isEmpty()) {
                Debug.log("No more waypoints");
//                            if (((Game) game).getTotalLaps() == lapCount && !gameEnded) {
//                                ((Game) game).setWinner(Player.this);
//                                game.doGameCompleted();
//
//                            } else {
//                                waypoints = ((Game) game).getCopyOfWaypoints();
//                                lapCount++;
//                                addScore(1);
//                            }

                waypoints = ((Game) game).getCopyOfWaypoints();
                lapCount++;
                addScore(1);

            }

        }
    }

    public void increaseSpeed() {
        this.targetSpeed += 0.2f;
        if (this.targetSpeed > this.maxSpeed) {
            this.targetSpeed = this.maxSpeed;
        }
    }

    public void decreaseSpeed() {
        this.targetSpeed -= 0.2f;
        if (this.targetSpeed < 0) {
            this.targetSpeed = 0;
        }
    }

    @Override
    public Vector3f getPosition() {
        return rbc.getPhysicsLocation();
    }

    @Override
    public void doDie() {

    }

    @Override
    public void start() {
        rbc.setGravity(new Vector3f(0, 0, 0));
        rbc.setFriction(0);
        rbc.setRestitution(0);
        rbc.setSleepingThresholds(0.0f, 0.0f);
//        rbc.setKinematicSpatial(true);
    }

    @Override
    public void prePhysicsTick(PhysicsSpace space, float tpf) {

    }

    @Override
    public void physicsTick(PhysicsSpace space, float tpf) {

//        rbc.clearForces();
//        rbc.setAngularVelocity(rbc.getAngularVelocity().multLocal(0, 1, 0));
//        rbc.setAngularFactor(new Vector3f(0, 1, 0));
    }

    public boolean isAccelarate() {
        return accelarate;
    }

    public void setAccelarate(boolean accelarate) {
        this.accelarate = accelarate;
    }

    public boolean isLeft() {
        return left;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public boolean isRight() {
        return right;
    }

    public void setRight(boolean right) {
        this.right = right;
    }

    @Override
    public void collision(PhysicsCollisionEvent event) {
        log("Collision: " + event.getNodeB().getName());
        if (event.getNodeB().getName().contains("track")) {
            crashed = true;
            rbc.setGravity(new Vector3f(0, -10, 0));
        }
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
