package com.easychat.contact.entity.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PageResultVO<T> {
    private Integer totalCount;
    private Integer pageSize;
    private Integer pageNo;
    private Integer pageTotal;
    private List<T> list = new ArrayList<T>();
}
