package com.hissp.distribution.module.pay.api.transfer.constant;

/**
 * 首信易渠道在 {@code channelExtras} 中约定的字段名。
 *
 * <p>业务侧只需按照以下键填充附加信息，底层 PayClient 将自动解析。</p>
 */
public interface PayeaseTransferConstants {

    String SUB_MERCHANT_ID = "subMerchantId";
    String BANK_ACCOUNT_NO = "bankAccountNo";
    String BANK_ACCOUNT_NAME = "bankAccountName";
    String BANK_NAME = "bankName";
    String BANK_BRANCH_NAME = "bankBranchName";
    String BANK_CODE = "bankCode";
    String PROVINCE_CODE = "provinceCode";
    String CITY_CODE = "cityCode";
    String AREA_CODE = "areaCode";
    String ADDRESS = "address";
    String RECEIVER_ADDRESS = "receiverAddress";
    String MOBILE = "mobile";
    String EMAIL = "email";
}
