package com.arknightsinfrastructurestationbackend.service.workFile.adapter;

import com.arknightsinfrastructurestationbackend.common.tools.JsonWorkProcessor;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.dto.query.WorkFileSimpleSearch;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

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
}
