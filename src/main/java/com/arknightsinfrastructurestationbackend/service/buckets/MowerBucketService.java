package com.arknightsinfrastructurestationbackend.service.buckets;

import com.arknightsinfrastructurestationbackend.common.aspect.redisLock.RedisLock;
import com.arknightsinfrastructurestationbackend.common.exception.ServiceException;
import com.arknightsinfrastructurestationbackend.common.tools.Log;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.obs.services.ObsClient;
import com.obs.services.exception.ObsException;
import com.obs.services.model.ObjectListing;
import com.obs.services.model.ObsObject;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;

@Service
@AllArgsConstructor
public class MowerBucketService {
    private final String ak = System.getenv("HUAWEICLOUD_OBS_ACCESS_KEY_ID");
    private final String sk = System.getenv("HUAWEICLOUD_OBS_SECRET_ACCESS_KEY_ID");
    private final String endPoint = "https://obs.cn-north-4.myhuaweicloud.com";
    private final String bucketName = "mower";
    private final ObsClient obsClient = new ObsClient(ak, sk, endPoint);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final int MAX_ATTEMPTS = 50;
    private final Random random = new Random(System.currentTimeMillis());

    private String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

    private List<String> getAllKeys() {
        List<String> keys = new ArrayList<>();
        try {
            ObjectListing result = obsClient.listObjects(bucketName);
            for (ObsObject obsObject : result.getObjects()) {
                keys.add(obsObject.getObjectKey());
            }
        } catch (ObsException e) {
            Log.error("Error listing objects: " + e.getErrorMessage());
        }
        return keys;
    }

    private String addKey() {
        List<String> existingKeys = getAllKeys();
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String key = generateRandomString(5);
            if (!existingKeys.contains(key)) {
                return key;
            }
        }
        throw new ServiceException("在尝试生成唯一键 50 次后失败");
    }

    private boolean deleteKey(String key) {
        try {
            obsClient.deleteObject(bucketName, key);
            return true;
        } catch (ObsException e) {
            Log.error("Error deleting object: " + e.getErrorMessage());
            return false;
        }
    }

    /**
     * 上传单个WebP文件并返回生成的键
     *
     * @param dataUrl webp文件数据url字符串
     * @return 生成的用于存储的桶键
     */
    @RedisLock(key = "'lock:WebPStorage:uploadSingleWebP:' + #dataUrl")
    public String uploadSingleWebP(String dataUrl) {
        String key = null;
        try {
            String base64Data = dataUrl.split(",")[1];
            byte[] pictureBytes = Base64.getDecoder().decode(base64Data);

            key = addKey();
            obsClient.putObject(bucketName, key, new ByteArrayInputStream(pictureBytes));
            return key;
        } catch (ObsException e) {
            Log.error("putObject failed: " + e.getErrorMessage());
            if (key != null) {
                deleteKey(key);
            }
            throw new ServiceException("上传WebP图片失败");
        }
    }

    /**
     * 上传多个WebP文件并返回生成的键数组
     *
     * @param picturesArrayString 图片数组字符串
     * @return 存储图片的键数组字符串
     * @throws JsonProcessingException JSON处理异常
     */
    @RedisLock(key = "'lock:WebPStorage:uploadMultipleWebP:' + #picturesArrayString")
    public String uploadMultipleWebP(String picturesArrayString) throws JsonProcessingException {
        String[] pictures = objectMapper.readValue(picturesArrayString, String[].class);
        List<String> existingKeys = getAllKeys();
        List<String> keys = new ArrayList<>();

        if (pictures.length > 5) {
            throw new ServiceException("图片数量超过限制，最多允许上传5张图片");
        }

        try {
            for (String picture : pictures) { //仅提供循环次数作用
                boolean keyGenerated = false;
                for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
                    String key = generateRandomString(5);
                    if (!existingKeys.contains(key) && !keys.contains(key)) {
                        keys.add(key);
                        keyGenerated = true;
                        break;
                    }
                }
                if (!keyGenerated) {
                    throw new ServiceException("生成唯一键失败");
                }
            }

            for (int i = 0; i < pictures.length; i++) {
                String base64Data = pictures[i].split(",")[1];
                byte[] pictureBytes = Base64.getDecoder().decode(base64Data);
                obsClient.putObject(bucketName, keys.get(i), new ByteArrayInputStream(pictureBytes));
            }
            return objectMapper.writeValueAsString(keys);
        } catch (ObsException e) {
            Log.error("putObject failed: " + e.getErrorMessage());
            for (String key : keys) {
                deleteKey(key);
            }
            throw new ServiceException("上传一个或多个WebP图片失败");
        }
    }

    /**
     * 删除单个WebP文件
     *
     * @param key 要删除的文件键
     */
    @CacheEvict("WebPStorage")
    @RedisLock(key = "'lock:WebPStorage:removeSingleWebP:' + #key")
    public void removeSingleWebP(String key) {
        if (!deleteKey(key)) {
            throw new ServiceException("删除WebP图片失败，键：" + key);
        }
    }

    //该方法不方便加@CacheEvict注解
    /**
     * 删除多个WebP文件
     *
     * @param keysArrayString 要删除的文件键数组字符串
     * @throws JsonProcessingException JSON处理异常
     */
    @RedisLock(key = "'lock:WebPStorage:removeMultipleWebP:' + #keysArrayString")
    public void removeMultipleWebP(String keysArrayString) throws JsonProcessingException {
        String[] keys = objectMapper.readValue(keysArrayString, String[].class);
        List<String> existingKeys = getAllKeys();

        for (String key : keys) {
            if (!existingKeys.contains(key)) {
                throw new ServiceException("桶中未找到键：" + key);
            }
        }

        List<String> failedKeys = new ArrayList<>();

        for (String key : keys) {
            boolean success = deleteKey(key);
            if (!success) {
                failedKeys.add(key);
            }
        }

        if (!failedKeys.isEmpty()) {
            for (String key : failedKeys) {
                boolean success = false;
                for (int attempt = 0; attempt < 3; attempt++) { //最多尝试3次再删除操作
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    success = deleteKey(key);
                    if (success) {
                        break;
                    }
                }
                if (!success) {
                    throw new ServiceException("删除多个WebP图片失败：" + key);
                }
            }
        }
    }

    @Cacheable("WebPStorage")
    @RedisLock(key = "'lock:WebPStorage:downloadWebP:' + #objectKey")
    public String downloadWebP(String objectKey) {
        String content = null;
        try {
            ObsObject obsObject = obsClient.getObject(bucketName, objectKey);
            InputStream input = obsObject.getObjectContent();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[2 * 1024 * 1024];
            int len;
            while ((len = input.read(b)) != -1) {
                bos.write(b, 0, len);
            }
            content = "data:image/webp;base64," + Base64.getEncoder().encodeToString(bos.toByteArray());
            bos.close();
            input.close();
        } catch (ObsException e) {
            Log.error("getObjectContent failed: " + e.getErrorMessage());
        } catch (Exception e) {
            Log.error("getObjectContent failed: " + e.getMessage());
        }
        return content;
    }
}