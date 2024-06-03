package com.arknightsinfrastructurestationbackend.service.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.entitiy.user.DownloadRecord;
import com.arknightsinfrastructurestationbackend.entitiy.user.User;
import com.arknightsinfrastructurestationbackend.mapper.user.DownloadRecordMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DownloadRecordService {
    private final DownloadRecordMapper downloadRecordMapper;
    private final SelectUserService selectUserService;

    public OperateResult tryIncreaseDownloadCount(String token, Long wid) {
        User user = selectUserService.getUserByToken(token);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }

        Long uid = user.getId();
        // 检查是否已存在下载记录
        LambdaQueryWrapper<DownloadRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DownloadRecord::getWid, wid)
                .eq(DownloadRecord::getUid, user.getId());
        if (downloadRecordMapper.selectCount(queryWrapper) == 0) {
            // 如果不存在下载记录，则插入新记录
            DownloadRecord downloadRecord = new DownloadRecord(wid, uid);
            int insertResult = downloadRecordMapper.insert(downloadRecord);

            if (insertResult > 0) {
                return new OperateResult(200, "下载记录增加成功");
            } else {
                return new OperateResult(500, "下载记录增加失败");
            }
        } else {
            // 如果已存在下载记录，可以选择返回一个不同的结果或者同样的结果
            return new OperateResult(200, "下载记录已存在");
        }
    }

    //查询某个作业的下载数量
    public int getDownloadNumberForWork(Long wid) {
        LambdaQueryWrapper<DownloadRecord> widQueryWrapper = new LambdaQueryWrapper<>();
        widQueryWrapper.eq(DownloadRecord::getWid, wid);
        return Math.toIntExact(downloadRecordMapper.selectCount(widQueryWrapper));
    }
}

