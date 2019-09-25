package io.renren.modules.jjk.controller;

import io.renren.common.utils.PageUtils;
import io.renren.common.utils.R;
import io.renren.common.validator.ValidatorUtils;
import io.renren.modules.jjk.entity.JjkWordsEntity;
import io.renren.modules.jjk.service.JjkWordsService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;



/**
 * 
 *
 * @author Mark
 * @email sunlightcs@gmail.com
 * @date 2019-09-20 09:36:33
 */
@RestController
@RequestMapping("sys/jjkwords")
public class JjkWordsController {
    @Autowired
    private JjkWordsService jjkWordsService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    @RequiresPermissions("sys:jjkwords:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = jjkWordsService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    @RequiresPermissions("sys:jjkwords:info")
    public R info(@PathVariable("id") String id){
        JjkWordsEntity jjkWords = jjkWordsService.getById(id);
        return R.ok().put("jjkWords", jjkWords);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    @RequiresPermissions("sys:jjkwords:save")
    public R save(@RequestBody JjkWordsEntity jjkWords){
        jjkWordsService.save(jjkWords);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @RequiresPermissions("sys:jjkwords:update")
    public R update(@RequestBody JjkWordsEntity jjkWords){
        ValidatorUtils.validateEntity(jjkWords);
        jjkWordsService.updateById(jjkWords);
        
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    @RequiresPermissions("sys:jjkwords:delete")
    public R delete(@RequestBody String[] ids){
        jjkWordsService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }


    /**
     * 删除
     */
    @RequestMapping("/firstSend")
    public R firstSend(String processId,String id){
        jjkWordsService.firstSend(processId,id);
        return R.ok();
    }


    /**
     * 删除
     */
    @RequestMapping("/secondSend")
    public R secondSend(String processId,String id){
        jjkWordsService.secondSend(processId,id);
        return R.ok();
    }




}
