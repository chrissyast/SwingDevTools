package dev.astles;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.awt.event.MouseEvent.MOUSE_ENTERED;
import static java.awt.event.MouseEvent.MOUSE_EXITED;



public class SwingDevTools {

    private static final List<Color> colours = Arrays.asList(Color.RED, Color.GREEN, Color.YELLOW, Color.BLUE, Color.MAGENTA, Color.ORANGE, Color.CYAN);
    private static final JWindow overlay = new JWindow();
    private static final Map<JComponent, Border> originalBorders = new HashMap<>();
    private static final Map<String, Color> assignedColours = new HashMap<>();
    private static boolean isControlPressed = false;

    public static void initialise() {
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
        try {
            boolean isSwing = comp.getClass().getPackageName().contains("javax.swing");
            int thickness = isSwing ? 1 : 3;
            comp.setBorder(resetting || !isControlPressed ? originalBorders.get(comp) : BorderFactory.createLineBorder(assignedColours.get(comp.getClass().getSimpleName()), thickness));
        } catch (IllegalArgumentException e) {
            // just carry on
        }
        if (comp.getParent() != null && comp.getParent() instanceof JComponent) {
            setComponentBorder((JComponent) comp.getParent(), resetting);
        }
    }

    private static void handleMouseEntered(AWTEvent e) {
        if (!(e.getSource() instanceof JComponent)) {
            return;
        }
        JComponent eventComponent = (JComponent) e.getSource();
        JComponent comp = (JComponent) SwingUtilities.getDeepestComponentAt(eventComponent, ((MouseEvent) e).getX(), ((MouseEvent) e).getY());
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
}
