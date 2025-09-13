package com.survivor.core;

import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.Parent;
import java.util.HashMap;
import java.util.Map;

public class UIManager {

    private final Map<String, Parent> uiMap;
    private String currentUI;

    public UIManager() {
        uiMap = new HashMap<>();
        currentUI = null;
    }

    // 注册 UI
    public void registerUI(String name, Parent uiContent) {
        uiMap.put(name, uiContent);
    }

    // 显示 UI
    public void showUI(String name) {
        if (currentUI != null) {
            clearUI(currentUI);
        }
        Parent uiContent = uiMap.get(name);
        if (uiContent != null) {
            FXGL.getGameScene().addUINode(uiContent);
            currentUI = name;
        }
    }

    // 销毁 UI
    public void clearUI(String name) {
        Parent uiContent = uiMap.get(name);
        if (uiContent != null) {
            FXGL.getGameScene().removeUINode(uiContent);
        }
    }

    // 获取当前 UI
    public String getCurrentUI() {
        return currentUI;
    }

    // ===== 新增：覆盖层（UpgradePanel 等临时弹层） =====
    public void addOverlay(Parent node) {
        if (node != null) {
            FXGL.getGameScene().addUINode(node);
        }
    }

    public void removeOverlay(Parent node) {
        if (node != null) {
            FXGL.getGameScene().removeUINode(node);
        }
    }
}
