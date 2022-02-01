package com.ironchoobs.rlclans.ui.helpers;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public abstract class CollapsiblePanel extends JPanel {

    private final JButton button = new JButton();
    private boolean collapsed = true;

    protected final JPanel header = new JPanel();
    protected final JPanel body = new JPanel();

    public CollapsiblePanel() {
        super();

        //button.setIcon()
        button.setText("Expand");
        body.setVisible(false);

        button.addActionListener(evt -> {
            setCollapsed((collapsed = !collapsed));
        });

        header.setLayout(new BoxLayout(header, BoxLayout.LINE_AXIS));
        header.add(button);
        header.add(Box.createRigidArea(new Dimension(10, 0)));

        body.setLayout(new BoxLayout(body, BoxLayout.PAGE_AXIS));
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.add(header);
        this.add(Box.createRigidArea(new Dimension(0, 5)));
        this.add(body);

        //testing
        this.setBorder(new LineBorder(Color.WHITE, 1));
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;

        if (collapsed) {
            button.setText("Expand");
            body.setVisible(false);
        }
        else {
            button.setText("Collapse");
            body.setVisible(true);

            loadData();
        }
    }

    protected abstract void loadData();

}
