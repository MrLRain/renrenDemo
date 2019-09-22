package io.renren.modules.jjk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import io.renren.common.utils.EmailUtil;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.ProcessRuntimeImpl;
import io.renren.common.utils.Query;
import io.renren.modules.jjk.dao.JjkWordsDao;
import io.renren.modules.jjk.entity.JjkWordsEntity;
import io.renren.modules.jjk.service.JjkWordsService;
import io.renren.modules.sys.entity.JjkActFeignEntity;
import io.renren.modules.sys.entity.SysUserEntity;
import io.renren.modules.sys.service.JjkActFeignService;
import io.renren.modules.sys.service.SysUserRoleService;
import io.renren.modules.sys.shiro.ShiroUtils;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.payloads.StartProcessPayload;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.util.*;


@Service("jjkWordsService")
public class JjkWordsServiceImpl extends ServiceImpl<JjkWordsDao, JjkWordsEntity> implements JjkWordsService {

    private ProcessRuntimeImpl processRuntime;
    private TaskService taskService;
    private RuntimeService runtimeService;
    private JjkActFeignService jjkActFeignService;
    private SysUserRoleService sysUserRoleService;

    public JjkWordsServiceImpl(ProcessRuntimeImpl processRuntime, TaskService taskService, RuntimeService runtimeService, JjkActFeignService jjkActFeignService, SysUserRoleService sysUserRoleService) {
        this.processRuntime = processRuntime;
        this.taskService = taskService;
        this.runtimeService = runtimeService;
        this.jjkActFeignService = jjkActFeignService;
        this.sysUserRoleService = sysUserRoleService;
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        params.put("createBy", ShiroUtils.getUserEntity().getUsername());
        IPage<JjkWordsEntity> page = this.page(
                new Query<JjkWordsEntity>().getPage(params),
                new QueryWrapper<JjkWordsEntity>()
        );
        PageUtils pageUtils = new PageUtils(page);
        if(CollectionUtils.isNotEmpty(pageUtils.getList())){
            List<JjkWordsEntity> list = (List<JjkWordsEntity>) pageUtils.getList();
            List<JjkActFeignEntity> arrayList = jjkActFeignService.list();
            for (JjkWordsEntity jjkWordsEntity : list) {
                String id = jjkWordsEntity.getId();
                JjkActFeignEntity jjkActFeignEntity = arrayList.stream().filter(eo -> id.equals(eo.getWordsId()+"")).findFirst().get();
                Task taskQuery = taskService.createTaskQuery().processInstanceId(jjkActFeignEntity.getActProcessId()).singleResult();
                //  name 和 委托角色
                String assignee = taskQuery.getAssignee();
                String name = taskQuery.getName();
                jjkWordsEntity.setProcessId(jjkActFeignEntity.getActProcessId());
                jjkWordsEntity.setNodeName("组:"+assignee+";"+"任务："+name);
                switch (jjkWordsEntity.getType()){
                    case 0:
                        jjkWordsEntity.setTypeStr("未提交");
                        break;
                    case 1:
                        jjkWordsEntity.setTypeStr("待审核");
                        break;
                    case 2:
                        jjkWordsEntity.setTypeStr("一级审核成功");
                        break;
                    case 3:
                        jjkWordsEntity.setTypeStr("审核完成");
                        break;
                }
            }

        }

        return pageUtils;
    }

    @Override
    public boolean save(JjkWordsEntity entity) {
        SysUserEntity userEntity = ShiroUtils.getUserEntity();
        List<Long> longs = sysUserRoleService.queryRoleIdList(userEntity.getUserId());
        entity.setCreateby(userEntity.getUsername());

        StartProcessPayload build = ProcessPayloadBuilder
                .start().withProcessDefinitionKey("myProcess_1")
                .withVariable("userId", longs.get(0))
                .build();
        //提交的状态
        entity.setType(0);
        ProcessInstance start = processRuntime.start(build);
        boolean save = super.save(entity);
        JjkActFeignEntity entityTwo = new JjkActFeignEntity();
        entityTwo.setWordsId(Integer.valueOf(entity.getId()));
        entityTwo.setActProcessId(start.getId());
        jjkActFeignService.save(entityTwo);
        return save;
    }

