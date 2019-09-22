package io.renren.modules.jjk.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.common.utils.PageUtils;
import io.renren.modules.jjk.entity.JjkWordsEntity;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * 
 *
 * @author Mark
 * @email sunlightcs@gmail.com
 * @date 2019-09-20 09:36:33
 */
public interface JjkWordsService extends IService<JjkWordsEntity> {

    PageUtils queryPage(Map<String, Object> params);

    @Override
    boolean save(JjkWordsEntity entity);


    @Override
    boolean removeByIds(Collection<? extends Serializable> idList);

    void secondSend(String processId, String id);

    void firstSend(String processId, String id);
}

