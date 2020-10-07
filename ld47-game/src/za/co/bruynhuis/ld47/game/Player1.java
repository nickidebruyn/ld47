/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.co.bruynhuis.ld47.game;

import com.bruynhuis.galago.games.physics.PhysicsGame;
import com.bruynhuis.galago.games.physics.PhysicsPlayer;
import com.bruynhuis.galago.util.SpatialUtils;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResults;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 *
 * @author NideBruyn
 */
public class Player1 extends PhysicsPlayer implements PhysicsTickListener {

    private Spatial model;
    private RigidBodyControl rbc;
    private CollisionResults collisionResults;
    private Ray ray;
    private Vector3f rayDirection = new Vector3f(0, -1, 0);
    private boolean accelarate = false;
    private boolean left = false;
    private boolean right = false;
    private Vector3f directionalForce = new Vector3f(0, 0, 0);
    private Node followPoint;

    public Player1(PhysicsGame physicsGame) {
        super(physicsGame);
    }

    @Override
    protected void init() {

        model = game.getBaseApplication().getAssetManager().loadModel("Models/player/ship1.j3o");
        playerNode.attachChild(model);
        
        followPoint = new Node("follow-point");
        SpatialUtils.addSphere(followPoint, 20, 20, 1);
        SpatialUtils.addColor(followPoint, ColorRGBA.Green, true);
        playerNode.attachChild(followPoint);
        followPoint.move(0, 5, -10);

//        rbc = new RigidBodyControl(CollisionShapeFactory.createMeshShape(model), 1);
        rbc = new RigidBodyControl(new BoxCollisionShape(new Vector3f(3, 0.35f, 1)), 1);
        playerNode.addControl(rbc);

        game.getBaseApplication().getBulletAppState().getPhysicsSpace().add(rbc);
        game.getBaseApplication().getBulletAppState().getPhysicsSpace().addTickListener(this);
//        game.getBaseApplication().getBulletAppState().getPhysicsSpace().addTickListener(new RigidBodyRotationLock(rbc, new Vector3f(0, 1, 0)));

        log("Start pos = " + game.getStartPosition());
        rbc.setPhysicsLocation(game.getStartPosition());
        rbc.setGravity(new Vector3f(0, -20, 0));
        rbc.setFriction(0);
        rbc.setRestitution(0);

    }

    @Override
    public Vector3f getPosition() {
        return rbc.getPhysicsLocation();
    }

    @Override
    public void doDie() {

    }

    protected float distanceFromSurface() {
        float distance = 100;
        collisionResults = new CollisionResults();

        // Aim the ray from camera location in camera direction
        // (assuming crosshairs in center of screen).
        ray = new Ray(getPosition(), rayDirection);

        // Collect intersections between ray and all nodes in results list.
        ((Game) game).getSceneNode().collideWith(ray, collisionResults);

        if (collisionResults.size() > 0) {
            distance = collisionResults.getClosestCollision().getDistance();
        }

        return distance;
    }

    @Override
    public void prePhysicsTick(PhysicsSpace space, float tpf) {

        if (left) {
            rbc.setAngularVelocity(new Vector3f(0, tpf*50, 0));
//            SpatialUtils.rotate(playerNode, 0, tpf * 30, 0);
//            rbc.setAngularVelocity(new Vector3f(0, 0, 0));
        } else if (right) {
            rbc.setAngularVelocity(new Vector3f(0, -tpf*50, 0));
//            SpatialUtils.rotate(playerNode, 0, -tpf * 30, 0);
//            rbc.setAngularVelocity(new Vector3f(0, 0, 0));
        } else {
            rbc.setAngularVelocity(new Vector3f(0, 0, 0));
        }

        directionalForce.set(0, 0, 0);
        if (accelarate) {
            Vector3f dir = rbc.getPhysicsRotation().getRotationColumn(2).normalize().mult(20);
            directionalForce.setX(dir.x);
            directionalForce.setZ(dir.z);
        }

        float dist = distanceFromSurface();
        if (dist < 3f) {
            directionalForce.setY(25 * (3f - dist));
        }

        rbc.applyCentralForce(directionalForce);

    }

    @Override
    public void physicsTick(PhysicsSpace space, float tpf) {
//        rbc.clearForces();
        rbc.setAngularVelocity(rbc.getAngularVelocity().multLocal(0, 1, 0));
        rbc.setAngularFactor(new Vector3f(0, 1, 0));

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

    public Node getFollowPoint() {
        return followPoint;
    }

}
