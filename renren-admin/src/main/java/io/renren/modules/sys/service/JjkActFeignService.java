package io.renren.modules.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.common.utils.PageUtils;
import io.renren.modules.sys.entity.JjkActFeignEntity;

import java.util.Map;

/**
 * 
 *
 * @author Mark
 * @email sunlightcs@gmail.com
 * @date 2019-09-20 15:06:12
 */
public interface JjkActFeignService extends IService<JjkActFeignEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

