package com.arknightsinfrastructurestationbackend.service.buckets;

import com.arknightsinfrastructurestationbackend.common.aspect.redisLock.RedisLock;
import com.arknightsinfrastructurestationbackend.common.tools.Log;
import com.obs.services.ObsClient;
import com.obs.services.exception.ObsException;
import com.obs.services.model.ObsObject;
import lombok.AllArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class MowerBucketService {
    private final String ak = System.getenv("HUAWEICLOUD_OBS_ACCESS_KEY_ID");
    private final String sk = System.getenv("HUAWEICLOUD_OBS_SECRET_ACCESS_KEY_ID");
    private final String endPoint = "https://obs.cn-north-4.myhuaweicloud.com";
    private final String bucketName = "mower";
    private final ObsClient obsClient = new ObsClient(ak, sk, endPoint);

    private final PNGKeysService pngKeysService;
    private final RedissonClient redissonClient;


    /**
     * 上传PNG文件字节流
     *
     * @param dataUrl png文件数据url字符串
     * @return 生成的，用于存储的桶键
     */
    public String uploadPng(String dataUrl) {
        String key = null;
        try {
            // 提取Base64数据部分
            String base64Data = dataUrl.split(",")[1];
            // 解码Base64数据
            byte[] pictureBytes = Base64.getDecoder().decode(base64Data);

            key = pngKeysService.addKey();
            obsClient.putObject(bucketName, key, new ByteArrayInputStream(pictureBytes));
            return key;
        } catch (ObsException e) {
            Log.error("putObject failed");
            // 请求失败,打印http状态码
            Log.error("HTTP Code:" + e.getResponseCode());
            // 请求失败,打印服务端错误码
            Log.error("Error Code:" + e.getErrorCode());
            // 请求失败,打印详细错误信息
            Log.error("Error Message:" + e.getErrorMessage());
            // 请求失败,打印请求id
            Log.error("Request ID:" + e.getErrorRequestId());
            Log.error("Host ID:" + e.getErrorHostId());
            if (key != null) {
                pngKeysService.deleteKey(key);
            }
        } catch (Exception e) {
            Log.error("putObject failed" + e.getMessage());
        }
        return null; //返回null代表对象存储失败
    }

    /**
     * 下载PNG文件字节流并以String格式返回
     * 原始方法，无分布式锁
     *
     * @param objectKey 要提取的桶对象的键
     * @return 桶对象字符串
     */
    private String downloadPng(String objectKey) {
        String content = null;
        try {
            ObsObject obsObject = obsClient.getObject(bucketName, objectKey);
            InputStream input = obsObject.getObjectContent();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int sizeInBytes = 3 * 1024 * 1024; // 3 MB
            byte[] b = new byte[sizeInBytes];
            int len;
            while ((len = input.read(b)) != -1) {
                bos.write(b, 0, len);
            }
            content = "data:image/png;base64," + Base64.getEncoder().encodeToString(bos.toByteArray());
            bos.close();
            input.close();
        } catch (ObsException e) {
            Log.error("getObjectContent failed");
            Log.error("HTTP Code:" + e.getResponseCode());
            Log.error("Error Code:" + e.getErrorCode());
            Log.error("Error Message:" + e.getErrorMessage());
            Log.error("Request ID:" + e.getErrorRequestId());
            Log.error("Host ID:" + e.getErrorHostId());
        } catch (Exception e) {
            Log.error("getObjectContent failed" + e.getMessage());
        }
        return content;
    }

    /**
     * 下载PNG文件字节流并以String格式返回
     * 包装方法，配备分布式锁
     *
     * @param objectKey 要提取的桶对象的键
     * @return 桶对象字符串
     */
    @Cacheable("PNGStorage")
    @RedisLock(key = "'lock:PNGStorage:syncDownloadPng:' + #objectKey")
    public String syncDownloadPng(String objectKey) {
        return downloadPng(objectKey);
    }

    /*
    该方法其实不需要配备@CacheEvict注解，因为该方法是服务层顺带调用的，作业在删除后前端就看不到了，
    也就不会再触发该作业的缓存键，不会出现异常显示问题，直接等待该缓存键过期就行
    这里还是标注了@CacheEvict是因为能及时释放缓存占用的内存空间
     */

    /**
     * 删除文件
     *
     * @param objectKey 要删除的桶对象的键
     */
    @CacheEvict("PNGStorage")
    public void deletePng(String objectKey) {
        try {
            pngKeysService.deleteKey(objectKey);
            obsClient.deleteObject(bucketName, objectKey);
            Log.error("deleteObject successfully");
        } catch (ObsException e) {
            Log.error("deleteObject failed");
            Log.error("HTTP Code:" + e.getResponseCode());
            Log.error("Error Code:" + e.getErrorCode());
            Log.error("Error Message:" + e.getErrorMessage());
            Log.error("Request ID:" + e.getErrorRequestId());
            Log.error("Host ID:" + e.getErrorHostId());
        } catch (Exception e) {
            Log.error("deleteObject failed");
        }
    }
}
