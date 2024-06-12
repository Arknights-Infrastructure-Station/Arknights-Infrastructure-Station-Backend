package com.arknightsinfrastructurestationbackend.controller.obs;

import com.arknightsinfrastructurestationbackend.common.aspect.tokenRefresh.ExcludeFromTokenRefresh;
import com.arknightsinfrastructurestationbackend.service.buckets.MowerBucketService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@ExcludeFromTokenRefresh
public class MowerBucketController {

    private final MowerBucketService mowerBucketService;

    /**
     * 根据键从对象存储服务中获取WebP文件的数据URL
     *
     * @param key 对象存储服务中的键
     * @return 对应的WebP文件的数据URL
     */
    @GetMapping("/webp/{key}")
    public String getWebPByKey(@PathVariable String key) {
        return mowerBucketService.downloadWebP(key);
    }
}
