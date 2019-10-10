package io.renren.modules.jjk.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 
 * 
 * @author Mark
 * @email sunlightcs@gmail.com
 * @date 2019-09-20 09:36:33
 */
@Data
@TableName("jjk_words")
public class JjkWordsEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	@TableId
	private String id;
	/**
	 * 
	 */
	private String name;
	/**
	 * 
	 */
	private Integer age;
	/**
	 * 
	 */
	private String createby;
	/**
	 * 0是保存，1是提交，2是审批成功，3是审批失败
	 */
	private Integer type;
	//@TableField(exist = false)：表示该属性不为数据库表字段，但又是必须使用的。
	@TableField(exist = false)
	private String nodeName;

	@TableField(exist = false)
	private String typeStr;

	@TableField(exist = false)
	private String processId;

	@TableField(exist = false)
	private String buttonIds;
}
