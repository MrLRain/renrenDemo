package io.renren.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * 
 * @author Mark
 * @email sunlightcs@gmail.com
 * @date 2019-09-20 15:06:12
 */
@Data
@TableName("jjk_act_feign")
public class JjkActFeignEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	private String actProcessId;
	/**
	 * 
	 */
	private Integer wordsId;

}
