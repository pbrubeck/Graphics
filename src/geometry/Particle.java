package geometry;

import java.awt.Color;
import java.util.function.DoubleFunction;

public class Particle extends Shape implements Runnable{
    @FunctionalInterface
    public interface VectorFunction extends DoubleFunction<Vector>, java.io.Serializable{
        @Override
        Vector apply(double t);
    }
    
    private static final long timeout=10;
    private double mass, charge;
    private double time=0;
    private Vector velocity;
    private VectorFunction acceleration;
    private Runnable spin;
    
    public Particle(Shape s, Vector v, VectorFunction f) {
        super(s.vertex, s.faces);
        velocity=v;
        acceleration=f;
    }
    
    @Override
    public synchronized void run(){
        while(true){
            double dt=timeout/1E3;
            velocity.translate(getAcceleration().scale(dt));
            translate(Vector.multiply(dt, velocity));
            if(spin!=null){
                spin.run();
            }
            try{
                wait(timeout);
            }catch(InterruptedException ex){
                return;
            }
            time+=dt;
        }
    }
    
    public void setMass(double m){
        mass=m;
    }
    public void setCharge(double c){
        charge=c;
    }
    public void setSpin(Runnable s){
        spin=s;
    }
    public void setVelocity(Vector v){
        velocity=v;
    }
    public void setAcceleration(VectorFunction a){
        acceleration=a;
    }
    public void addAcceleration(VectorFunction a){
        if(acceleration==null){
            setAcceleration(a);
        }else{
            final VectorFunction u=acceleration;
            setAcceleration(t -> Vector.add(u.apply(t), a.apply(t))); 
        }
    }
    
    public double getMass(){
        return mass;
    }
    public double getCharge(){
        return charge;
    }
    public Vector getVelocity(){
        return velocity;
    }
    public Vector getAcceleration(){
        if(acceleration==null){
            return new Vector(0,0,0);
        }
        return acceleration.apply(time);
    }
    
    public void gravity(Particle p){
        addAcceleration(t -> {
            Vector distance=Vector.subtract(p.getPosition(), getPosition());
            double r=distance.getLength();
            return distance.scale(p.getMass()/(r*r*r));
        });
    }
    public void gravity(Particle... particles){
        for(Particle p: particles){
            if(p!=this){
                gravity(p);
            }
        }
    }
    public void orbit(Particle p, Vector axis){
        Vector r=Vector.subtract(p.getPosition(), getPosition());
        Vector vel=Vector.crossP(axis, r).normalize().scale(Math.sqrt(p.getMass()/r.getLength()));
        setVelocity(Vector.add(p.getVelocity(), vel));
        gravity(p);
    }
    
    public static Particle[] solarSystem(){
        int n=30;
        double r1=1000, r2=200, r3=1400, r4=1800;
        
        Shape s1=ShapeFactory.sphere(n, 200);
        s1.setColor(Color.yellow);
        Shape s2=ShapeFactory.sphere(n, 100);
        s2.setColor(Color.blue);
        Shape s3=ShapeFactory.sphere(n, 27);
        s3.setColor(Color.white);
        Shape s4=ShapeFactory.sphere(n, 53);
        s4.setColor(Color.red);
        Shape s5=ShapeFactory.sphere(n, 120);
        s5.setColor(Color.orange);
        
        Particle sun=new Particle(s1, new Vector(0,0,0), null);
        Particle earth=new Particle(s2, null, null);
        Particle moon=new Particle(s3, null, null);
        Particle mars=new Particle(s4, null, null);
        Particle saturn=new Particle(s5, null, null);
        
        sun.setMass(4E8);
        earth.setMass(4E8);
        moon.setMass(100);
        mars.setMass(4E4);
        saturn.setMass(4E8);
        
        sun.setSpin(() -> sun.rotate(0, 0, Math.PI/360));
        
        earth.setPosition(new Vector(0, r1, 0));
        earth.orbit(sun, new Vector(1,0,1));
        earth.setSpin(() -> earth.rotate(0, 0, Math.PI/360));
        
        moon.setPosition(Vector.add(earth.getPosition(), new Vector(0, r2, 0)));
        moon.gravity(sun);
        moon.orbit(earth, new Vector(0,0,1));
        moon.setSpin(() -> moon.rotate(0, 0, Math.PI/180));
        
        mars.setPosition(new Vector(0, r3, 0));
        mars.orbit(sun, new Vector(-0.2,0,1));
        mars.setSpin(() -> mars.rotate(0, 0, Math.PI/360));
        
        saturn.setPosition(new Vector(0, r4, 0));
        saturn.orbit(sun, new Vector(-0.2,0,1));
        saturn.setSpin(() -> saturn.rotate(0, 0, Math.PI/360));
        
        return new Particle[]{sun, earth, moon, mars, saturn};
    }
}