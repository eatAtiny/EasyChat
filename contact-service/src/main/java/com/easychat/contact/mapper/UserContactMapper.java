package com.easychat.contact.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easychat.contact.entity.po.UserContact;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserContactMapper extends BaseMapper<UserContact> {

    /**
     * 获取联系人列表
     * @param contactType 联系人类型 0:好友 1:群组
     * @return 联系人列表
     * @author  easychat
     * @date 2023/8/2 11:20
     */
    List<UserContact> getContactList(String userId, Integer contactType);
}
