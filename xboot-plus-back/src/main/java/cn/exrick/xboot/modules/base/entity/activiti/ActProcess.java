package cn.exrick.xboot.modules.base.entity.activiti;

import cn.exrick.xboot.base.XbootBaseEntity;
import cn.exrick.xboot.common.constant.ActivitiConstant;
import com.baomidou.mybatisplus.annotations.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author Exrick
 */
@Data
@Entity
@Table(name = "t_act_process")
@TableName("t_act_process")
public class ActProcess extends XbootBaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "流程名称")
    private String name;

    @ApiModelProperty(value = "流程标识名称")
    private String processKey;

    @ApiModelProperty(value = "版本")
    private Integer version;

    @ApiModelProperty(value = "部署id")
    private String deploymentId;

    @ApiModelProperty(value = "所属分类")
    private String categoryId;

    @ApiModelProperty(value = "xml文件名")
    private String xmlName;

    @ApiModelProperty(value = "流程图片名")
    private String diagramName;

    @ApiModelProperty(value = "描述/备注")
    private String description;

    @ApiModelProperty(value = "流程状态 部署后默认1激活")
    private Integer status = ActivitiConstant.PROCESS_STATUS_ACTIVE;
}