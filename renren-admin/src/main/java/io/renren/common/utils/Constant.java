/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.common.utils;

/**
 * 常量
 *
 * @author Mark sunlightcs@gmail.com
 */
public class Constant {
	/** 超级管理员ID */
	public static final int SUPER_ADMIN = 1;
    /** 数据权限过滤 */
	public static final String SQL_FILTER = "sql_filter";
    /**
     * 当前页码
     */
    public static final String PAGE = "page";
    /**
     * 每页显示记录数
     */
    public static final String LIMIT = "limit";
    /**
     * 排序字段
     */
    public static final String ORDER_FIELD = "sidx";
    /**
     * 排序方式
     */
    public static final String ORDER = "order";
    /**
     *  升序
     */
    public static final String ASC = "asc";

	/**
	 * 菜单类型
	 */
    public enum MenuType {
        /**
         * 目录
         */
    	CATALOG(0),
        /**
         * 菜单
         */
        MENU(1),
        /**
         * 按钮
         */
        BUTTON(2);

        private int value;

        MenuType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
    
    /**
     * 定时任务状态
     */
    public enum ScheduleStatus {
        /**
         * 正常
         */
    	NORMAL(0),
        /**
         * 暂停
         */
    	PAUSE(1);

        private int value;

        ScheduleStatus(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    }

    /**
     * 云服务商
     */
    public enum CloudService {
        /**
         * 七牛云
         */
        QINIU(1),
        /**
         * 阿里云
         */
        ALIYUN(2),
        /**
         * 腾讯云
         */
        QCLOUD(3);

        private int value;

        CloudService(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }




    /**
     * 按钮
     */
    public enum Buttons {
        /**
         * 查看
         */
        LOOK_BTN(1),
        /**
         * c添加
         */
        ADD_BTN(2),
        /**
         * 修改
         */
        UPDATE_BTN(3),
        /**
         * approval
         */
        APPROVAL_BTN(4),
        /**
         * 采购经理审批
         */
        BUY_APPROVAL_BTN(5),
        /**
         * approval
         */
       UPDATE_APPROVAL_BTN(6);
        private int value;

        Buttons(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }


    /**
     * 按钮
     */
    public enum RoleButtonEnum {
        /**
         * 采购员
         */

        BUYER(1l,new Buttons[]{Buttons.LOOK_BTN,Buttons.ADD_BTN,Buttons.UPDATE_BTN,Buttons.APPROVAL_BTN,Buttons.UPDATE_APPROVAL_BTN}),
        /**
         * 采购组长
         */

        BUYER_HEADMAN(2l,new Buttons[]{Buttons.APPROVAL_BTN,Buttons.UPDATE_APPROVAL_BTN}),
        /**
         * 财务
         */
        FINANCIAL_BTN(3l,new Buttons[]{Buttons.APPROVAL_BTN,Buttons.UPDATE_APPROVAL_BTN});

        private long roleId;
        private Buttons[] buttons;

        RoleButtonEnum(long roleId, Buttons[] buttons) {
            this.roleId = roleId;
            this.buttons = buttons;
        }

        public long getRoleId() {
            return roleId;
        }

        public void setRoleId(long roleId) {
            this.roleId = roleId;
        }

        public Buttons[] getButtons() {
            return buttons;
        }

        public void setButtons(Buttons[] buttons) {
            this.buttons = buttons;
        }
        public static RoleButtonEnum swtichRoleId(String  roleId){
            switch (roleId){
                case "1":
                    return BUYER;
                case "2":
                    return BUYER_HEADMAN;
                case "3":
                    return FINANCIAL_BTN;
            }
            return null;
        }
    }


}
