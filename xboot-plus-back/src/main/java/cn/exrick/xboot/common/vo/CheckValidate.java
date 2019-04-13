package cn.exrick.xboot.common.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Exrickx
 */
@Data
public class CheckValidate implements Serializable {

    private String userId;

    private String email;

    private String validateUrl;
}
