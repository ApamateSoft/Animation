/**
 * - LOOPS INFINITO
 * - DOCUMENTAR
 * - PUBLICAR
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
    private int fps; // <--------------------------------------------------------------------- Frame rate default 60fps
    private int steps; // <------------------------------------------------------------------------ Number steps
    private int duration; // <--------------------------------------------------------------------- Duration in milliseconds
    private int periode; // <------------------------------------------------------------------------------------------- Time between frame
    private int i;
    private long newTime, oldTime;
    private UpdateListener updateListener;
    private EndListener endListener;
    private ErrorListener errorListener;
    private Event event;
    private byte interpolation = LINEAL;
    private int dt;
    private int t;
    private boolean alive;
    private boolean pause;
    //</editor-fold>
    
    public Animation(int duration) {
        this.duration = duration;
        fps = 60;
        calculate();
    }
    
    public Animation(Builder builder) {
        duration = builder.duration;
        fps = builder.fps;
        interpolation = builder.interpolation;
        updateListener = builder.updateListener;
        endListener = builder.endListener;
        errorListener = builder.errorListener;
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
        return alive;
    }

    public boolean isPause() {
        return pause;
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="CONTROLS">
    public void play() {
        if (pause) {
            pause = false;
            newTime = System.currentTimeMillis();
        } else {
            reset();
        }
        new Thread(this).start();
    }

    public void pause() {
        pause = true;
    }

    public void stop() {
        i = steps-1;
    }

    public void toTime(int time) {
        i = (time*steps)/duration;
        t = i*periode;
    }

    public void toFrame(int frame) {
        i = frame;
        t = i*periode;
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="PRIVATE">
    private void calculate() {
        event = new Event();
        periode = (int) Math.round(1000/(double)fps);
        steps = (int) Math.round(duration/(double)periode);
    }
    
    private void update() {
        oldTime = newTime;
        newTime = System.currentTimeMillis();
        dt = (int) (newTime-oldTime);
        t += dt;
    }

    private void reset() {
        newTime = System.currentTimeMillis();
        i = 0;
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

    public void onError(ErrorListener errorListener) {
        this.errorListener = errorListener;
    }
    //</editor-fold>
    
    @Override
    public void run() {
        try {

            alive = true;

            for (; i<=steps; i++) {
                update();
                if (updateListener!=null) updateListener.action(event);
                Thread.sleep(periode);
                if (pause) break;
            }

            if (endListener!=null && !pause) endListener.action();

            alive = false;

        } catch (Exception e) {
            if (errorListener!=null) errorListener.action(e);
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

    @FunctionalInterface
    public interface ErrorListener {
        void action(Exception e);
    }
    //</editor-fold>
    
    public class Event {

        private double tn;
            
        public int delta(byte interpolation, int size) {
            
            if (i>=steps || t>=duration) t = duration;
            tn = t/(double)duration;
            double x;
            
            switch (interpolation) {
                
                case ACCELERATED:
                    x = Math.pow(tn, 2);
                    break;
                
                case CUSHION:
                    x = -Math.pow(1-tn, 2)+1;
                    break;

                case REBOUND:
                    x = (tn>=0.5) ? (Math.pow(tn-0.75, 2)*4)+0.75 : Math.pow(tn*2, 2);
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

        public int fromTo(int from, int to) {
            return from+delta(to-from);
        }

        public int fromTo(byte interpolation, int from, int to) {
            return from+delta(interpolation, to-from);
        }

        public int getNumberFrame() {
            return i;
        }

        public int getDeltaTime() {
            return dt;
        }

        public int getTimeElapsed() {
            return t;
        }

        public double getTimeNormalize() {
            return tn;
        }
        
    }
    
    public static class Builder {
        
        private int duration;
        private byte interpolation;
        private int fps;
        private UpdateListener updateListener;
        private EndListener endListener;
        private ErrorListener errorListener;
        
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

        public Builder onError(ErrorListener errorListener) {
            this.errorListener = errorListener;
            return this;
        }
        
        public Animation build() {
            return new Animation(this);
        }
        
        public void start() {
            new Animation(this).play();
        }
        
    }
 
}