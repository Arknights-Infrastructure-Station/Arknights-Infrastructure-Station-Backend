package com.arknightsinfrastructurestationbackend.dto.query;

import lombok.Data;

@Data
public class WorkFileSimpleSearch {
    private String wid; //操作作业的ID，可用于将已发布作业加入回收箱、删除回收箱作业、删除暂存作业
    private String workQuery; //模糊查询字符串，涵盖作业名称、作业类型、作业布局、作业发布日期（仅暂存作业没有）、作业描述、作业内容、作业精英化要求、作业存储类型
    private int currentPage; //当前页
    private int pageSize; //页大小
}
