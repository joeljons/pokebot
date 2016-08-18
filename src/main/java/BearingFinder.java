import com.grum.geocalc.EarthCalc;
import com.grum.geocalc.Point;

import java.util.HashMap;
import java.util.Map;

public class BearingFinder {
    private static Map<Double, String> dirs = new HashMap<>();
    static {
        dirs.put(0d, "N");
        dirs.put(22.5d, "NNE");
        dirs.put(45d, "NE");
        dirs.put(67.5d, "ENE");
        dirs.put(90d, "E");
        dirs.put(112.5d, "ESE");
        dirs.put(135d, "SE");
        dirs.put(157.5d, "SSE");
        dirs.put(180d, "S");
        dirs.put(202.5d, "SSW");
        dirs.put(225d, "SW");
        dirs.put(247.5d, "WSW");
        dirs.put(270d, "W");
        dirs.put(292.5d, "WNW");
        dirs.put(315d, "NW");
        dirs.put(337.5d, "NNW");
        dirs.put(360d, "N");
    }

    public static String getBearingString(Point from, Point to) {
        double bearing = EarthCalc.getBearing(from, to);
        String bestDir = null;
        double bestDiff = Double.MAX_VALUE;
        for (Map.Entry<Double, String> entry : dirs.entrySet()) {
            double diff = Math.abs(entry.getKey() - bearing);
            if(diff < bestDiff){
                bestDiff = diff;
                bestDir = entry.getValue();
            }
        }
        return bestDir;
    }
}
