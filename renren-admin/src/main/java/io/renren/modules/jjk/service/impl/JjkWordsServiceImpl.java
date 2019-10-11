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
import net.sf.json.JSONObject;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.payloads.StartProcessPayload;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.impl.HistoricTaskInstanceQueryImpl;
import org.activiti.engine.impl.persistence.entity.data.DataManager;
import org.activiti.engine.impl.persistence.entity.data.HistoricTaskInstanceDataManager;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.task.Task;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


//    @GetMapping("/processTaskPage")
//    public List<Map<String, Object>> processTaskPage(int startIndex, int endIndex) {
//        return testService.findRUTask(startIndex,endIndex);
//    }

//    /**
//     * 启动流程
@Service("jjkWordsService")
public class JjkWordsServiceImpl extends ServiceImpl<JjkWordsDao, JjkWordsEntity> implements JjkWordsService {

    private ProcessRuntimeImpl processRuntime;
    private TaskService taskService;
    private RuntimeService runtimeService;
    private JjkActFeignService jjkActFeignService;
    private SysUserRoleService sysUserRoleService;
    private ProcessEngine processEngine;
    private HistoryService historyService;
    @Autowired
    public JjkWordsServiceImpl(ProcessRuntimeImpl processRuntime, TaskService taskService, RuntimeService runtimeService, JjkActFeignService jjkActFeignService, SysUserRoleService sysUserRoleService, ProcessEngine processEngine, HistoryService historyService) {
        this.processRuntime = processRuntime;
        this.taskService = taskService;
        this.runtimeService = runtimeService;
        this.jjkActFeignService = jjkActFeignService;
        this.sysUserRoleService = sysUserRoleService;
        this.processEngine = processEngine;
        this.historyService = historyService;
    }



    public JjkWordsServiceImpl() {
    }

    /*
     *部署流程定义 （从classpath）
     */
    public void deploymentProcessDefinition_classpath(){
        Deployment deployment = processEngine.getRepositoryService()//与流程定义和部署对象相关的Service
                .createDeployment()//创建一个部署对象
                .name("aaaaaa")
                .addClasspathResource("processes/qingJ.bpmn20.xml")//从classpath的资源中加载，一次只能加载一个文件
                .deploy();//完成部署
        System.out.println("部署ID："+deployment.getId());
        System.out.println("部署名称:"+deployment.getName());
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String username = ShiroUtils.getUserEntity().getUsername();
        List<Long> longs = sysUserRoleService.queryRoleIdList(ShiroUtils.getUserEntity().getUserId());
        ArrayList<String> strings = new ArrayList<>();
        for (Long aLong : longs) {
            strings.add(aLong + "");
        }
        final String STATE_FINAL = "state";

        PageUtils pageUtils = null;
        List<Task> tasks = null;
        int[] buttonArray = null;
        if (!longs.isEmpty()&&!longs.contains(1l)) {
            tasks = taskService.createTaskQuery().taskAssigneeIds(strings).orderByTaskCreateTime().desc().listPage(Integer.valueOf(params.get("page") + "") - 1, Integer.valueOf(params.get("limit") + ""));
        }
        if (CollectionUtil.isNotEmpty(tasks)) {
            QueryWrapper<JjkWordsEntity> jjkWordsEntityQueryWrapper = new QueryWrapper<>();
            jjkWordsEntityQueryWrapper.in("id", tasks.stream().map(task -> {
                JSONObject jsonObject = JSONObject.fromObject(task.getDescription());
                ;
                return jsonObject.get("id");
            }).collect(Collectors.toList()));
            IPage<JjkWordsEntity> page = this.page(
                    new Query<JjkWordsEntity>().getPage(params),
                    jjkWordsEntityQueryWrapper
            );
            for (JjkWordsEntity record : page.getRecords()) {
                record.setButtonIds(JSONObject.fromObject(tasks.get(0).getDescription()).getString("buttons"));
            }

            pageUtils = new PageUtils(page);
            if (pageTool(STATE_FINAL, pageUtils,longs)) return pageUtils;
        } else {
            QueryWrapper<JjkWordsEntity> jjkWordsEntityQueryWrapper = new QueryWrapper<>();
            jjkWordsEntityQueryWrapper.eq("createBy", username);
            IPage<JjkWordsEntity> page = this.page(
                    new Query<JjkWordsEntity>().getPage(params),
                    jjkWordsEntityQueryWrapper
            );
            pageUtils = new PageUtils(page);

            if (pageTool(STATE_FINAL, pageUtils,longs)) return pageUtils;
        }
        return pageUtils;


    }

