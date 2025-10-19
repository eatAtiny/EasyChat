package com.easychat.contact.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.easychat.common.advice.BaseController;
import com.easychat.common.entity.vo.ResponseVO;
import com.easychat.common.utils.UserContext;
import com.easychat.common.entity.dto.ContactDTO;
import com.easychat.common.entity.po.Contact;
import com.easychat.contact.service.ContactService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contact")
public class ContactController extends BaseController {

    @Autowired
    private ContactService contactService;

    /**
     * 根路径默认处理方法
     * 处理GET请求到/contact
     */
    @GetMapping
    public ResponseVO defaultContactEndpoint() {
        return getSuccessResponseVO("Contact API is working");
    }

    /**
     * 获取联系人列表
     */
    @ApiOperation("根据联系人类型获取联系人列表")
    @GetMapping("/list/{contactType}")
    public ResponseVO loadContact(@PathVariable("contactType") String contactType) {
        List<Contact> contacts = contactService.getContactList(contactType);
        return getSuccessResponseVO(contacts);
    }

    /**
     * 获取联系人信息
     * @param contactId 对方id
     * @return 关系信息DTO
     */
    @GetMapping("/info/{contactId}")
    public ContactDTO getContactInfo(@PathVariable("contactId") String contactId) {
        Contact contact = contactService.getBaseMapper().selectOne(
                new LambdaQueryWrapper<Contact>()
                        .eq(Contact::getUserId, UserContext.getUser())
                        .eq(Contact::getContactId, contactId)
        );
        if (contact == null) {
            return null;
        }
        ContactDTO contactDTO = new ContactDTO();
        BeanUtils.copyProperties(contact, contactDTO);
        return contactDTO;
    }

    /**
     * 删除联系人
     */
    @ApiOperation("删除联系人")
    @DeleteMapping("")
    public ResponseVO deleteContact(@RequestParam("contactId") String contactId) {
        contactService.deleteContact(contactId);
        return getSuccessResponseVO(null);
    }

    /**
     * 拉黑联系人
     */
     @ApiOperation("拉黑联系人")
    @PostMapping("/blacklist")
    public ResponseVO blacklistContact(@RequestParam("contactId") String contactId) {
        contactService.blacklistContact(contactId);
        return getSuccessResponseVO(null);
    }

    /**
     * 退出群聊
     */
    @ApiOperation("退出群聊")
    @DeleteMapping("/group")
    public ResponseVO exitGroup(@RequestParam("groupId") String groupId) {
        contactService.exitGroup(groupId);
        return getSuccessResponseVO(null);
    }


    /**
     * 创建联系人关系(api)
     */
    @ApiOperation("创建联系人关系")
    @PostMapping("")
    public ResponseVO createContact(@RequestBody ContactDTO contactDTO) {
        contactService.manageContact(contactDTO);
        return getSuccessResponseVO(null);
    }

}