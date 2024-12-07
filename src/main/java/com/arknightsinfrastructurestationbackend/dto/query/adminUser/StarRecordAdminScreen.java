package com.arknightsinfrastructurestationbackend.dto.query.adminUser;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Data
public class StarRecordAdminScreen {
    private List<String> userIds; // 用户 ID 列表（字符串类型）
    private List<String> workFileIds; // 作业 ID 列表（字符串类型）
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private List<Date> dateRange; // 收藏日期范围
    private int currentPage; // 当前页
    private int pageSize; // 页大小
}
