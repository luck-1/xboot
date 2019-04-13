package cn.exrick.xboot.common.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Exrickx
 */
@Data
public class OtherSetting implements Serializable{

    @ApiModelProperty(value = "域名")
    private String domain;


    @ApiModelProperty(value = "IP黑名单")
    private String blacklist;
}
