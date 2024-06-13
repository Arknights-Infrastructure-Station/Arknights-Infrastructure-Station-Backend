package com.arknightsinfrastructurestationbackend.service.buckets;

import com.arknightsinfrastructurestationbackend.common.aspect.redisLock.RedisLock;
import com.arknightsinfrastructurestationbackend.common.exception.ServiceException;
import com.arknightsinfrastructurestationbackend.service.utils.CommonService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.obs.services.ObsClient;
import com.obs.services.exception.ObsException;
import com.obs.services.model.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
@Slf4j
public class MowerBucketService {
    private final String ak = System.getenv("HUAWEICLOUD_OBS_ACCESS_KEY_ID");
    private final String sk = System.getenv("HUAWEICLOUD_OBS_SECRET_ACCESS_KEY_ID");
    private final String endPoint = "https://obs.cn-north-4.myhuaweicloud.com";
    private final String bucketName;
    private final ObsClient obsClient = new ObsClient(ak, sk, endPoint);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final int MAX_ATTEMPTS = 50;
    private final Random random = new Random(System.currentTimeMillis());

    private final CommonService commonService;

    public MowerBucketService(@Value("${bucketNames.name}") String bucketName, CommonService commonService) {
        this.bucketName = bucketName;
        this.commonService = commonService;
    }

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
            log.error("Error listing objects: {}", e.getErrorMessage());
        }
        return keys;
    }

    private ByteArrayInputStream getStream(String base64String) {
        try {
            String base64Data = base64String.split(",")[1];
            byte[] pictureBytes = Base64.getDecoder().decode(base64Data);
            return new ByteArrayInputStream(pictureBytes);
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            throw new ServiceException("Invalid base64 format");
        }
    }

    private boolean stateIsValid(HeaderResponse result) {
        return result.getStatusCode() >= 200 && result.getStatusCode() < 400;
    }

    private String addKey() {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String key = generateRandomString(5);
            if (!obsClient.doesObjectExist(bucketName, key)) {
                return key;
            }
        }
        throw new ServiceException("在尝试生成唯一键 50 次后失败");
    }

    private boolean deleteKey(String key) {
        try {
            if (obsClient.doesObjectExist(bucketName, key)) {
                boolean success = true;
                DeleteObjectResult deleteObjectResult = obsClient.deleteObject(bucketName, key);
                if (!stateIsValid(deleteObjectResult)) { //最多尝试3次删除
                    int maxAttempts = 3;
                    for (int i = 0; i < maxAttempts; i++) {
                        try {
                            DeleteObjectResult result = obsClient.deleteObject(bucketName, key);
                            if (stateIsValid(result))
                                break;
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            log.error("Thread interrupted: {}", e.getMessage());
                            Thread.currentThread().interrupt();
                        }
                        if (i == maxAttempts - 1)
                            success = false;
                    }
                }
                return success;
            } else {
                log.warn("Key not found: {}", key);
                return false;
            }
        } catch (ObsException e) {
            log.error("Error deleting object: {}", e.getErrorMessage());
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
        String key = addKey();
        PutObjectResult putObjectResult = obsClient.putObject(bucketName, key, getStream(dataUrl));
        if (stateIsValid(putObjectResult))
            return key;
        else throw new ServiceException("上传WebP图片失败");
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
        String[] pictures = commonService.convertStringArray(picturesArrayString);
        if (pictures.length > 5) {
            throw new ServiceException("图片数量超过限制，最多允许上传5张图片");
        }
        List<String> keys = new ArrayList<>();
        for (String picture : pictures) {
            String key = addKey();
            PutObjectResult putObjectResult = obsClient.putObject(bucketName, key, getStream(picture));
            if (stateIsValid(putObjectResult)) {
                keys.add(key);
            } else {
                throw new ServiceException("上传WebP图片失败，键：" + key);
            }
        }
        return objectMapper.writeValueAsString(keys);
    }

    /**
     * 删除单个WebP文件
     *
     * @param key 要删除的文件键
     */
    @CacheEvict("WebPStorage")
    @RedisLock(key = "'lock:WebPStorage:removeSingleWebP:' + #key")
    public void removeSingleWebP(String key) {
        if (!obsClient.doesObjectExist(bucketName, key)) {
            throw new ServiceException("桶中未找到键：" + key);
        }
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
        String[] keys = commonService.convertStringArray(keysArrayString);
        for (String key : keys) {
            if (!obsClient.doesObjectExist(bucketName, key)) {
                throw new ServiceException("桶中未找到键：" + key);
            }
        }
        for (String key : keys) {
            if (!deleteKey(key)) {
                throw new ServiceException("删除多个WebP图片失败，错误的键：" + key);
            }
        }
    }

    /**
     * 批量删除冗余的key
     * @param existedKeys 数据库中的所有key
     */
    public void deleteRedundantKeys(List<String> existedKeys) {
        List<String> keys = getAllKeys(); //获取线上OBS的所有Key
        keys.removeAll(existedKeys); //从OBS的keys中减去数据库中包含的所有有效keys，得到冗余keys
        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName);
        for (String redundantKey : keys) {
            deleteObjectsRequest.addKeyAndVersion(redundantKey);
        }
        if (deleteObjectsRequest.getKeyAndVersions().length > 0) {
            obsClient.deleteObjects(deleteObjectsRequest);
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
        } catch (Exception e) {
            log.error("getObjectContent failed: {}", e.getMessage());
        }
        return content;
    }
}
