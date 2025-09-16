package com.survivor.ui.upgrades;

import javafx.scene.image.Image;

public class UpgradeOption {
    public String id;
    private String title;
    private String description;
    private String icon; // 从 JSON 读出来的路径，不直接存 Image
    public boolean unique;

    public UpgradeOption() {
        // Jackson 需要无参构造函数
    }

    public UpgradeOption(String id, String title, String description, String icon, boolean unique) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.unique = unique;
    }

    // getter / setter
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }

    // 提供一个方法把路径转成 Image
    public Image getImage() {
        var url = UpgradeOption.class.getResource("/" + icon);
        return new Image(url.toExternalForm());
    }
}
