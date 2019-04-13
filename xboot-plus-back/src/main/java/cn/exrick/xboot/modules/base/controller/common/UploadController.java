package cn.exrick.xboot.modules.base.controller.common;

import cn.exrick.xboot.common.constant.CommonConstant;
import cn.exrick.xboot.common.constant.SettingConstant;
import cn.exrick.xboot.common.limit.RedisRaterLimiter;
import cn.exrick.xboot.common.utils.*;
import cn.exrick.xboot.common.vo.OssSetting;
import cn.exrick.xboot.common.vo.Result;
import cn.exrick.xboot.modules.base.entity.File;
import cn.exrick.xboot.config.exception.XbootException;
import cn.exrick.xboot.modules.base.service.FileService;
import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.FileInputStream;

/**
 * @author Exrickx
 */
@Slf4j
@RestController
@Api(description = "文件上传接口")
@RequestMapping("/xboot/upload")
@Transactional
public class UploadController {

    @Autowired
    private RedisRaterLimiter redisRaterLimiter;

    @Autowired
    private QiniuUtil qiniuUtil;

    @Autowired
    private AliOssUtil aliOssUtil;

    @Autowired
    private TencentOssUtil tencentOssUtil;

    @Autowired
    private FileUtil fileUtil;

    @Autowired
    private IpInfoUtil ipInfoUtil;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private FileService fileService;

    @RequestMapping(value = "/file",method = RequestMethod.POST)
    @ApiOperation(value = "文件上传")
    public Result<Object> upload(@RequestParam("file") MultipartFile file,
                                 HttpServletRequest request) {

        // IP限流 在线Demo所需 5分钟限1个请求
        String token = redisRaterLimiter.acquireTokenFromBucket("upload:"+ipInfoUtil.getIpAddr(request), 1, 300000);
        if (StrUtil.isBlank(token)) {
            throw new XbootException("上传那么多干嘛，等等再传吧");
        }

        String used = redisTemplate.opsForValue().get(SettingConstant.OSS_USED);
        if(StrUtil.isBlank(used)){
            return new ResultUtil<Object>().setErrorMsg(501, "您还未配置OSS服务");
        }

        String result = "";
        String fKey = CommonUtil.renamePic(file.getOriginalFilename());
        File f = new File();
        try {
            FileInputStream inputStream = (FileInputStream) file.getInputStream();
            // 上传至第三方云服务或服务器
            if(used.equals(SettingConstant.QINIU_OSS)){
                result = qiniuUtil.qiniuInputStreamUpload(inputStream, fKey);
                f.setLocation(CommonConstant.OSS_QINIU);
            }else if(used.equals(SettingConstant.ALI_OSS)){
                result = aliOssUtil.aliInputStreamUpload(inputStream, fKey);
                f.setLocation(CommonConstant.OSS_ALI);
            }else if(used.equals(SettingConstant.TENCENT_OSS)){
                result = tencentOssUtil.tencentInputStreamUpload(file, inputStream, fKey);
                f.setLocation(CommonConstant.OSS_TENCENT);
            }else if(used.equals(SettingConstant.LOCAL_OSS)){
                result = fileUtil.localUpload(file, fKey);
                f.setLocation(CommonConstant.OSS_LOCAL);
            }
            // 保存数据信息至数据库
            f.setName(file.getOriginalFilename());
            f.setSize(file.getSize());
            f.setType(file.getContentType());
            f.setFKey(fKey);
            f.setUrl(result);
            fileService.save(f);
        } catch (Exception e) {
            log.error(e.toString());
            return new ResultUtil<Object>().setErrorMsg(e.toString());
        }
        if(used.equals(SettingConstant.LOCAL_OSS)){
            OssSetting os = fileUtil.getOssSetting();
            result = os.getHttp() + os.getEndpoint() + "/" + f.getId();
        }
        return new ResultUtil<Object>().setData(result);
    }
}
