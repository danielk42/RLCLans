package com.ironchoobs.rlclans;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.List;

@Slf4j
public class GroupSelectPanel extends CollapsiblePanel {

    private final JList<String> groupList = new JList<>();
    private final DefaultListModel<String> groupModel = new DefaultListModel<>();
    private final DataProvider dataProvider = DataProvider.instance();
    private final Map<String, PlayerGroup> groups = new HashMap<>();

    private final Consumer<PlayerGroup> callback;

    public GroupSelectPanel(Font headerFont, Consumer<PlayerGroup> activeGroupChanged) {
        super();

        this.callback = activeGroupChanged;

        JLabel headerLabel = new JLabel("Group");
        headerLabel.setFont(headerFont);
        header.add(headerLabel);
        header.add(Box.createHorizontalGlue());

        body.add(groupList);
        groupList.setModel(groupModel);

        // TODO: Active group display in header

        // group selection callback
        groupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        groupList.getSelectionModel().addListSelectionListener(evt -> {
            ListSelectionModel m = (ListSelectionModel) evt.getSource();
            if (!m.isSelectionEmpty()) {
                callback.accept(groups.get(groupList.getSelectedValue()));
            }
        });

        // TODO: Store group ID and return the ID in callback instead of name

        // default expanded
        setCollapsed(false);
    }

    // Call when logging in. Can be called again when re-logging.
    public void setup(Player player) {
        groupModel.removeAllElements();
        groups.clear();

        dataProvider.getPlayerGroups(player.id, groups -> {
            for (PlayerGroup g : groups) {
                this.groups.put(g.name, g);
                groupModel.addElement(g.name);
            }

            // TODO: Default group setting (put in index 0)
            // Set default group & notify RLClansPanel
            groupList.setSelectedIndex(0);
            callback.accept(this.groups.get(groupModel.get(0)));
        }, error -> {
            // TODO: Error handling
        });
    }
}
