package cn.exrick.xboot.modules.base.serviceimpl.activiti;

import cn.exrick.xboot.common.vo.SearchVo;
import cn.exrick.xboot.modules.base.dao.activiti.ActProcessDao;
import cn.exrick.xboot.modules.base.entity.activiti.ActProcess;
import cn.exrick.xboot.modules.base.service.activiti.ActProcessService;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 流程管理接口实现
 * @author Exrick
 */
@Slf4j
@Service
@Transactional
public class ActProcessServiceImpl implements ActProcessService {

    @Autowired
    private ActProcessDao actProcessDao;

    @Override
    public ActProcessDao getRepository() {
        return actProcessDao;
    }

    @Override
    public Page<ActProcess> findByCondition(ActProcess actProcess, SearchVo searchVo, Pageable pageable) {

        return actProcessDao.findAll(new Specification<ActProcess>() {
            @Nullable
            @Override
            public Predicate toPredicate(Root<ActProcess> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {

                Path<String> nameField = root.get("name");
                Path<String> processKeyField = root.get("processKey");
                Path<Integer> statusField = root.get("status");
                Path<Date> createTimeField = root.get("createTime");

                List<Predicate> list = new ArrayList<Predicate>();

                // 模糊搜素
                if(StrUtil.isNotBlank(actProcess.getName())){
                    list.add(cb.like(nameField,'%'+actProcess.getName()+'%'));
                }
                if(StrUtil.isNotBlank(actProcess.getProcessKey())){
                    list.add(cb.like(processKeyField,'%'+actProcess.getProcessKey()+'%'));
                }

                //状态
                if(actProcess.getStatus()!=null){
                    list.add(cb.equal(statusField, actProcess.getStatus()));
                }
                //创建时间
                if(StrUtil.isNotBlank(searchVo.getStartDate())&&StrUtil.isNotBlank(searchVo.getEndDate())){
                    Date start = DateUtil.parse(searchVo.getStartDate());
                    Date end = DateUtil.parse(searchVo.getEndDate());
                    list.add(cb.between(createTimeField, start, DateUtil.endOfDay(end)));
                }

                Predicate[] arr = new Predicate[list.size()];
                cq.where(list.toArray(arr));
                return null;
            }
        }, pageable);
    }
}