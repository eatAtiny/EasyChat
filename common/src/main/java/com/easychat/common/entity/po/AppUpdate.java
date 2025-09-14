package com.easychat.common.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.easychat.common.entity.enums.DateTimePatternEnum;
import com.easychat.common.utils.DateUtil;
import com.easychat.common.utils.StringTools;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * app发布
 */
@Data
public class AppUpdate implements Serializable {


    /**
     * 自增ID
     */
    private Integer id;

    /**
     * 版本号
     */
    private String version;

    /**
     * 更新描述
     */
    private String updateDesc;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 0:未发布 1:灰度发布 2:全网发布
     */
    private Integer status;

    /**
     * 灰度uid
     */
    private String grayscaleUid;

    /**
     * 文件类型0:本地文件 1:外链
     */
    private Integer fileType;

    /**
     * 外链地址
     */
    private String outerLink;

    @TableField(exist = false)
    private String[] updateDescArray;

    public String[] getUpdateDescArray() {
        if (!StringTools.isEmpty(updateDesc)) {
            return updateDesc.split("\\|");
        }
        return updateDescArray;
    }

    @Override
    public String toString() {
        return "自增ID:" + (id == null ? "空" : id) + "，版本号:" + (version == null ? "空" : version) + "，更新描述:" + (updateDesc == null ? "空" : updateDesc) + "，创建时间:" + (createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + "，0:未发布 1:灰度发布 2:全网发布:" + (status == null ? "空" : status) + "，灰度uid:" + (grayscaleUid == null ? "空" : grayscaleUid) + "，文件类型0:本地文件 1:外链:" + (fileType == null ? "空" : fileType) + "，外链地址:" + (outerLink == null ? "空" : outerLink);
    }
}
