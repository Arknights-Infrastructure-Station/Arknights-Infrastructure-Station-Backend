package com.arknightsinfrastructurestationbackend.service.workFile;

import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.dto.query.WorkFileSimpleSearch;
import com.arknightsinfrastructurestationbackend.entitiy.user.StarRecord;
import com.arknightsinfrastructurestationbackend.entitiy.user.User;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.RecyclingWorkFile;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.WorkFile;
import com.arknightsinfrastructurestationbackend.mapper.user.StarRecordMapper;
import com.arknightsinfrastructurestationbackend.mapper.workFile.RecyclingWorkFileMapper;
import com.arknightsinfrastructurestationbackend.mapper.workFile.WorkFileMapper;
import com.arknightsinfrastructurestationbackend.service.user.SelectUserService;
import com.arknightsinfrastructurestationbackend.service.utils.CommonService;
import com.arknightsinfrastructurestationbackend.service.workFile.adapter.AdapterService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class StaredWorkFileService {
    private final StarRecordMapper starRecordMapper;
    private final WorkFileMapper workFileMapper;
    private final RecyclingWorkFileMapper recyclingWorkFileMapper;
    private final SelectUserService selectUserService;
    private final CommonService commonService;
    private final AdapterService adapterService;

    //插入收藏记录（收藏某个作业）
    public OperateResult insertStarRecord(String token, Long wid) {
        User user = selectUserService.getUserByToken(token);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }

        LambdaQueryWrapper<StarRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StarRecord::getWid, wid)
                .eq(StarRecord::getUid, user.getId());
        if (!starRecordMapper.selectList(queryWrapper).isEmpty()) {
            return new OperateResult(409, "该作业已被收藏");
        }

        if (workFileMapper.selectById(wid) == null) {
            return new OperateResult(404, "作业记录不存在");
        }

        StarRecord starRecord = new StarRecord(wid, user.getId(), commonService.getCurrentDateTime());
        if (starRecordMapper.insert(starRecord) > 0) {
            return new OperateResult(200, "作业收藏成功");
        } else {
            return new OperateResult(500, "作业收藏失败");
        }
    }

    //删除收藏记录（取消收藏某个作业）
    public OperateResult deleteStarRecord(String token, Long wid) {
        User user = selectUserService.getUserByToken(token);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }

        LambdaQueryWrapper<StarRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StarRecord::getWid, wid)
                .eq(StarRecord::getUid, user.getId());
        StarRecord existingRecord = starRecordMapper.selectOne(queryWrapper);
        if (existingRecord == null) {
            return new OperateResult(404, "收藏记录不存在");
        }

        if (starRecordMapper.delete(queryWrapper) > 0) {
            return new OperateResult(200, "取消收藏成功");
        } else {
            return new OperateResult(500, "取消收藏失败");
        }
    }

    public List<StarRecord> getStarListForUser(String token) {
        User user = selectUserService.getUserByToken(token);
        if (user == null) {
            return Collections.emptyList();
        }
        return starRecordMapper.selectList(new LambdaQueryWrapper<StarRecord>()
                .eq(StarRecord::getUid, user.getId()));
    }

    //查询某个作业的收藏数量
    public int getStarNumberForWork(Long wid) {
        LambdaQueryWrapper<StarRecord> widQueryWrapper = new LambdaQueryWrapper<>();
        widQueryWrapper.eq(StarRecord::getWid, wid);
        return Math.toIntExact(starRecordMapper.selectCount(widQueryWrapper));
    }

    /**
     * 根据用户设置的筛选条件查询出指定数量的收藏作业列表
     *
     * @param token                用户token
     * @param workFileSimpleSearch 作业筛选简单参数
     * @return 符合条件的收藏作业列表
     */
    @Cacheable("startedWorkFileList")
    public List<WorkFile> screenStaredWorkFileList(String token, WorkFileSimpleSearch workFileSimpleSearch) {
        List<Long> workIds = getUserStarredWorkIds(token);
        if (workIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<WorkFile> workFileList = getUserStarredWorkFiles(workFileSimpleSearch, workIds);

        // 分页处理
        int start = (workFileSimpleSearch.getCurrentPage() - 1) * workFileSimpleSearch.getPageSize();
        int end = Math.min(start + workFileSimpleSearch.getPageSize(), workFileList.size());

        if (start > end) {
            return Collections.emptyList();
        }

        return workFileList.subList(start, end);
    }

    /**
     * 获取某个用户收藏的作业总数
     *
     * @param token 用户token
     * @return 该用户的收藏的作业总数
     */
    @Cacheable("startedWorkFileListCount")
    public int getStaredWorkFileListCount(String token, WorkFileSimpleSearch workFileSimpleSearch) {
        List<Long> workIds = getUserStarredWorkIds(token);
        if (workIds.isEmpty()) {
            return 0;
        }

        List<WorkFile> workFileList = getUserStarredWorkFiles(workFileSimpleSearch, workIds);
        return workFileList.size();
    }

    /**
     * 获取用户收藏的所有作业ID
     *
     * @param token 用户token
     * @return 用户收藏的所有作业ID列表
     */
    private List<Long> getUserStarredWorkIds(String token) {
        User user = selectUserService.getUserByToken(token);
        if (user == null) {
            return Collections.emptyList();
        }

        return starRecordMapper.selectList(new LambdaQueryWrapper<StarRecord>()
                        .eq(StarRecord::getUid, user.getId()))
                .stream()
                .map(StarRecord::getWid)
                .collect(Collectors.toList());
    }

    /**
     * 根据筛选条件和作业ID列表查询作业文件
     *
     * @param workFileSimpleSearch 作业筛选简单参数
     * @param workIds              作业ID列表
     * @return 符合条件的作业文件列表
     */
    private List<WorkFile> getUserStarredWorkFiles(WorkFileSimpleSearch workFileSimpleSearch, List<Long> workIds) {
        // 查询这些作业文件
        QueryWrapper<WorkFile> workFileQueryWrapper = adapterService.createLimitedQueryWrapper(workFileSimpleSearch, null, WorkFile.class);
        workFileQueryWrapper.in("id", workIds);
        List<WorkFile> workFileList = workFileMapper.selectList(workFileQueryWrapper);

        // 查询不再公开的，已经被加入待回收作业表但尚未被清除的作业，用以给用户提示
        QueryWrapper<RecyclingWorkFile> recyclingWorkFileQueryWrapper = adapterService.createLimitedQueryWrapper(workFileSimpleSearch, null, RecyclingWorkFile.class);
        recyclingWorkFileQueryWrapper.in("id", workIds);
        List<RecyclingWorkFile> recyclingWorkFileList = recyclingWorkFileMapper.selectList(recyclingWorkFileQueryWrapper);

        for (RecyclingWorkFile recyclingWorkFile : recyclingWorkFileList) {
            // 对于这些作业，发布者不再想要其他用户去下载它，所以将作业内容设置为空字符串
            recyclingWorkFile.setFileContent("");
            workFileList.add(recyclingWorkFile.toWorkFile());
        }

        return workFileList;
    }

}
