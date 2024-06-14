package com.arknightsinfrastructurestationbackend.service.utils;

import com.arknightsinfrastructurestationbackend.entitiy.user.User;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.RecyclingWorkFile;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.StagingWorkFile;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.WorkFile;
import com.arknightsinfrastructurestationbackend.mapper.workFile.RecyclingWorkFileMapper;
import com.arknightsinfrastructurestationbackend.mapper.workFile.StagingWorkFileMapper;
import com.arknightsinfrastructurestationbackend.mapper.workFile.WorkFileMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@AllArgsConstructor
public class CommonService {
    private final WorkFileMapper workFileMapper;
    private final StagingWorkFileMapper stagingWorkFileMapper;
    private final RecyclingWorkFileMapper recyclingWorkFileMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 用户名称同步
     * 当用户修改自己的用户名后，他发布的所有作业中的“发布者”名称会同步修改
     *
     * @param user 要同步的用户对象
     * @return 是否同步成功
     */
    public boolean syncFileAuthor(User user) {
        boolean updated = false;

        Long userId = user.getId();
        String userName = user.getUsername();

        List<WorkFile> workFiles = workFileMapper.selectList(new QueryWrapper<WorkFile>().lambda().eq(WorkFile::getAuthorId, userId));
        if (workFiles != null && !workFiles.isEmpty()) {
            workFiles.forEach(file -> {
                file.setAuthor(userName);
                workFileMapper.updateById(file);
            });
            updated = true;
        }

        List<StagingWorkFile> stagingWorkFiles = stagingWorkFileMapper.selectList(new QueryWrapper<StagingWorkFile>().lambda().eq(StagingWorkFile::getAuthorId, userId));
        if (stagingWorkFiles != null && !stagingWorkFiles.isEmpty()) {
            stagingWorkFiles.forEach(file -> {
                file.setAuthor(userName);
                stagingWorkFileMapper.updateById(file);
            });
            updated = true;
        }

        List<RecyclingWorkFile> recyclingWorkFiles = recyclingWorkFileMapper.selectList(new QueryWrapper<RecyclingWorkFile>().lambda().eq(RecyclingWorkFile::getAuthorId, userId));
        if (recyclingWorkFiles != null && !recyclingWorkFiles.isEmpty()) {
            recyclingWorkFiles.forEach(file -> {
                file.setAuthor(userName);
                recyclingWorkFileMapper.updateById(file);
            });
            updated = true;
        }

        return updated;
    }

    /**
     * 获取当前时间，以yyyy-MM-dd HH:mm:ss格式返回
     */
    public String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

    /**
     * Rounds a float to the specified number of decimal places.
     *
     * @param value  the float value to be rounded
     * @param places the number of decimal places to round to
     * @return the rounded float value
     */
    public float round(float value, int places) {
        if (places < 0) throw new IllegalArgumentException("位数必须是非负的");

        BigDecimal bd = new BigDecimal(Float.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    /**
     * 将数组形式的字符串转换为字符串数组
     *
     * @param stringArray 字符串形式的数组
     * @return 字符串数组
     * @throws JsonProcessingException 解析异常
     */
    public String[] convertStringArray(String stringArray) throws JsonProcessingException {
        //"null"是前端JSON.stringify(null)的结果
        if (stringArray == null || stringArray.isBlank() || "null".equals(stringArray)) {
            return null;
        }
        return objectMapper.readValue(stringArray, String[].class);
    }
}
