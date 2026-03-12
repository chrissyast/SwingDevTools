package dev.astles;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.awt.event.FocusEvent.FOCUS_LOST;
import static java.awt.event.MouseEvent.*;


public class SwingDevTools {

    private static final List<Color> colours = Arrays.asList(Color.RED, Color.GREEN, Color.YELLOW, Color.BLUE, Color.MAGENTA, Color.ORANGE, Color.CYAN);
    private static final JWindow overlay = new JWindow();
    private static final Map<JComponent, Border> originalBorders = new HashMap<>();
    private static final Map<String, Color> assignedColours = new HashMap<>();
    private static boolean isControlPressed = false;
    private static JComponent topNonSwingComponent = null;

    private static String project;

    public static void initialise() {
        initialise("");
    }
    public static void initialise(String projectName) {
        project = projectName;
        Toolkit.getDefaultToolkit().addAWTEventListener(e -> {
            if (e.getID() == MOUSE_ENTERED) {
                handleMouseEntered(e);
            }
            if (e.getID() == MOUSE_EXITED) {
                handleMouseExit(e);
            }
            if (e.getID() == MOUSE_CLICKED && ((MouseEvent) e).getButton() == 2 && isControlPressed && !project.isBlank()) {
                handleMiddleButton(e);
            }
        }, AWTEvent.MOUSE_EVENT_MASK);
        Toolkit.getDefaultToolkit().addAWTEventListener(e -> {
            if (((KeyEvent) e).getKeyCode() == KeyEvent.VK_CONTROL) {
                isControlPressed = e.getID() == KeyEvent.KEY_PRESSED;
            }
        }, AWTEvent.KEY_EVENT_MASK);
        Toolkit.getDefaultToolkit().addAWTEventListener(e -> {
            if (e.getID() == FOCUS_LOST) {
                clearAllBorders();
                isControlPressed = false;
            }}, AWTEvent.FOCUS_EVENT_MASK);
    }

    private static void clearAllBorders() {
        for (Map.Entry<JComponent, Border> borderEntry : originalBorders.entrySet()) {
            borderEntry.getKey().setBorder(borderEntry.getValue());
        }
    }

    private static String getComponentPath(JComponent comp) {
        String text = comp.getClass().getSimpleName();
        boolean isSwing = comp.getClass().getPackageName().contains("javax.swing");
        if (!isSwing) {
            text = "<span style='color: red'>" + text + "</span>";
            if (topNonSwingComponent == null) {
                topNonSwingComponent = comp;
            }
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

    private static void setComponentBorder(JComponent comp) {
        try {
            boolean isSwing = comp.getClass().getPackageName().contains("javax.swing");
            int thickness = isSwing ? 1 : 3;
            Border border = comp.getBorder();
            if (!originalBorders.containsKey(comp)) {
                originalBorders.put(comp, border);
            }
            if (!assignedColours.containsKey(comp.getClass().getSimpleName())) {
                assignedColours.put(comp.getClass().getSimpleName(), colours.get(assignedColours.size() % colours.size()));
            }
            comp.setBorder(!isControlPressed ? originalBorders.get(comp) : BorderFactory.createLineBorder(assignedColours.get(comp.getClass().getSimpleName()), thickness));
        } catch (IllegalArgumentException e) {
            // just carry on
        }
        if (comp.getParent() != null && comp.getParent() instanceof JComponent) {
            setComponentBorder((JComponent) comp.getParent());
        }
    }

    private static void handleMouseEntered(AWTEvent e) {
        if (!(e.getSource() instanceof JComponent)) {
            return;
        }
        JComponent eventComponent = (JComponent) e.getSource();
        JComponent comp = (JComponent) SwingUtilities.getDeepestComponentAt(eventComponent, ((MouseEvent) e).getX(), ((MouseEvent) e).getY());

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
        setComponentBorder(comp);
        overlay.setLocation(p.x + 10, p.y + 10); // TODO
        overlay.setVisible(isControlPressed);
    }

    private static void handleMouseExit(AWTEvent e) {
        topNonSwingComponent = null;
        if (!isControlPressed) {
            clearAllBorders();
            return;
        }
        if (!(e.getSource() instanceof JComponent)) {
            return;
        }
        JComponent eventComponent = (JComponent) e.getSource();
        Component comp = SwingUtilities.getDeepestComponentAt(eventComponent, ((MouseEvent) e).getX(), ((MouseEvent) e).getY());
        if (comp == null) {
            return;
        }
        while (!(comp instanceof JComponent)) {
            comp = comp.getParent();
        }
        setComponentBorder((JComponent) comp);
        overlay.setVisible(false);
    }

    private static void handleMiddleButton(AWTEvent e) {
        String componentPath = topNonSwingComponent.getClass().getName();
        componentPath = componentPath.replace(".", "%2f");
        String uri = String.format("jetbrains://idea/navigate/reference?project=%s&path=%s", project, componentPath);
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(uri));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
