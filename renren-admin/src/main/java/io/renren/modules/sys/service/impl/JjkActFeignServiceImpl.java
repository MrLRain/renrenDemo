package io.renren.modules.sys.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;

import io.renren.modules.sys.dao.JjkActFeignDao;
import io.renren.modules.sys.entity.JjkActFeignEntity;
import io.renren.modules.sys.service.JjkActFeignService;


@Service("jjkActFeignService")
public class JjkActFeignServiceImpl extends ServiceImpl<JjkActFeignDao, JjkActFeignEntity> implements JjkActFeignService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<JjkActFeignEntity> page = this.page(
                new Query<JjkActFeignEntity>().getPage(params),
                new QueryWrapper<JjkActFeignEntity>()
        );

        return new PageUtils(page);
    }

}
