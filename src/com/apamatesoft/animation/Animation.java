/* NOTA
 * - CONTROLES:
 *   pause: pausa la animación
 *   stop: termina la animación
 *   restart: si la animacion se encuentra pausada la inicia. puedera ser el mismo start. cambiar el nombre start por play.
 * - los valores por default setearlos en algun metodo.
 */
package com.apamatesoft.animation;

public class Animation implements Runnable {
    
    //<editor-fold defaultstate="collapsed" desc="CONSTANTS">
    public static final byte LINEAL = 0;
    public static final byte ACCELERATED = 1;
    public static final byte CUSHION = 2;
    public static final byte REBOUND = 3;
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="ATTRIBUTES">
    private int fps = 60; // <--------------------------------------------------------------------- Frame rate default 60fps
    private int steps; // <------------------------------------------------------------------------ Number steps
    private int duration; // <--------------------------------------------------------------------- Duration in milliseconds
    private int periode; // <---------------------------------------------------------------------- Time between frame
    private int i;
    private long newTime, oldTime;
    private UpdateListener updateListener;
    private EndListener endListener;
    private Event event = new Event();
    private byte interpolation = LINEAL;
    private int dt;
    private int t;
    private boolean live = false;
    //</editor-fold>
    
    public Animation(int duration) {
        this.duration = duration;
        calculate();
    }
    
    public Animation(Builder builder) {
        duration = builder.duration;
        fps = builder.fps;
        interpolation = builder.interpolation;
        updateListener = builder.updateListener;
        endListener = builder.endListener;
        calculate();
    }
    
    //<editor-fold defaultstate="collapsed" desc="SETTERS">
    public void setFps(int fps) {
        this.fps = fps;
        calculate();
    }
    
    public void setInterpolation(byte interpolation) {
        this.interpolation = interpolation;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="GETTERS">
    public int getDuration() {
        return duration;
    }
    
    public int getFps() {
        return fps;
    }
    
    public int getSteps() {
        return steps;
    }
    
    public int getPeriode() {
        return periode;
    }
    
    public byte getInterpolation() {
        return interpolation;
    }
    
    public boolean isAlive() {
        return live;
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="CONTROLS">
    public void start() {
        reset();
        new Thread(this).start();
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="PRIVATE">
    private void calculate() {
        periode = (int) Math.round(1000/(double)fps);
        steps = (int) Math.round(duration/(double)periode);
    }
    
    private void update() {
        oldTime = newTime;
        newTime = System.currentTimeMillis();
        dt = (int) (newTime-oldTime);
        t += dt;
    }
    // System.out.println(">>: i: "+i+", dt: "+dt+", t: "+t);
    
    private void reset() {
        newTime = System.currentTimeMillis();
        t = 0;
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="EVENTS">
    public void onUpdate(UpdateListener updateListener) {
        this.updateListener = updateListener;
    }
    
    public void onEnd(EndListener endListener) {
        this.endListener = endListener;
    }
    //</editor-fold>
    
    @Override
    public void run() {
        try {
            live = true;
            for (i=0; i<=steps; i++) {
                update();
                if (updateListener!=null) updateListener.action(event);
                Thread.sleep(periode);
            }
            if (endListener!=null) endListener.action();
            live = false;
        } catch (Exception e) {
            
        }
        
    }
    
    //<editor-fold defaultstate="collapsed" desc="FUNCTIONAL INTERFACE">
    @FunctionalInterface
    public interface UpdateListener {
        void action(Event event);
    }
    
    @FunctionalInterface
    public interface EndListener {
        void action();
    }
    //</editor-fold>
    
    public class Event {
            
        public int delta(byte interpolation, int size) {
            
            if (i>=steps || t>=duration) t = duration;
            double tn = t/(double)duration;
            double x;
            
            switch (interpolation) {
                
                case ACCELERATED:
                    x = Math.pow(tn, 2);
                    break;
                
                case CUSHION:
                    x = -Math.pow(1-tn, 2)+1;
                    break;

                case REBOUND:
                    x = (tn>=0.5) ? (Math.pow(tn-0.75, 2)/0.25)+0.75 : Math.pow(tn*2, 2);
                    break;
                
                default: 
                    x = tn;
                    break;
                    
            }
            
            return (int) Math.round(x*size);

        }
        
        public int delta(int size) {
            return delta(interpolation, size);
        }
        
    }
    
    public static class Builder {
        
        private int duration;
        private byte interpolation;
        private int fps;
        private UpdateListener updateListener;
        private EndListener endListener;
        
        public Builder(int duration) {
            this.duration = duration;
            interpolation = LINEAL;
            fps = 60;
        }
        
        public Builder fps(int fps) {
            this.fps = fps;
            return this;
        }
        
        public Builder interpolation(byte interpolation) {
            this.interpolation = interpolation;
            return this;
        }
        
        public Builder onUpdate(UpdateListener updateListener) {
            this.updateListener = updateListener;
            return this;
        }
        
        public Builder onEnd(EndListener endListener) {
            this.endListener = endListener;
            return this;
        }
        
        public Animation build() {
            return new Animation(this);
        }
        
        public void start() {
            new Animation(this).start();
        }
        
    }
 
}