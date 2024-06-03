package com.arknightsinfrastructurestationbackend.entitiy.workFile;

import com.arknightsinfrastructurestationbackend.entitiy.workFile.adapter.WorkFileInterface;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 暂存的作业
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("`staging_work_files`")
public class StagingWorkFile implements WorkFileInterface {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id; //作业ID

    private String name; //作业名称

    //只有登录后才可以使用“暂存”功能，因此暂存的作业这两项不能为空
    private String author; //作业作者，非空

    private Long authorId; //作业作者的ID，非空

    private String type; //作业类型

    private String layout; //作业采用的基建布局

    private String stagingDate; //作业暂存时间，非空，该字符串的格式已被getCurrentDateTime设定为适配MySQL的datetime格式，只有这个是要求非空的

    private String description; //作业描述

    private String storageType; //作业内容存储类型

    private String fileContent; //作业文件内容

    private String fileRequest; //作业文件要求，由前端设定，包括基建设施排布顺序（Mower限定）、干员精英化要求
}
