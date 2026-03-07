import dev.astles.ChrissyAPanel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.awt.event.MouseEvent.MOUSE_ENTERED;
import static java.awt.event.MouseEvent.MOUSE_EXITED;

public class Debugger {

    private static final List<Color> colours = Arrays.asList(Color.RED, Color.GREEN, Color.YELLOW, Color.BLUE, Color.MAGENTA, Color.ORANGE, Color.CYAN);
    private static final JWindow overlay = new JWindow();
    private static final Map<JComponent, Border> originalBorders = new HashMap<>();
    private static final Map<String, Color> assignedColours = new HashMap<>();
    private static boolean isControlPressed = false;

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

        overlay.setBackground(new Color(0,0,0,0));
        overlay.pack();
        addListeners();
    }

    private static void addListeners() {
        Toolkit.getDefaultToolkit().addAWTEventListener(e -> {
            if (e.getID() == MOUSE_ENTERED) {
                handleMouseEntered(e);
            }
            if (e.getID() == MOUSE_EXITED) {
                handleMouseExit(e);
            }
        }, AWTEvent.MOUSE_EVENT_MASK);
        Toolkit.getDefaultToolkit().addAWTEventListener(e -> {
            if (((KeyEvent) e).getKeyCode() == KeyEvent.VK_CONTROL) {
                isControlPressed = e.getID() == KeyEvent.KEY_PRESSED;
            }
        }, AWTEvent.KEY_EVENT_MASK);
    }

    private static String getComponentPath(JComponent comp) {
        String text = comp.getClass().getSimpleName();
        if (!comp.getClass().getPackageName().contains("javax.swing")) {
            text = "<span style='color: red'>" + text + "</span>";
        }
        if (!assignedColours.containsKey(comp.getClass().getSimpleName())) {
            assignedColours.put(comp.getClass().getSimpleName(), colours.get(assignedColours.size() % colours.size()));
        }
        Color borderColour = assignedColours.get(comp.getClass().getSimpleName());
        String hex = String.format("#%02x%02x%02x", borderColour.getRed(), borderColour.getGreen(), borderColour.getBlue());
        text = text + "<font color='" + hex + "'>●</font>";
        if (comp.getParent() != null && comp.getParent() instanceof JComponent) {
            text = text + "<br>" + getComponentPath((JComponent) comp.getParent());
        }
        return text;
    }

    private static void setComponentBorder(JComponent comp, boolean resetting) {
        comp.setBorder(resetting || !isControlPressed ? originalBorders.get(comp) : BorderFactory.createLineBorder(assignedColours.get(comp.getClass().getSimpleName()), 3));
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
        if (!assignedColours.containsKey(comp.getClass().getSimpleName())) {
            assignedColours.put(comp.getClass().getSimpleName(), colours.get(assignedColours.size() % colours.size()));
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
        overlay.setVisible(isControlPressed);
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

