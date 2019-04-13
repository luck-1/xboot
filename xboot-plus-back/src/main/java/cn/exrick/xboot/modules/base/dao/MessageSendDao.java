package cn.exrick.xboot.modules.base.dao;

import cn.exrick.xboot.base.XbootBaseDao;
import cn.exrick.xboot.modules.base.entity.MessageSend;

/**
 * 消息发送数据处理层
 * @author Exrick
 */
public interface MessageSendDao extends XbootBaseDao<MessageSend,String> {

    /**
     * 通过消息id删除
     * @param messageId
     */
    void deleteByMessageId(String messageId);
}