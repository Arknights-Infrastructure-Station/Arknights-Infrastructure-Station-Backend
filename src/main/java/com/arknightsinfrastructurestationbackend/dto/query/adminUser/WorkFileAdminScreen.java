package com.arknightsinfrastructurestationbackend.dto.query.adminUser;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Data
public class WorkFileAdminScreen {
    private String wid; // 作业ID
    private String type; // 作业类型
    private String layout; // 作业布局
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private List<Date> dateRange; // 日期范围
    private String workQuery; // 模糊查询字符串，涵盖作业名称、作业描述、发布者名称、发布者ID
    private String sortOrder; // 排序方式
    private int currentPage; // 当前页
    private int pageSize; // 页大小

    // 管理端特有的检索字段
    private List<String> userIds; // 用户 ID 列表（字符串类型）
    private List<Integer> downloadNumberRange; // 下载次数范围
    private List<Integer> starNumberRange; // 收藏次数范围
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private List<Date> clearTimeRange; // 通关时间范围
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private List<Date> stagingDateRange; // 暂存日期范围
    private String storageType; // 存储类型
    private List<Float> scoreRange; // 评分范围
}
