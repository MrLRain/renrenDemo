package io.renren.modules.sys.controller;

import java.util.Arrays;
import java.util.Map;

import io.renren.common.validator.ValidatorUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.renren.modules.sys.entity.JjkActFeignEntity;
import io.renren.modules.sys.service.JjkActFeignService;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.R;



/**
 * 
 *
 * @author Mark
 * @email sunlightcs@gmail.com
 * @date 2019-09-20 15:06:12
 */
@RestController
@RequestMapping("sys/jjkactfeign")
public class JjkActFeignController {
    @Autowired
    private JjkActFeignService jjkActFeignService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    @RequiresPermissions("sys:jjkactfeign:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = jjkActFeignService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{actProcessId}")
    @RequiresPermissions("sys:jjkactfeign:info")
    public R info(@PathVariable("actProcessId") String actProcessId){
        JjkActFeignEntity jjkActFeign = jjkActFeignService.getById(actProcessId);

        return R.ok().put("jjkActFeign", jjkActFeign);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    @RequiresPermissions("sys:jjkactfeign:save")
    public R save(@RequestBody JjkActFeignEntity jjkActFeign){
        jjkActFeignService.save(jjkActFeign);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @RequiresPermissions("sys:jjkactfeign:update")
    public R update(@RequestBody JjkActFeignEntity jjkActFeign){
        ValidatorUtils.validateEntity(jjkActFeign);
        jjkActFeignService.updateById(jjkActFeign);
        
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    @RequiresPermissions("sys:jjkactfeign:delete")
    public R delete(@RequestBody String[] actProcessIds){
        jjkActFeignService.removeByIds(Arrays.asList(actProcessIds));

        return R.ok();
    }

}
