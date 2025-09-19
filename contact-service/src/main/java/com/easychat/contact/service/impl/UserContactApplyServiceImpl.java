package com.easychat.contact.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easychat.contact.entity.po.UserContactApply;
import com.easychat.contact.mapper.UserContactApplyMapper;
import com.easychat.contact.service.UserContactApplyService;
import org.springframework.stereotype.Service;

@Service
public class UserContactApplyServiceImpl extends ServiceImpl<UserContactApplyMapper, UserContactApply> implements UserContactApplyService {
}
