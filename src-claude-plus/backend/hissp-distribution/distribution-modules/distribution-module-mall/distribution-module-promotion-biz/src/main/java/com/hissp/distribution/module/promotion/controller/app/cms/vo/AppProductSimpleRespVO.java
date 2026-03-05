package com.hissp.distribution.module.promotion.controller.app.cms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端 - 商品简单信息 Response VO (用于CMS文章关联商品展示)
 *
 * @author 芋道源码
 */
@Schema(description = "用户端 - 商品简单信息 Response VO")
@Data
public class AppProductSimpleRespVO {

    @Schema(description = "商品SPU编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "iPhone 15")
    private String name;

    @Schema(description = "商品封面图", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://example.com/product.jpg")
    private String picUrl;

    @Schema(description = "商品价格", requiredMode = Schema.RequiredMode.REQUIRED, example = "5999")
    private Integer price;

    @Schema(description = "市场价", example = "6999")
    private Integer marketPrice;

    @Schema(description = "销量", example = "100")
    private Integer salesCount;

    @Schema(description = "商品状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "是否是种草商品", example = "true")
    private Boolean isRecommend;

}