package com.arknightsinfrastructurestationbackend.dto.query;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Data
public class WorkFileScreen {
    private String type; //作业类型
    private String layout; //作业布局
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") //日期格式化注解
    private List<Date> dateRange; //日期范围
    private String workQuery; //模糊查询字符串，涵盖作业名称、作业描述、发布者名称、发布者ID
    private int currentPage; //当前页
    private int pageSize; //页大小

    /*
    自定义适配功能由前端实现，原因如下：
    1.要满足缓存键的构建需求，前端必须将自定义养成练度和基建排布配置一起传给后端
    ，否则会一直触发缓存导致适配功能开启而无实际效果，但“前端传输练度和排布数据”这个行动是多此一举，因为后端本就有用户的数据；

    2.额外的数据传输会增大响应延迟，后端会花额外的时间去处理这种请求，既然后端已经将用户数据返回给前端，那么这部分筛选需求完全可由用户的计算机来满足；

    3.Mybatis Plus没有提供直接操作和检测字段内容的支持方式，从而很难从简地满足分页的完整性需求，后端实现这个需求便没有显著优势。
     */
}