    private boolean pageTool(String STATE_FINAL, PageUtils pageUtils,List<Long> longs) {
        if (CollectionUtils.isNotEmpty(pageUtils.getList())) {
            List<JjkWordsEntity> list = (List<JjkWordsEntity>) pageUtils.getList();


            for (JjkWordsEntity jjkWordsEntity : list) {
                String id = jjkWordsEntity.getId();
                System.out.println("id = " + id);
                Task taskQuery = getTask(id);
                if(taskQuery==null){
                    jjkWordsEntity.setTypeStr("审批完成");
                    jjkWordsEntity.setAssign(longs.get(0)+"");
                    jjkWordsEntity.setButtonIds("{\"buttons\":{\"noun\":[\"新增\",\"修改\",\"确定\",\"取消\"],\"line\":[\"查看\"]}}");
                    jjkWordsEntity.setRoleList(longs);
                    continue;
                }
                //  name 和 委托角色
                String assignee = taskQuery.getAssignee();
                String name = taskQuery.getName();
                jjkWordsEntity.setNodeName("组:" + assignee + ";" + "任务：" + name );
                jjkWordsEntity.setButtonIds(taskQuery.getDescription());
                jjkWordsEntity.setAssign(taskQuery.getAssignee());
                jjkWordsEntity.setRoleList(longs);

                switch (jjkWordsEntity.getType()) {
                    case 0:
                        int state;
                        String typeStr = "未提交";
                        jjkWordsEntity.setTypeStr(typeStr);
                        break;
                    case 1:
                        //1是采购组长 审批，2 采购审批成功，3， 财务
                        state = JSONObject.fromObject(taskQuery.getDescription()).getInt(STATE_FINAL);
                        typeStr = "待审核";

                        switch (state) {
                            case 1:
                                typeStr = "护士长" + typeStr;
                                break;
                            case 2:
                                typeStr = "医生" + typeStr;
                                break;
                        }
                        jjkWordsEntity.setTypeStr(typeStr);
                        break;
                    case 2:
                        state = JSONObject.fromObject(taskQuery.getDescription()).getInt(STATE_FINAL);
                        typeStr = "完成";
                        switch (state) {
                            case 3:
                                typeStr = "护士长审批" + typeStr;
                                break;
                            case 4:
                                typeStr = "医生审批" + typeStr;
                                break;
                        }
                        jjkWordsEntity.setTypeStr(typeStr);
                        break;
                    case 3:
                        state = JSONObject.fromObject(taskQuery.getDescription()).getInt(STATE_FINAL);
                        typeStr = "未通过";
                        switch (state) {
                            case 5:
                                typeStr = "护士长审批" + typeStr;
                                break;
                            case 6:
                                typeStr = "医生审批" + typeStr;
                                break;
                        }
                        jjkWordsEntity.setTypeStr(typeStr);
                        break;
                }
            }
            return true;

        }
        return false;
    }

    private Task getTask(String id) {
        StringBuilder sb = new StringBuilder();
        sb.append("select * from ACT_RU_TASK  where DESCRIPTION_ like '%");
        sb.append(id);
        sb.append("%' ");
        return taskService.createNativeTaskQuery().sql(sb.toString()).singleResult();
    }


    @Override
    public boolean save(JjkWordsEntity entity) {
        SysUserEntity userEntity = ShiroUtils.getUserEntity();
        List<Long> longs = sysUserRoleService.queryRoleIdList(userEntity.getUserId());
        entity.setCreateby(userEntity.getUsername());
        //提交的状态
        entity.setType(0);
        boolean save = super.save(entity);
        StartProcessPayload build = ProcessPayloadBuilder
                .start().withProcessDefinitionKey("myProcess_1")
                .withVariable("userId", 1)
                .withVariable("typeOne", 0)
                .withVariable("resonTwo", entity.getId())
                .build();
        ProcessInstance start = processRuntime.start(build);
        return save;
    }

