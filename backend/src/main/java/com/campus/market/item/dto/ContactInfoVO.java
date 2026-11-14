package com.campus.market.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 联系方式（仅在用户主动点击「复制」时下发完整值） */
@Data
@AllArgsConstructor
public class ContactInfoVO {

    private String contactInfo;
}
