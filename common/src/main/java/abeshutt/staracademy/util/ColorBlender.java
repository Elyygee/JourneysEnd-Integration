package abeshutt.staracademy.util;

import org.joml.Math;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;

public class ColorBlender {

    private final float speed;
    private final List<Pair<Integer, Float>> sequence = new ArrayList<>();
    private float totalTime;

    public ColorBlender(float speed) {
        this.speed = speed;
    }

    public ColorBlender add(int color, float interval) {
        this.sequence.add(new Pair<>(color, interval));
        this.totalTime += interval;
        return this;
    }

    public int getColor(double time) {
        if(this.totalTime == 0) {
            return 0xFFFFFF;
        }

        double value = time * this.speed % this.totalTime;
        double ratio = 0.0F;
        int color1 = 0, color2 = 0;

        for(int j = 0; j < this.sequence.size(); j++) {
            Pair<Integer, Float> pair = this.sequence.get(j);
            int color = pair.getA();
            float interval = pair.getB();

            if(value < interval) {
                color1 = color;
                color2 = this.sequence.get((j + 1) % this.sequence.size()).getA();
                ratio = value / interval;
                break;
            }

            value -= interval;
        }

        return blendColors(color2, color1, ratio);
    }

    public static int blendColors(int color1, int color2, double color1Ratio) {
        double ratio1 = Math.clamp(color1Ratio, 0F, 1F);
        double ratio2 = 1F - ratio1;

        int a1 = (color1 & 0xFF000000) >> 24;
        int r1 = (color1 & 0x00FF0000) >> 16;
        int g1 = (color1 & 0x0000FF00) >>  8;
        int b1 = (color1 & 0x000000FF);

        int a2 = (color2 & 0xFF000000) >> 24;
        int r2 = (color2 & 0x00FF0000) >> 16;
        int g2 = (color2 & 0x0000FF00) >>  8;
        int b2 = (color2 & 0x000000FF);

        int a = Math.clamp((int)Math.round(a1 * ratio1 + a2 * ratio2), 0, 255);
        int r = Math.clamp((int)Math.round(r1 * ratio1 + r2 * ratio2), 0, 255);
        int g = Math.clamp((int)Math.round(g1 * ratio1 + g2 * ratio2), 0, 255);
        int b = Math.clamp((int)Math.round(b1 * ratio1 + b2 * ratio2), 0, 255);

        return a << 24 | r << 16 | g << 8 | b;
    }

}
