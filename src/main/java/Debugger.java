import dev.astles.ChrissyAPanel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.awt.event.MouseEvent.MOUSE_ENTERED;
import static java.awt.event.MouseEvent.MOUSE_EXITED;

public class Debugger {

    //private static final List<Border> borders = Arrays.asList(RED_BORDER, GREEN_BORDER, YELLOW_BORDER, BLUE_BORDER);
    private static final List<Color> colours = Arrays.asList(Color.RED, Color.GREEN, Color.YELLOW, Color.BLUE);
    private static final JWindow overlay = new JWindow();
    //private static final JLabel overlayLabel = new JLabel();
    private static final Map<JComponent, Border> originalBorders = new HashMap<>();
    private static final Map<JComponent, Color> assignedColours = new HashMap<>();
    private static final Map<JComponent, JLabel> labels = new HashMap<>();

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setSize(500, 200);
        ChrissyAPanel panel = new ChrissyAPanel();
        panel.add(new JCheckBox());
        panel.add(new JTextArea());
        panel.add(new JTextField());
        panel.setSize(200, 200);
        JSplitPane pane = new JSplitPane();
        pane.setSize(100, 100);
        pane.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(pane);
        frame.add(panel);
        frame.setVisible(true);
        System.out.println("foo!");

        overlay.setBackground(new Color(0,0,0,0));
        overlay.pack();
        //attachListeners(frame);
        Toolkit.getDefaultToolkit().addAWTEventListener(e -> {
            if (e.getID() == MOUSE_ENTERED) {
                System.out.println("ye " + e.getSource());
                handleMouseEntered(e);
            }
            if (e.getID() == MOUSE_EXITED) {
                System.out.println("ey " + e.getSource());
                handleMouseExit(e);
            }
        }, AWTEvent.MOUSE_EVENT_MASK);
    }


    private static String getComponentPath(JComponent comp) {
        String text = comp.getClass().getSimpleName();
        if (comp.getClass().getPackageName().equals("dev.astles")) {
            text = "<span style='color: red'>" + text + "</span>";
        }
        if (!assignedColours.containsKey(comp)) {
            assignedColours.put(comp, colours.get(assignedColours.size() % 4));
        }
        Color borderColour = assignedColours.get(comp);
        String hex = String.format("#%02x%02x%02x", borderColour.getRed(), borderColour.getGreen(), borderColour.getBlue());
        text = text + "<font color='" + hex + "'>●</font>";
        if (comp.getParent() != null && comp.getParent() instanceof JComponent) {
            text = text + "<br>" + getComponentPath((JComponent) comp.getParent());
        }
        return text;
    }

    private static void setComponentBorder(JComponent comp, boolean resetting) {
        comp.setBorder(resetting ? originalBorders.get(comp) : BorderFactory.createLineBorder(assignedColours.get(comp), 3));
        if (comp.getParent() != null && comp.getParent() instanceof JComponent) {
            setComponentBorder((JComponent) comp.getParent(), resetting);
        }
    }

    private static void handleMouseEntered(AWTEvent e) {
        if (!(e.getSource() instanceof JComponent)) {
            return;
        }
        JComponent comp = (JComponent) e.getSource();
        Border originalBorder = comp.getBorder();
        originalBorders.put(comp, originalBorder);
        if (!assignedColours.containsKey(comp)) {
            assignedColours.put(comp, colours.get(assignedColours.size() % 4));
        }
        String text = getComponentPath(comp);
        JLabel overlayLabel = new JLabel();
        overlayLabel.setOpaque(true);
        overlayLabel.setBackground(new Color(0,0,0,200));
        overlayLabel.setForeground(Color.WHITE);
        overlayLabel.setBorder(BorderFactory.createEmptyBorder(4,8,4,8));
        overlayLabel.setText("<html><body>" + text + "</body></html>");
        overlay.getContentPane().removeAll();
        overlay.add(overlayLabel);
        overlay.pack();

        Point p = MouseInfo.getPointerInfo().getLocation();
        setComponentBorder(comp, false);
        overlay.setLocation(p.x + 10, p.y + 10); // TODO
        overlay.setVisible(true);
    }

    private static void handleMouseExit(AWTEvent e) {
        if (!(e.getSource() instanceof JComponent)) {
            return;
        }
        JComponent comp = (JComponent) e.getSource();
        setComponentBorder(comp, true);
        overlay.setVisible(false);
    }

    /*

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
        originalBorders.put(comp, originalBorder);

        comp.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                String text = getComponentPath(comp);
                JLabel overlayLabel = new JLabel();
                overlayLabel.setOpaque(true);
                overlayLabel.setBackground(new Color(0,0,0,200));
                overlayLabel.setForeground(Color.WHITE);
                overlayLabel.setBorder(BorderFactory.createEmptyBorder(4,8,4,8));
                overlayLabel.setText("<html><body>" + text + "</body></html>");
                //overlayLabel.move((int) (Math.random() * 100), (int) (Math.random() * 100));
                overlay.getContentPane().removeAll();
                overlay.add(overlayLabel);
                overlay.pack();

                Point p = e.getLocationOnScreen();
                setComponentBorder(comp, false);
                overlay.setLocation(p.x + 10, p.y + 10); // TODO
                overlay.setVisible(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setComponentBorder(comp, true);
                overlay.setVisible(false);
                //overlay.removeAll();
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
    }*/
}

