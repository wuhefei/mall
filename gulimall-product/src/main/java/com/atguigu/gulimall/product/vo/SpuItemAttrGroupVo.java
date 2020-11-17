package com.atguigu.gulimall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @Description spu规格参数
 **/
@Data
@ToString
public class SpuItemAttrGroupVo {

    private String groupName;
    List<Attr> attrs;
}
