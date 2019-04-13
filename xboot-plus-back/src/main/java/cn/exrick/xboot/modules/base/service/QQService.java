package cn.exrick.xboot.modules.base.service;

import cn.exrick.xboot.base.XbootBaseService;
import cn.exrick.xboot.common.vo.SearchVo;
import cn.exrick.xboot.modules.base.entity.social.QQ;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * qq登录接口
 * @author Exrick
 */
public interface QQService extends XbootBaseService<QQ,String> {

    /**
     * 通过openId获取
     * @param openId
     * @return
     */
    QQ findByOpenId(String openId);

    /**
     * 通过username获取
     * @param username
     * @return
     */
    QQ findByRelateUsername(String username);

    /**
     * 分页多条件获取
     * @param username
     * @param relateUsername
     * @param searchVo
     * @param pageable
     * @return
     */
    Page<QQ> findByCondition(String username, String relateUsername, SearchVo searchVo, Pageable pageable);
}