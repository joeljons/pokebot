import com.grum.geocalc.DegreeCoordinate;
import com.grum.geocalc.EarthCalc;
import com.grum.geocalc.Point;

import java.util.Locale;

public class Algo {
        private final static double LATITUDE = 59.316140;
    private final static double LONGITUDE = 18.034604;
    private static int count = 0;

    public static void main(String[] args) {
        Point home = new Point(new DegreeCoordinate(LATITUDE), new DegreeCoordinate(LONGITUDE));
        print(home);
        for(int i=0;i<6;i++){
            print(EarthCalc.pointRadialDistance(home, i * 60, 120));
        }
        Point p = EarthCalc.pointRadialDistance(home, 30, 210);
        print(p);
        p = EarthCalc.pointRadialDistance(p, 120, 120);
        print(p);
        for(int i=0;i<5;i++){
            for(int j=0;j<2;j++){
                p = EarthCalc.pointRadialDistance(p, 180+i*60, 120);
                print(p);
            }
        }
    }

    private static void print(Point point) {
        System.out.println(String.format(Locale.ENGLISH, "%d: {lat: %f, lng: %f},", count++, point.getLatitude(), point.getLongitude()));
    }
}
