import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRootPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;

public class CountdownTimer extends JFrame {

    private static final Color DESKTOP_BACKGROUND = new Color(154, 0, 137);
    private static final Color SOME_OTHER_COLOR = new Color(154, 200, 137);
    private final JLabel label;
    private int count;
    private int blinkCount;
    private Timer timer;

    public static void main(String[] args) {
        CountdownTimer countdownTimer = new CountdownTimer();
        if(args.length == 2){
            countdownTimer.setLocation(Integer.valueOf(args[0]), Integer.valueOf(args[1]));
        }
    }

    private CountdownTimer() throws HeadlessException {
        setAlwaysOnTop(true);
        setLayout(new BorderLayout());
        setUndecorated(true);
        getRootPane().setWindowDecorationStyle(JRootPane.NONE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        label = new JLabel("5:00");
        label.setFont(label.getFont().deriveFont(20f));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                restart();
            }
        });
        add(label);
        restart();
        pack();
        setVisible(true);
    }

    private void restart() {
        if (timer != null) {
            timer.cancel();
        }
        count = 5 * 60;
        timer = new Timer(true);
        label.setText("5:00");
        label.setBackground(DESKTOP_BACKGROUND);
        label.setOpaque(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                count--;
                label.setText(String.format("%d:%02d", count / 60, count % 60));
                if (count == 0) {
                    timer.cancel();
                    timer = new Timer(true);
                    blinkCount = 0;
                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            label.setBackground(
                                    label.getBackground().equals(DESKTOP_BACKGROUND)
                                            ? SOME_OTHER_COLOR
                                            : DESKTOP_BACKGROUND);
                            if(blinkCount++ == 20){
                                timer.cancel();
                            }
                        }
                    }, 0, 300);
                }
            }
        }, 1000, 1000);
    }
}
