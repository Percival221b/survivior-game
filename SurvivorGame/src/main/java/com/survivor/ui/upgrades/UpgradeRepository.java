package com.survivor.ui.upgrades;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.*;

public class UpgradeRepository {

    private static final List<UpgradeOption> ALL_OPTIONS = new ArrayList<>();
    private static final Set<String> CHOSEN_UNIQUES = new HashSet<>();

    static {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream in = UpgradeRepository.class.getResourceAsStream("/config/upgrades.json");
            List<UpgradeOption> loaded = mapper.readValue(in, new TypeReference<>() {});
            ALL_OPTIONS.addAll(loaded);
        } catch (Exception e) {
            throw new RuntimeException("加载升级配置失败", e);
        }
    }

    /** 获取随机3个升级选项 */
    public static List<UpgradeOption> getRandomOptions() {
        List<UpgradeOption> copy = new ArrayList<>();
        for (UpgradeOption opt : ALL_OPTIONS) {
            if (!opt.unique || !CHOSEN_UNIQUES.contains(opt.id)) {
                copy.add(opt);
            }
        }
        Collections.shuffle(copy);
        return copy.subList(0, Math.min(3, copy.size()));
    }

    public static void chooseUpgrade(UpgradeOption option) {
        if (option.unique) {
            CHOSEN_UNIQUES.add(option.id);
            System.out.println("记录唯一升级: " + option.id);
        }
    }
}