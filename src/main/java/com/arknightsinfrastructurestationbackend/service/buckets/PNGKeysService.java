package com.arknightsinfrastructurestationbackend.service.buckets;

import com.arknightsinfrastructurestationbackend.config.JWTUtil;
import com.arknightsinfrastructurestationbackend.entitiy.obs.PNGKeys;
import com.arknightsinfrastructurestationbackend.mapper.obs.PNGKeysMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PNGKeysService {
    private static final int MAX_ATTEMPTS = 100;
    private final PNGKeysMapper pngKeysMapper;
    private final JWTUtil jwtUtil;

    /**
     * 添加 key 到 png_keys 表
     *
     * @return 添加成功的 key 或 null
     */
    public String addKey() {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String key = jwtUtil.generateRandomString(5);
            try {
                int result = pngKeysMapper.insert(new PNGKeys(key));
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
     * 删除 png_keys 表中的指定 key
     *
     * @param key 要删除的 key
     * @return 是否删除成功
     */
    public boolean deleteKey(String key) {
        QueryWrapper<PNGKeys> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(PNGKeys::getKey, key);
        int result = pngKeysMapper.delete(queryWrapper);
        return result > 0;
    }
}
