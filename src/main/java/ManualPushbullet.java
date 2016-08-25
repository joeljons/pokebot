import net.iharder.jpushbullet2.PushbulletClient;
import net.iharder.jpushbullet2.PushbulletException;

public class ManualPushbullet {
    public static void main(String[] args) throws PushbulletException {
        PushbulletClient client = new PushbulletClient("o.2FD8luBXIQbcVXCu1T5PZcedKulHUmc2");
        String result = client.sendLink(null, "Gotta catch em all. Undrar hur lång headern kan vara.", "https://maps.googleapis.com/maps/api/staticmap?size=250x250&markers=59.316509,18.034096");
//        String result = client.sendNote(null, "My First Push", "Great library. All my devices can see this! https://maps.googleapis.com/maps/api/staticmap?size=250x250&markers=59.316509,18.034096");
        System.out.println("Result: " + result);
    }
}
