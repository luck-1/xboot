package cn.exrick.xboot.modules.base.service;

import cn.exrick.xboot.base.XbootBaseService;
import cn.exrick.xboot.common.vo.SearchVo;
import cn.exrick.xboot.modules.base.entity.social.Weibo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 微博登录接口
 * @author Exrick
 */
public interface WeiboService extends XbootBaseService<Weibo,String> {

    /**
     * 通过openId获取
     * @param openId
     * @return
     */
    Weibo findByOpenId(String openId);

    /**
     * 通过username获取
     * @param username
     * @return
     */
    Weibo findByRelateUsername(String username);

    /**
     * 分页多条件获取
     * @param username
     * @param relateUsername
     * @param searchVo
     * @param pageable
     * @return
     */
    Page<Weibo> findByCondition(String username, String relateUsername, SearchVo searchVo, Pageable pageable);
}