    public boolean removeByIds(Collection<? extends Serializable> idList) {
        List<JjkActFeignEntity> list = jjkActFeignService.list();
        List<String> stringList = new ArrayList<>();
        list.stream().filter(eo -> idList.contains(eo.getWordsId() + "")).forEach(action -> stringList.add(action.getActProcessId()));
        jjkActFeignService.removeByIds(stringList);
        for (String id : stringList) {
            runtimeService.deleteProcessInstance(id, "删除订单");
        }
        return SqlHelper.retBool(this.baseMapper.deleteBatchIds(idList));
    }

    @Override
    public void secondSend(String id) {
        JjkWordsEntity byId = getById(id);
        byId.setType(1);
        updateById(byId);
        Task task = getTask(id);
        List<Long> longs = sysUserRoleService.queryRoleIdList(ShiroUtils.getUserEntity().getUserId());
        //填写完之后，给下个审批人赋值
        Map<String, Object> val = new HashMap<>();
        val.put("resonTwo", id);
        val.put("typeTwo", 1);
        //执行任务
        taskService.complete(task.getId(), val);
        //可以设置成写死的角色id  可给该角色下的所有用户发送邮件 审批   由于 我写的内存验证，就省略了 可以写成查表的
        try {
            EmailUtil.sendMessage("修改后送审");
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * 首营送审
     *
     * @param id
     */
    @Override
    public void firstSend(String id) {
        JjkWordsEntity byId = getById(id);
        byId.setType(1);
        updateById(byId);
        Task task = getTask(id);
        List<Long> longs = sysUserRoleService.queryRoleIdList(ShiroUtils.getUserEntity().getUserId());

        //填写完之后，给下个审批人赋值
        Map<String, Object> val = new HashMap<>();
        val.put("resonTwo", id);
        val.put("typeTwo", 1);
        //执行任务
        taskService.complete(task.getId(), val);
        //可以设置成写死的角色id  可给该角色下的所有用户发送邮件 审批   由于 我写的内存验证，就省略了 可以写成查表的1
        try {
            EmailUtil.sendMessage("首迎送审开始");
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * 同意
     *
     * @param id
     */
    @Override
    public void accpect(String id, boolean fool) {
        JjkWordsEntity byId = getById(id);

        Task task = getTask(id);
        byId.setType(fool?2:3);
        Map<String,Object> mapVal = new HashMap();
        mapVal.put("resonTwo", id);
        //护士长
        if ("2".equals(task.getAssignee())) {
            mapVal.put(fool ?"typeThree":"typeOne",fool ? 3 : 5);
            taskService.setVariable(task.getId(),fool ?"typeThree":"typeOne",fool ? 3 : 5);
            mapVal.put("fool", fool?1:2);
        } else {
            //医生
            mapVal.put(fool ?"typeThree":"typeOne", fool ? 4 : 6);
            taskService.setVariable(task.getId(),fool ?"typeThree":"typeOne",fool ? 4 : 6);
            mapVal.put("bool", fool?1:2);
        }

        taskService.complete(task.getId(), mapVal);
        updateById(byId);
        //可以设置成写死的角色id  可给该角色下的所有用户发送邮件 审批   由于 我写的内存验证，就省略了 可以写成查表的1
        try {
            EmailUtil.sendMessage("首迎送审"+fool);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }


    /**
     * 查询审批记录
     */
    public List<JjkWordsEntity> approveNotes(String id){

        StringBuilder sb = new StringBuilder();
        sb.append("select * from ACT_RU_TASK  where DESCRIPTION_ like '%");
        sb.append(id);
        sb.append("%' ");
        List<HistoricTaskInstance> historicTask = historyService.createHistoricTaskInstanceQuery().taskDescriptionLike("%\"id\":"+id+"%").orderByTaskCreateTime().asc().list();

        List<JSONObject> des = historicTask.stream().map(task -> {
          JSONObject jsonObject =  JSONObject.fromObject(task.getDescription());
            jsonObject.put("assign",task.getAssignee());
            jsonObject.put("name",task.getName());
            return jsonObject;
        }).collect(Collectors.toList());

        List<String> ids = des.stream().map(eo -> eo.getString("id")).collect(Collectors.toList());
        QueryWrapper<JjkWordsEntity> jjkWordsEntityQueryWrapper = new QueryWrapper<>();
        jjkWordsEntityQueryWrapper.in("id",ids);
        List<JjkWordsEntity> list = this.list(jjkWordsEntityQueryWrapper);
        List<Long> longs = sysUserRoleService.queryRoleIdList(ShiroUtils.getUserEntity().getUserId());
        ArrayList<String> strings = new ArrayList<>();
        String STATE_FINAL = "state";
        List<JjkWordsEntity> result  = new ArrayList<>();
        for (JSONObject de : des) {
                JjkWordsEntity jjkWordsEntityOld = list.stream().filter(eo -> eo.getId().equals(de.getString("id"))).findFirst().get();
               JjkWordsEntity jjkWordsEntity = null;
            try {
                jjkWordsEntity= (JjkWordsEntity) jjkWordsEntityOld.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
                //  name 和 委托角色
                String assignee = de.getString("assign");
               jjkWordsEntity.setName(de.getString("name"));
                jjkWordsEntity.setNodeName("组:" + assignee + ";" + "任务：" +de.getString("name"));
                jjkWordsEntity.setButtonIds(de.toString());
                jjkWordsEntity.setAssign(de.getString("assign"));
                jjkWordsEntity.setRoleList(longs);
                switch (de.getInt("state")) {
                    case 0:
                        String typeStr = "未提交";
                        jjkWordsEntity.setTypeStr(typeStr);
                        break;
                    case 1:
                        //1是采购组长 审批，2 采购审批成功，3， 财务
                        typeStr = "护士长待审核";
                        jjkWordsEntity.setTypeStr(typeStr);
                        break;
                    case 2:
                        //1是采购组长 审批，2 采购审批成功，3， 财务
                        typeStr = "医生待审核";
                        jjkWordsEntity.setTypeStr(typeStr);
                        break;
                    case 3:
                        //1是采购组长 审批，2 采购审批成功，3， 财务
                        typeStr = "一次审批完成";
                        jjkWordsEntity.setTypeStr(typeStr);
                        break;
                    case 4:
                        //1是采购组长 审批，2 采购审批成功，3， 财务
                        typeStr = "审批完成";
                        jjkWordsEntity.setTypeStr(typeStr);
                        break;
                    case 5:
                        typeStr = "护士长审批不通过";
                        jjkWordsEntity.setTypeStr(typeStr);
                        break;
                    case 6:
                        typeStr = "医生审批不通过";
                        jjkWordsEntity.setTypeStr(typeStr);
                        break;
                }
            result.add(jjkWordsEntity);
        }


        System.out.println("result.toString() = " + result.toString());

        return result;
    }


/**
 public JjkWordsServiceImpl(ProcessRuntimeImpl processRuntime, TaskService taskService, RuntimeService runtimeService, JjkActFeignService jjkActFeignService, SysUserRoleService sysUserRoleService, ProcessEngine processEngine, HistoricTaskInstanceQueryImpl historicTaskInstanceQuery) {
 this.processRuntime = processRuntime;
 this.taskService = taskService;
 this.runtimeService = runtimeService;
 this.jjkActFeignService = jjkActFeignService;
 this.sysUserRoleService = sysUserRoleService;
 this.processEngine = processEngine;
 this.historicTaskInstanceQuery = historicTaskInstanceQuery;
 * 首先先展示流程 和 任务，每个 护士制药都属于一个流程，展示各个流程中的任务的状态 为了审批的时候用
 *
 * @param startIndex 开始位置
 * @param endIndex   结束位置
 */
}
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

