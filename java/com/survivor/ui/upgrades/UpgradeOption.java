package com.survivor.ui.upgrades;

import javafx.scene.image.Image;

public class UpgradeOption {
    private final String id;
    private final String title;
    private final String description;
    private final Image icon;

    public UpgradeOption(String id, String title, String description, Image icon) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.icon = icon;
    }

    public String id() { return id; }
    public String title() { return title; }
    public String description() { return description; }
    public Image icon() { return icon; }
}
