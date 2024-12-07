package com.arknightsinfrastructurestationbackend.service.workFile.user.commonUser;

import com.arknightsinfrastructurestationbackend.dto.query.adminUser.StarRecordAdminScreen;
import com.arknightsinfrastructurestationbackend.entitiy.user.ordinaryUser.StarRecord;
import com.arknightsinfrastructurestationbackend.mapper.user.ordinaryUser.StarRecordMapper;
import com.arknightsinfrastructurestationbackend.service.workFile.adapter.AdapterService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 基础的收藏记录服务类。
 * <p>抽象出收藏记录的通用查询和操作逻辑，为子类提供公共方法。</p>
 */
@Service
@AllArgsConstructor
public abstract class BaseStarRecordService {
    protected final StarRecordMapper starRecordMapper;
    protected final AdapterService adapterService;

    /**
     * 根据工作ID和用户ID获取收藏记录。
     *
     * @param wid 作业文件ID
     * @param uid 用户ID
     * @return 如果存在则返回StarRecord对象，否则返回null
     */
    protected StarRecord getStarRecord(Long wid, Long uid) {
        LambdaQueryWrapper<StarRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StarRecord::getWid, wid)
                .eq(StarRecord::getUid, uid);
        return starRecordMapper.selectOne(queryWrapper);
    }

    /**
     * 根据管理员的筛选条件获取收藏记录列表。
     *
     * @param starRecordAdminScreen 管理员筛选条件
     * @return 满足条件的StarRecord列表
     */
    protected List<StarRecord> getStarRecordList(StarRecordAdminScreen starRecordAdminScreen) {
        QueryWrapper<StarRecord> queryWrapper = adapterService.createAdminStarRecordQueryWrapper(starRecordAdminScreen);
        return starRecordMapper.selectList(queryWrapper);
    }

    /**
     * 根据用户ID获取所有收藏的工作文件ID列表。
     *
     * @param uid 用户ID
     * @return 收藏的工作文件ID列表
     */
    protected List<Long> getUserStarredWorkIds(Long uid) {
        return starRecordMapper.selectList(new LambdaQueryWrapper<StarRecord>()
                        .eq(StarRecord::getUid, uid))
                .stream()
                .map(StarRecord::getWid)
                .collect(Collectors.toList());
    }
}
