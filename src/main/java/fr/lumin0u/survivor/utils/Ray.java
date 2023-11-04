package fr.lumin0u.survivor.utils;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Ray {
    protected List<Location> points = new ArrayList();
    protected boolean pointsOk;
    protected Block end;
    protected Location start;
    protected Vector increase;
    protected double length;
    protected double accuracy;

    public Ray(Location start, Vector increase, double length, double accuracy) {
        this.start = start;
        this.increase = increase;
        this.length = length;
        this.accuracy = accuracy;
        this.pointsOk = false;
        this.calculate();
    }

    public void calculate() {
        Location point = this.start.clone();
        Vector increase = this.increase;
        double m = increase.length() / increase.clone().normalize().length();
        Random r = new Random();
        increase.setX(increase.getX() + (double)(r.nextBoolean() ? -1 : 1) * r.nextDouble() * this.accuracy / 50.0D);
        increase.setY(increase.getY() + (double)(r.nextBoolean() ? -1 : 1) * r.nextDouble() * this.accuracy / 50.0D);
        increase.setZ(increase.getZ() + (double)(r.nextBoolean() ? -1 : 1) * r.nextDouble() * this.accuracy / 50.0D);
        increase.normalize().multiply(m);
        if (increase.length() > 0.0D) {
            Location wantedEndPoint = this.start.clone().add(increase.clone().normalize().multiply(this.length));
            
            RayTraceResult collisionResult = TransparentUtils.collisionBetween(this.start, wantedEndPoint);
            
            Location endPoint = collisionResult == null ? wantedEndPoint : collisionResult.getHitPosition().toLocation(point.getWorld());

            for(int i = 0; (double)i < this.start.distance(endPoint) * (1.0D / increase.length()); ++i) {
                point.add(increase);
                increase.setY(increase.getY());
                this.points.add(point.clone());
            }

            if (collisionResult != null) {
                this.end = collisionResult.getHitBlock();
            }
        }

        this.pointsOk = true;
    }

    public List<Location> getPoints() {
        if (!this.pointsOk) {
            points.clear();
            this.calculate();
        }

        return this.points;
    }

    public Block getEnd() {
        return this.end;
    }

    public Vector getIncrease() {
        return this.increase.clone();
    }

    public Location getStart() {
        return this.start.clone();
    }

    public double getLength() {
        return this.length;
    }

    public double getAccuracy() {
        return this.accuracy;
    }

    public void setStart(Location start) {
        this.start = start;
        this.pointsOk = false;
    }

    public void setIncrease(Vector increase) {
        this.increase = increase;
        this.pointsOk = false;
    }

    public void setLength(double length) {
        this.length = length;
        this.pointsOk = false;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
        this.pointsOk = false;
    }
}
