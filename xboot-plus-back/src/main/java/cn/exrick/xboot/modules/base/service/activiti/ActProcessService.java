package cn.exrick.xboot.modules.base.service.activiti;

import cn.exrick.xboot.base.XbootBaseService;
import cn.exrick.xboot.common.vo.SearchVo;
import cn.exrick.xboot.modules.base.entity.activiti.ActProcess;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 流程管理接口
 * @author Exrick
 */
public interface ActProcessService extends XbootBaseService<ActProcess,String> {

    /**
     * 多条件分页获取
     * @param actProcess
     * @param searchVo
     * @param pageable
     * @return
     */
    Page<ActProcess> findByCondition(ActProcess actProcess, SearchVo searchVo, Pageable pageable);
}