package mod.pilot.jar_of_chaos.data;

import java.util.Random;

public class IntegerCycleTracker {
    public final int MAX;
    public int tracker;
    public IntegerCycleTracker(int max){
        this.MAX = max;
        tracker = 0;
    }

    public boolean tick(){
        if (tracker < 0) tracker = MAX;
        return --tracker == 0;
    }

    public void reset(){
        tracker = 0;
    }

    public Randomized randomized(int clamp){
        return new Randomized(MAX, clamp);
    }

    public static class Randomized extends IntegerCycleTracker {
        private static final Random random = new Random();
        public final int clamp;
        public Randomized(int middle, int clamp){
            super(middle);
            this.clamp = clamp;
        }

        @Override
        public boolean tick() {
            if (tracker < 0) tracker = MAX + random.nextInt(-clamp, clamp);
            return --tracker == 0;
        }

        public IntegerCycleTracker fixed(){
            return new IntegerCycleTracker(MAX);
        }

        @Override
        public Randomized randomized(int clamp) {
            System.err.println("WARNING! An attempt to get a randomized version of an already randomized IntegerCycleTracker was made! Returning self...");
            return this;
        }
    }
}