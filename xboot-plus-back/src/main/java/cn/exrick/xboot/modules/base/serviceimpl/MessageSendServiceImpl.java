package cn.exrick.xboot.modules.base.serviceimpl;

import cn.exrick.xboot.modules.base.dao.MessageSendDao;
import cn.exrick.xboot.modules.base.entity.MessageSend;
import cn.exrick.xboot.modules.base.service.MessageSendService;
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
import java.util.List;

/**
 * 消息发送接口实现
 * @author Exrick
 */
@Slf4j
@Service
@Transactional
public class MessageSendServiceImpl implements MessageSendService {

    @Autowired
    private MessageSendDao messageSendDao;

    @Override
    public MessageSendDao getRepository() {
        return messageSendDao;
    }

    @Override
    public void deleteByMessageId(String messageId) {

        messageSendDao.deleteByMessageId(messageId);
    }

    @Override
    public Page<MessageSend> findByCondition(MessageSend messageSend, Pageable pageable) {

        return messageSendDao.findAll(new Specification<MessageSend>() {
            @Nullable
            @Override
            public Predicate toPredicate(Root<MessageSend> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {

                Path<String> messageIdField = root.get("messageId");
                Path<String> userIdField = root.get("userId");
                Path<Integer> statusField = root.get("status");

                List<Predicate> list = new ArrayList<Predicate>();

                if(StrUtil.isNotBlank(messageSend.getMessageId())){
                    list.add(cb.equal(messageIdField,messageSend.getMessageId()));
                }
                if(StrUtil.isNotBlank(messageSend.getUserId())){
                    list.add(cb.equal(userIdField,messageSend.getUserId()));
                }
                if(messageSend.getStatus()!=null){
                    list.add(cb.equal(statusField,messageSend.getStatus()));
                }

                Predicate[] arr = new Predicate[list.size()];
                cq.where(list.toArray(arr));
                return null;
            }
        }, pageable);
    }

}