package com.arknightsinfrastructurestationbackend.common.tools.fatherUtils.sensitiveInfo;

import com.arknightsinfrastructurestationbackend.common.tools.Log;

import java.lang.reflect.Field;

public abstract class SensitiveData {
    public void handleSensitiveData() {
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            SensitiveInfo annotation = field.getAnnotation(SensitiveInfo.class);
            if (annotation != null) {
                // 设置字段可以被访问，即使它们是私有的
                field.setAccessible(true);
                try {
                    Object value = field.get(this);
                    if (value instanceof String) {
                        String originalValue = value.toString();
                        String maskedValue = maskSensitiveData(
                                originalValue, annotation.start(), annotation.end()
                        );
                        field.set(this, maskedValue);
                    }
                } catch (IllegalAccessException e) {
                    Log.error("脱敏失败：" + e.getMessage());
                }
            }
        }
    }

    private static String maskSensitiveData(String value, int start, int end) {
        // 计算需要脱敏的部分
        int maskLength = value.length() - start - end;

        if (maskLength <= 0) {
            // 如果原始字符串长度不足以保留指定的start和end个字符，直接返回原始字符串
            return value;
        }

        StringBuilder builder = new StringBuilder();
        // 添加保留的前start个字符
        builder.append(value, 0, start);

        // 用星号替换中间的部分
        for (int i = 0; i < maskLength; i++) {
            builder.append("*");
        }

        // 添加保留的后end个字符
        builder.append(value, value.length() - end, value.length());

        return builder.toString();
    }


}
