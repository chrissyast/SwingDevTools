## Example Main File For Testing

```java

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import static dev.astles.SwingDevTools.initialise;

public class Debugger {
    
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setSize(500, 200);
        JPanel panel = new JPanel();
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
        JViewport port = new JViewport();
        port.setView(panel);
        port.setVisible(true);
        frame.add(port);
        initialise();
    }

}
```