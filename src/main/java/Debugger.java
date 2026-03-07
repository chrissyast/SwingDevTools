import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Debugger {


    private static final Border HOVER_BORDER = BorderFactory.createLineBorder(Color.RED, 2);
    private static final JWindow overlay = new JWindow();
    private static final JLabel overlayLabel = new JLabel();

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setSize(500, 200);
        JPanel panel = new JPanel();
        panel.add(new JCheckBox());
        panel.add(new JTextArea());
        panel.add(new JTextField());
        panel.setSize(200, 200);
        frame.add(panel);
        frame.setVisible(true);
        System.out.println("foo!");

        overlay.setBackground(new Color(0,0,0,0));

        overlayLabel.setOpaque(true);
        overlayLabel.setBackground(new Color(0,0,0,200));
        overlayLabel.setForeground(Color.WHITE);
        overlayLabel.setBorder(BorderFactory.createEmptyBorder(4,8,4,8));

        overlay.add(overlayLabel);
        overlay.pack();
        attachListeners(frame);
    }

    private static void attachListeners(Container root) {

        for (Component c : root.getComponents()) {

            if (c instanceof JComponent) {
                JComponent jc = ((JComponent) c);
                installListener(jc);
            }

            if (c instanceof Container) {
                Container child = (Container) c;
                attachListeners(child);
            }
        }
    }

    private static void installListener(JComponent comp) {

        Border originalBorder = comp.getBorder();

        comp.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {

                comp.setBorder(HOVER_BORDER);

                String name = comp.getName();
                if (name == null || name.isBlank()) {
                    name = "(unnamed)";
                }

                String text = name + " : " + comp.getClass().getSimpleName();
                overlayLabel.setText(text);
                overlay.pack();

                Point p = e.getLocationOnScreen();
                overlay.setLocation(p.x + 10, p.y + 10);
                overlay.setVisible(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                comp.setBorder(originalBorder);
                overlay.setVisible(false);
            }

            @Override
            public void mouseClicked(MouseEvent e) {

                System.out.println("---- Component Info ----");
                System.out.println("Name: " + comp.getName());
                System.out.println("Class: " + comp.getClass());
                System.out.println("Size: " + comp.getSize());
                System.out.println("Location: " + comp.getLocation());
                System.out.println("Parent: " + comp.getParent());
                System.out.println("------------------------");
            }
        });
    }
}

