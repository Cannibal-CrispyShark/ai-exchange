package com.huge.aiexchange.enums;

import lombok.Getter;

/**
 * 风险偏好枚举
 */
@Getter
public enum RiskPreferenceEnum {

    CONSERVATIVE("保守型", "conservative", "注重本金安全，追求稳定收益，风险承受能力低"),
    MODERATE("稳健型", "moderate", "平衡风险与收益，适度承担风险"),
    AGGRESSIVE("激进型", "aggressive", "追求高收益，愿意承担较高风险");

    /**
     * 显示名称
     */
    private final String displayName;

    /**
     * 代码
     */
    private final String code;

    /**
     * 描述
     */
    private final String description;

    RiskPreferenceEnum(String displayName, String code, String description) {
        this.displayName = displayName;
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     * @param code 代码
     * @return 枚举对象，如果不存在返回MODERATE
     */
    public static RiskPreferenceEnum getByCode(String code) {
        for (RiskPreferenceEnum preference : values()) {
            if (preference.getCode().equalsIgnoreCase(code)) {
                return preference;
            }
        }
        return MODERATE;
    }

}
