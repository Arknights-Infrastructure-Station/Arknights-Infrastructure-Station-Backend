package com.arknightsinfrastructurestationbackend.service.workFile.user.adminUser;

import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.dto.query.adminUser.StarRecordAdminScreen;
import com.arknightsinfrastructurestationbackend.entitiy.user.ordinaryUser.StarRecord;
import com.arknightsinfrastructurestationbackend.mapper.user.ordinaryUser.StarRecordMapper;
import com.arknightsinfrastructurestationbackend.service.workFile.adapter.AdapterService;
import com.arknightsinfrastructurestationbackend.service.workFile.user.commonUser.BaseStarRecordService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理员专用的收藏记录服务类。
 * <p>管理员可直接删除指定用户的收藏记录，并根据筛选条件查询收藏记录。</p>
 */
@Service
public class StarRecordAdminService extends BaseStarRecordService {

    public StarRecordAdminService(StarRecordMapper starRecordMapper,
                                  AdapterService adapterService) {
        super(starRecordMapper, adapterService);
    }

    /**
     * 管理员删除指定用户的收藏记录。
     *
     * @param wid 作业文件ID
     * @param uid 用户ID
     * @return 操作结果
     */
    public OperateResult deleteStarRecord(Long wid, Long uid) {
        StarRecord existingRecord = getStarRecord(wid, uid);
        if (existingRecord == null) {
            return new OperateResult(404, "收藏记录不存在");
        }

        if (starRecordMapper.delete(new LambdaQueryWrapper<StarRecord>()
                .eq(StarRecord::getWid, wid)
                .eq(StarRecord::getUid, uid)) > 0) {
            return new OperateResult(200, "取消收藏成功");
        } else {
            return new OperateResult(500, "取消收藏失败");
        }
    }

    /**
     * 管理员根据筛选条件筛选收藏记录列表。
     *
     * @param starRecordAdminScreen 管理员筛选条件
     * @return 满足条件的StarRecord列表
     */
    public List<StarRecord> screenStarRecordList(StarRecordAdminScreen starRecordAdminScreen) {
        return getStarRecordList(starRecordAdminScreen);
    }
}

