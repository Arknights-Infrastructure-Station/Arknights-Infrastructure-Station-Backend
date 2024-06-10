package com.arknightsinfrastructurestationbackend.service.user;

import com.arknightsinfrastructurestationbackend.common.exception.ServiceException;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.entitiy.user.User;
import com.arknightsinfrastructurestationbackend.entitiy.user.UserRate;
import com.arknightsinfrastructurestationbackend.global.type.ScoreNumber;
import com.arknightsinfrastructurestationbackend.mapper.user.UserRateMapper;
import com.arknightsinfrastructurestationbackend.service.utils.CommonService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 作业评分服务层
 */
@Service
@AllArgsConstructor
public class UserRateService {
    private final UserRateMapper userRateMapper;
    private final SelectUserService selectUserService;
    private final CommonService commonService;

    public OperateResult rate(String token, UserRate userRate) {
        User user = selectUserService.getUserByToken(token);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }

        userRate.setUid(user.getId());
        Float value = userRate.getScore();
        LambdaQueryWrapper<UserRate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserRate::getUid, userRate.getUid()).eq(UserRate::getWid, userRate.getWid());
        UserRate userRateExisted = userRateMapper.selectOne(queryWrapper);
        if (userRateExisted != null) {
            //已有评分记录
            if (value == ScoreNumber.NULL.getValue()) {
                //如果用户的评分值为-1（由前端自动指定），那么删除该用户对这条作业的评分记录
                if (userRateMapper.delete(queryWrapper) > 0) {
                    return new OperateResult(200, "取消评分成功");
                } else {
                    return new OperateResult(500, "取消评分失败");
                }
            } else if (value == ScoreNumber.LIKE.getValue() || value == ScoreNumber.DISLIKE.getValue()) {
                //前端已经做了筛选，只有在评分值与原来不同时才会调用评分方法
                userRateExisted.setScore(userRate.getScore());
                if (userRateMapper.update(userRateExisted, queryWrapper) > 0) {
                    return new OperateResult(200, "评分修改成功");
                } else {
                    return new OperateResult(500, "评分修改失败");
                }
            } else {
                return new OperateResult(400, "评分不合理");
            }
        } else {
            //还没有评分记录
            if (value == ScoreNumber.NULL.getValue()) {
                //一般不会走到这里，前端作业默认评分是-1，备份的也会是-1，除非修改为“赞”或“踩”中的任意一个，否则不会调用rate方法
                return new OperateResult(404, "评分记录不存在");
            } else if (value == ScoreNumber.LIKE.getValue() || value == ScoreNumber.DISLIKE.getValue()) {
                UserRate newUserRate = new UserRate(userRate.getUid(), userRate.getWid(), userRate.getScore());
                if (userRateMapper.insert(newUserRate) > 0) {
                    return new OperateResult(200, "评分成功");
                } else {
                    return new OperateResult(500, "评分失败");
                }
            } else {
                return new OperateResult(400, "评分不合理");
            }
        }
    }

    public List<UserRate> getRateRecordsForUser(String token) {
        User user = selectUserService.getUserByToken(token);
        if (user == null) {
            throw new ServiceException("用户未找到");
        }

        LambdaQueryWrapper<UserRate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserRate::getUid, user.getId());
        return userRateMapper.selectList(queryWrapper);
    }

    /**
     * 计算作业评分
     *
     * @param workFileSimpleSearch 仅包含作业ID的DTO
     * @return 作业评分
     */
    public Float getScoreForWork(Long wid) {
        LambdaQueryWrapper<UserRate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserRate::getWid, wid);
        List<UserRate> userRates = userRateMapper.selectList(queryWrapper);
        if (userRates.isEmpty())
            return -1f;
        float sum = 0;
        for (UserRate userRate : userRates) {
            sum += userRate.getScore();
        }
        sum /= userRates.size();
        sum *= 5; //对接前端的rate组件，满分为5分，以得分率换算分数
        return commonService.round(sum, 2); //保留2位小数
    }
}
