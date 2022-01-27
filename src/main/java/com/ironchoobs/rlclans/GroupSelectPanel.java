package com.ironchoobs.rlclans;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;
import java.util.List;

public class GroupSelectPanel extends CollapsiblePanel {

    private final JLabel headerLabel = new JLabel("Group");
    private final JList<JLabel> groupList = new JList<>();
    private final DataProvider dataProvider = DataProvider.instance();

    private final Consumer<String> callback;

    public GroupSelectPanel(Font headerFont, Consumer<String> activeGroupChanged) {
        super();

        this.callback = activeGroupChanged;

        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        headerLabel.setFont(headerFont);
        header.add(headerLabel);

        body.add(groupList);

        // TODO: Active group display in header

        // group selection callback
        groupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        groupList.getSelectionModel().addListSelectionListener(evt -> {
            ListSelectionModel m = (ListSelectionModel) evt.getSource();
            if (!m.isSelectionEmpty()) {
                callback.accept(groupList.getSelectedValue().getText());
            }
        });

        // TODO: Store group ID and return the ID in callback instead of name
    }

    // Call when logging in. Can be called again when re-logging.
    public void setup(Player player) {
        groupList.removeAll();

        dataProvider.getPlayerGroups(player.id, groups -> {
            for (PlayerGroup g : groups) {
                groupList.add(new JLabel(g.name));
            }

            // TODO: Default group setting
            // Set default group & notify RLClansPanel
            groupList.setSelectedIndex(0);
            callback.accept(groupList.getSelectedValue().getText());

            //groupList.revalidate();
            //groupList.repaint();
        }, error -> {
            // TODO: Error handling
        });
    }
}
