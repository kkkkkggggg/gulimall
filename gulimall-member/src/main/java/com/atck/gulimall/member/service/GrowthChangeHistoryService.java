package com.atck.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atck.common.utils.PageUtils;
import com.atck.gulimall.member.entity.GrowthChangeHistoryEntity;

import java.util.Map;

/**
 * 成长值变化历史记录
 *
 * @author kkkkk
 * @email chenk3166@gmail.com
 * @date 2022-01-01 13:14:32
 */
public interface GrowthChangeHistoryService extends IService<GrowthChangeHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