    public boolean removeByIds(Collection<? extends Serializable> idList) {
        List<JjkActFeignEntity> list = jjkActFeignService.list();
        List<String> stringList = new ArrayList<>();
        list.stream().filter(eo -> idList.contains(eo.getWordsId()+"")).forEach(action->stringList.add(action.getActProcessId()));
        jjkActFeignService.removeByIds(stringList);
        for (String id : stringList) {
            runtimeService.deleteProcessInstance(id,"删除订单");
        }
        return SqlHelper.retBool(this.baseMapper.deleteBatchIds(idList));
    }

    @Override
    public void secondSend(String processId, String id) {
        JjkWordsEntity byId = getById(id);
        byId.setType(1);
        updateById(byId);
        Task task = taskService.createTaskQuery()//创建查询对象
                .processInstanceId(processId)//通过流程实例id来查询当前任务
                .singleResult();//获取单个查询结果

        //填写完之后，给下个审批人赋值
        Map<String, Object> val = new HashMap<>();
        val.put("reson", "zzzzzzzzzzzzzzzz");
        val.put("auditorId", ShiroUtils.getUserEntity().getUsername());
        //执行任务
        taskService.complete(task.getId(), val);
        //可以设置成写死的角色id  可给该角色下的所有用户发送邮件 审批   由于 我写的内存验证，就省略了 可以写成查表的
        try {
            EmailUtil.sendMessage("修改后送审");
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void firstSend(String processId, String id) {
        JjkWordsEntity byId = getById(id);
        byId.setType(1);
        updateById(byId);
        Task task = taskService.createTaskQuery()//创建查询对象
                .processInstanceId(processId)//通过流程实例id来查询当前任务
                .singleResult();//获取单个查询结果

        //填写完之后，给下个审批人赋值
        Map<String, Object> val = new HashMap<>();
        val.put("reson", "zzzzzzzzzzzzzzzz");
        val.put("auditorId", ShiroUtils.getUserEntity().getUsername());
        //执行任务
        taskService.complete(task.getId(), val);
        //可以设置成写死的角色id  可给该角色下的所有用户发送邮件 审批   由于 我写的内存验证，就省略了 可以写成查表的
        try {
            EmailUtil.sendMessage("首迎送审开始");
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }


/**
     * 首先先展示流程 和 任务，每个 护士制药都属于一个流程，展示各个流程中的任务的状态 为了审批的时候用
     *
     * @param startIndex 开始位置
     * @param endIndex   结束位置
     */
//    @GetMapping("/processTaskPage")
//    public List<Map<String, Object>> processTaskPage(int startIndex, int endIndex) {
//        return testService.findRUTask(startIndex,endIndex);
//    }

//    /**
//     * 启动流程
//     */
//    @GetMapping("/startUpProcess")
//    public void startUpProcess() throws GeneralSecurityException {
//        processRuntime.start(ProcessPayloadBuilder
//                .start().withProcessDefinitionKey("myProcess_1")
//                .withVariable("userId", "root")
//                .build());
//        EmailUtil.sendMessage("护士可以开始制药了");
//    }
//
//    /**
//     * 删除流程
//     */
//    @GetMapping("/removeProcess")
//    public void removeProcess(String processId){
//        //删除流程
//        runtimeService.deleteProcessInstance(processId,"删除订单");
//    }
//
//    /**
//     * 护士制药
//     * @param taskId
//     * @param name
//     * @throws GeneralSecurityException
//     */
//    @GetMapping("/sendForm")
//    public void sendForm(String taskId,String name) throws GeneralSecurityException {
//
//        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
//                .getAuthentication()
//                .getPrincipal();
//        System.out.println("登录用户名 = " + userDetails.getUsername());
//        ProcessInstance start = processRuntime.start(ProcessPayloadBuilder
//                .start().withProcessDefinitionKey("myProcess_1")
//                .withVariable("userId", "root")
//                .build());
//        String id = start.getId();
//
//        Task task = taskService.createTaskQuery()//创建查询对象
//                .processDefinitionId(id)//通过流程实例id来查询当前任务
//                .singleResult();//获取单个查询结果
//
//        //填写完之后，给下个审批人赋值
//        Map<String, Object> val = new HashMap<>();
//        val.put("reson", "制" + name + "药");
//        val.put("auditorId", userDetails.getUsername());
//        //执行任务
//        taskService.complete(task.getId(), val);
//        EmailUtil.sendMessage("护士制药了，请护士长审批药物");
//        //可以设置成写死的角色id  可给该角色下的所有用户发送邮件 审批   由于 我写的内存验证，就省略了 可以写成查表的
//        //String assignee = task.getAssignee();
//        //更改任务的状态
//    }
//
//
//    /**
//     * 护士长校验
//     */
//    @GetMapping("/validOne")
//    public void validHSZ(String taskId,boolean fool) throws GeneralSecurityException {
//        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
//                .getAuthentication()
//                .getPrincipal();
//        Task task = taskService.createTaskQuery()//创建查询对象
//                .taskId(taskId)//通过流程实例id来查询当前任务
//                .singleResult();//获取单个查询结果
//        Map<String, Object> val = new HashMap<>();
//        val.put("fool", fool);
//        val.put("auditorId", userDetails.getUsername());
//        if(fool){
//            EmailUtil.sendMessage("护士长审批同意，请医生审批药物");
//            val.put("reson","护士长"+userDetails.getUsername()+"审批成功");
//        }else{
//            EmailUtil.sendMessage("护士长审批不同意，请护士检查一下所配置药物");
//            val.put("reson","护士长"+userDetails.getUsername()+"审批失败");
//        }
//        taskService.complete(task.getId(), val);
//    }
//
//    /**
//     * 医生审批
//     */
//    @GetMapping("/validTwo")
//    public void validHSZTwo(String taskId,boolean fool) throws GeneralSecurityException {
//        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
//                .getAuthentication()
//                .getPrincipal();
//        Task task = taskService.createTaskQuery()//创建查询对象
//                .taskId(taskId)//通过流程实例id来查询当前任务
//                .singleResult();//获取单个查询结果
//        Map<String, Object> val = new HashMap<>();
//        val.put("bool", fool);
//        val.put("auditorId", userDetails.getUsername());
//        if(fool){
//            EmailUtil.sendMessage("医生审批同意，请科长审批药物");
//            val.put("reson","医生"+userDetails.getUsername()+"审批成功");
//        }else{
//            EmailUtil.sendMessage("医生审批不同意，请护士检查一下所配置药物");
//            val.put("reson","医生"+userDetails.getUsername()+"审批失败");
//        }
//        taskService.complete(task.getId(), val);
//    }
//
//
//    /**
//     * 科长审批
//     */
//    @GetMapping("/valid")
//    public void valid(String taskId,boolean fool) throws GeneralSecurityException {
//        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
//                .getAuthentication()
//                .getPrincipal();
//        Task task = taskService.createTaskQuery()//创建查询对象
//                .taskId(taskId)//通过流程实例id来查询当前任务
//                .singleResult();//获取单个查询结果
//        Map<String, Object> val = new HashMap<>();
//        val.put("zool", fool);
//        val.put("auditorId", userDetails.getUsername());
//        if(fool){
//            EmailUtil.sendMessage("药物审批同意，请护士给患者实施");
//            val.put("reson","科长"+userDetails.getUsername()+"审批成功");
//        }else{
//            EmailUtil.sendMessage("科长审批不同意，请护士检查一下所配置药物");
//            val.put("reson","科长"+userDetails.getUsername()+"审批失败");
//        }
//        taskService.complete(task.getId(), val);
//    }
//


}
