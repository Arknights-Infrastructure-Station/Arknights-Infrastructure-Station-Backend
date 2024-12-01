package com.arknightsinfrastructurestationbackend.service.workFile.adapter;

import com.arknightsinfrastructurestationbackend.common.tools.JsonWorkProcessor;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.dto.query.admin.StarRecordAdminScreen;
import com.arknightsinfrastructurestationbackend.dto.query.admin.WorkFileAdminScreen;
import com.arknightsinfrastructurestationbackend.dto.query.user.WorkFileSimpleSearch;
import com.arknightsinfrastructurestationbackend.global.type.SortOrderType;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AdapterService {
    public OperateResult mowerAdapt(String sourceJson, String requireJson) {
        return JsonWorkProcessor.exchangeRoomDataForMower(sourceJson, requireJson);
    }

    public <T> QueryWrapper<T> createLimitedQueryWrapper(WorkFileSimpleSearch workFileSimpleSearch, Long userId, Class<T> clazz) {
        QueryWrapper<T> queryWrapper = createQueryWrapper(workFileSimpleSearch, userId, clazz);

        int currentPage = workFileSimpleSearch.getCurrentPage();
        int pageSize = workFileSimpleSearch.getPageSize();
        int offset = (currentPage - 1) * pageSize;
        queryWrapper.last("LIMIT " + offset + "," + pageSize);

        return queryWrapper;
    }

    public <T> QueryWrapper<T> createQueryWrapper(WorkFileSimpleSearch workFileSimpleSearch, Long userId, Class<T> clazz) {
        //除非实现同一个接口，否则泛型只能写死查询字段，考虑到不同作业文件之间的差别，暂不采用实现同一个接口的做法
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        if (userId != null) {
            queryWrapper.eq("author_id", userId);
        }

        if (workFileSimpleSearch.getWorkQuery() != null && !workFileSimpleSearch.getWorkQuery().isEmpty()) {
            queryWrapper.and(wrapper -> {
                wrapper.like("name", workFileSimpleSearch.getWorkQuery())
                        .or().like("type", workFileSimpleSearch.getWorkQuery())
                        .or().like("layout", workFileSimpleSearch.getWorkQuery())
                        .or().like("description", workFileSimpleSearch.getWorkQuery())
                        .or().like("file_content", workFileSimpleSearch.getWorkQuery())
                        .or().like("file_request", workFileSimpleSearch.getWorkQuery())
                        .or().like("storage_type", workFileSimpleSearch.getWorkQuery());

                // StagingWorkFile不存在 releaseDate 字段
                try {
                    clazz.getDeclaredField("releaseDate");
                    wrapper.or().like("release_date", workFileSimpleSearch.getWorkQuery());
                } catch (NoSuchFieldException ignored) {
                }
                try {
                    clazz.getDeclaredField("stagingDate");
                    wrapper.or().like("staging_date", workFileSimpleSearch.getWorkQuery());
                } catch (NoSuchFieldException ignored) {
                }
            });
        }

        // 按创建时间倒序排序
        try {
            clazz.getDeclaredField("releaseDate");
            queryWrapper.orderByDesc("release_date");
        } catch (NoSuchFieldException ignored) {
        }
        try {
            clazz.getDeclaredField("stagingDate");
            queryWrapper.orderByDesc("staging_date");
        } catch (NoSuchFieldException ignored) {
        }

        return queryWrapper;
    }

    public <T> QueryWrapper<T> createAdminLimitedQueryWrapper(WorkFileAdminScreen screen, Class<T> entityClass) {
        QueryWrapper<T> queryWrapper = createAdminQueryWrapper(screen, entityClass);

        // 分页
        int currentPage = screen.getCurrentPage();
        int pageSize = screen.getPageSize();
        int offset = Math.max((currentPage - 1) * pageSize, 0);
        queryWrapper.last("LIMIT " + offset + "," + pageSize);

        return queryWrapper;
    }

    public <T> QueryWrapper<T> createAdminQueryWrapper(WorkFileAdminScreen screen, Class<T> entityClass) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();

        // 作业类型
        if (screen.getType() != null && !"全部".equals(screen.getType())) {
            queryWrapper.eq("type", screen.getType());
        }

        // 布局
        if (screen.getLayout() != null && !"全部".equals(screen.getLayout())) {
            queryWrapper.eq("layout", screen.getLayout());
        }

        // 日期范围
        if (screen.getDateRange() != null && screen.getDateRange().size() == 2) {
            queryWrapper.between("release_date", screen.getDateRange().get(0), screen.getDateRange().get(1));
        }

        // 模糊查询
        if (screen.getWorkQuery() != null && !screen.getWorkQuery().isEmpty()) {
            queryWrapper.and(wrapper ->
                    wrapper.like("name", screen.getWorkQuery())
                            .or().like("description", screen.getWorkQuery())
                            .or().like("author", screen.getWorkQuery())
                            .or().like("author_id", screen.getWorkQuery()));
        }

        // 用户ID列表
        if (screen.getUserIds() != null && !screen.getUserIds().isEmpty()) {
            List<Long> userIds = screen.getUserIds().stream().map(Long::parseLong).collect(Collectors.toList());
            queryWrapper.in("author_id", userIds);
        }

        // 下载次数范围
        if (screen.getDownloadNumberRange() != null && screen.getDownloadNumberRange().size() == 2) {
            queryWrapper.between("download_number", screen.getDownloadNumberRange().get(0), screen.getDownloadNumberRange().get(1));
        }

        // 收藏次数范围
        if (screen.getStarNumberRange() != null && screen.getStarNumberRange().size() == 2) {
            queryWrapper.between("star_number", screen.getStarNumberRange().get(0), screen.getStarNumberRange().get(1));
        }

        // 通关时间范围（针对 RecyclingWorkFile）
        if (screen.getClearTimeRange() != null && screen.getClearTimeRange().size() == 2) {
            queryWrapper.between("clear_time", screen.getClearTimeRange().get(0), screen.getClearTimeRange().get(1));
        }

        // 暂存日期范围（针对 StagingWorkFile）
        if (screen.getStagingDateRange() != null && screen.getStagingDateRange().size() == 2) {
            queryWrapper.between("staging_date", screen.getStagingDateRange().get(0), screen.getStagingDateRange().get(1));
        }

        // 存储类型
        if (screen.getStorageType() != null && !screen.getStorageType().isEmpty()) {
            queryWrapper.eq("storage_type", screen.getStorageType());
        }

        // 评分范围
        if (screen.getScoreRange() != null && screen.getScoreRange().size() == 2) {
            queryWrapper.between("score", screen.getScoreRange().get(0), screen.getScoreRange().get(1));
        }

        // 排序
        if (SortOrderType.RELEASE_DATE_DESC.getValue().equals(screen.getSortOrder()))
            queryWrapper.orderByDesc("release_date");
        else if (SortOrderType.RELEASE_DATE_ASC.getValue().equals(screen.getSortOrder()))
            queryWrapper.orderByAsc("release_date");
        else if (SortOrderType.SCORE_DESC.getValue().equals(screen.getSortOrder()))
            queryWrapper.orderByDesc("score");
        else if (SortOrderType.SCORE_ASC.getValue().equals(screen.getSortOrder()))
            queryWrapper.orderByAsc("score");

        return queryWrapper;
    }

    public <T> QueryWrapper<T> createAdminStarRecordQueryWrapper(StarRecordAdminScreen screen) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();

        if (screen.getUserIds() != null && !screen.getUserIds().isEmpty()) {
            List<Long> userIds = screen.getUserIds().stream().map(Long::parseLong).collect(Collectors.toList());
            queryWrapper.in("uid", userIds);
        }

        if (screen.getWorkFileIds() != null && !screen.getWorkFileIds().isEmpty()) {
            List<Long> workFileIds = screen.getWorkFileIds().stream().map(Long::parseLong).collect(Collectors.toList());
            queryWrapper.in("wid", workFileIds);
        }

        if (screen.getDateRange() != null && screen.getDateRange().size() == 2) {
            queryWrapper.between("star_date", screen.getDateRange().get(0), screen.getDateRange().get(1));
        }

        // 分页
        int currentPage = screen.getCurrentPage();
        int pageSize = screen.getPageSize();
        int offset = Math.max((currentPage - 1) * pageSize, 0);
        queryWrapper.last("LIMIT " + offset + "," + pageSize);

        return queryWrapper;
    }
}
