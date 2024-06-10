package com.arknightsinfrastructurestationbackend.service.buckets;

import com.arknightsinfrastructurestationbackend.config.filter.JWTUtil;
import com.arknightsinfrastructurestationbackend.entitiy.obs.WebPKeys;
import com.arknightsinfrastructurestationbackend.mapper.obs.WebPKeysMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class WebPKeysService {
    private static final int MAX_ATTEMPTS = 100;
    private final WebPKeysMapper webpKeysMapper;
    private final JWTUtil jwtUtil;

    /**
     * 添加 key 到 webp_keys 表
     *
     * @return 添加成功的 key 或 null
     */
    public String addKey() {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String key = jwtUtil.generateRandomString(5);
            try {
                int result = webpKeysMapper.insert(new WebPKeys(key));
                if (result > 0) {
                    return key;
                }
            } catch (Exception e) {
                // 插入失败，继续尝试
            }
        }
        return null; //返回null意味着所有尝试均失败
    }

    /**
     * 删除 webp_keys 表中的指定 key
     *
     * @param key 要删除的 key
     * @return 是否删除成功
     */
    public boolean deleteKey(String key) {
        QueryWrapper<WebPKeys> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(WebPKeys::getKey, key);
        int result = webpKeysMapper.delete(queryWrapper);
        return result > 0;
    }
}
