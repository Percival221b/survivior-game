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
        // 如果当前有 UI 显示，先清除它
        if (currentUI != null) {
            clearUI(currentUI);  // 调用 clearUI() 清除当前 UI
        }

        // 获取要显示的 UI
        Parent uiContent = uiMap.get(name);
        if (uiContent != null) {
            FXGL.getGameScene().addUINode(uiContent);  // 添加新的 UI
            currentUI = name;  // 更新当前 UI
        }
    }

    // 销毁 UI
    private void clearUI(String name) {
        Parent uiContent = uiMap.get(name);
        if (uiContent != null) {
            FXGL.getGameScene().removeUINode(uiContent);  // 从场景中移除 UI
        }
    }

    // 获取当前 UI
    public String getCurrentUI() {
        return currentUI;
    }
}
