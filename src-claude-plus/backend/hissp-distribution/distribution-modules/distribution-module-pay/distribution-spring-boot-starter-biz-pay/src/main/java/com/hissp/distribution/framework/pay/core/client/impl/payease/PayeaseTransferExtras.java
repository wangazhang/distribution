package com.hissp.distribution.framework.pay.core.client.impl.payease;

import cn.hutool.core.util.StrUtil;
import com.hissp.distribution.framework.common.exception.ServiceException;
import com.hissp.distribution.framework.pay.core.client.dto.transfer.PayTransferUnifiedInnerReqDTO;
import lombok.Getter;

import java.util.Map;

import static com.hissp.distribution.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;

/**
 * 负责解析并校验首信易渠道在 {@link PayTransferUnifiedInnerReqDTO} 中透传的附加字段。
 *
 * <p>首信易账户打款需要同时掌握子商户号、银行卡信息、联系信息等数据。
 * 这些数据由业务侧通过 {@code channelExtras} 传入，这里统一抽取成对象，
 * 便于客户端在不同阶段复用并保证字段完备。</p>
 */
@Getter
class PayeaseTransferExtras {

    private static final String EXTRA_SUB_MERCHANT_ID = "subMerchantId";
    private static final String EXTRA_BANK_ACCOUNT_NO = "bankAccountNo";
    private static final String EXTRA_BANK_ACCOUNT_NAME = "bankAccountName";
    private static final String EXTRA_BANK_NAME = "bankName";
    private static final String EXTRA_BANK_BRANCH_NAME = "bankBranchName";
    private static final String EXTRA_BANK_CODE = "bankCode";
    private static final String EXTRA_PROVINCE_CODE = "provinceCode";
    private static final String EXTRA_CITY_CODE = "cityCode";
    private static final String EXTRA_AREA_CODE = "areaCode";
    private static final String EXTRA_ADDRESS = "address";
    private static final String EXTRA_RECEIVER_ADDRESS = "receiverAddress";
    private static final String EXTRA_MOBILE = "mobile";
    private static final String EXTRA_EMAIL = "email";
    private final String subMerchantId;
    private final String bankAccountNo;
    private final String bankAccountName;
    private final String bankName;
    private final String bankBranchName;
    private final String bankCode;
    private final String provinceCode;
    private final String cityCode;
    private final String areaCode;
    private final String address;
    private final String receiverAddress;
    private final String mobile;
    private final String email;

    private PayeaseTransferExtras(String subMerchantId, String bankAccountNo, String bankAccountName,
                                  String bankName, String bankBranchName, String bankCode,
                                  String provinceCode, String cityCode, String areaCode,
                                  String address, String receiverAddress, String mobile, String email) {
        this.subMerchantId = subMerchantId;
        this.bankAccountNo = bankAccountNo;
        this.bankAccountName = bankAccountName;
        this.bankName = bankName;
        this.bankBranchName = bankBranchName;
        this.bankCode = bankCode;
        this.provinceCode = provinceCode;
        this.cityCode = cityCode;
        this.areaCode = areaCode;
        this.address = address;
        this.receiverAddress = receiverAddress;
        this.mobile = mobile;
        this.email = email;
    }

    /**
     * 从统一的转账请求中提取并验证首信易所需字段。
     *
     * @param reqDTO 统一转账请求
     * @return 结构化的附加信息
     */
    static PayeaseTransferExtras from(PayTransferUnifiedInnerReqDTO reqDTO) {
        Map<String, String> extras = reqDTO.getChannelExtras();
        String subMerchantId = firstNotBlank(reqDTO.getReceiverId(), extras, EXTRA_SUB_MERCHANT_ID);
        String bankAccountNo = trimOrNull(mandatoryValue(extras, EXTRA_BANK_ACCOUNT_NO));
        String bankAccountName = trimOrNull(mandatoryValue(extras, EXTRA_BANK_ACCOUNT_NAME));
        String bankName = trimOrNull(mandatoryValue(extras, EXTRA_BANK_NAME));
        String bankBranchName = extras != null ? extras.get(EXTRA_BANK_BRANCH_NAME) : null;
        String bankCode = extras != null ? extras.get(EXTRA_BANK_CODE) : null;
        String provinceCode = extras != null ? extras.get(EXTRA_PROVINCE_CODE) : null;
        String cityCode = extras != null ? extras.get(EXTRA_CITY_CODE) : null;
        String areaCode = extras != null ? extras.get(EXTRA_AREA_CODE) : null;
        String address = extras != null ? extras.get(EXTRA_ADDRESS) : null;
        String receiverAddress = extras != null ? extras.get(EXTRA_RECEIVER_ADDRESS) : null;
        String mobile = extras != null ? extras.get(EXTRA_MOBILE) : null;
        String email = extras != null ? extras.get(EXTRA_EMAIL) : null;

        if (StrUtil.isBlank(subMerchantId)) {
            throw new ServiceException(BAD_REQUEST.getCode(), "缺少首信易子商户号，无法发起渠道打款");
        }
        if (StrUtil.isBlank(bankAccountNo) || StrUtil.isBlank(bankAccountName) || StrUtil.isBlank(bankName)) {
            throw new ServiceException(BAD_REQUEST.getCode(), "银行卡信息不完整，无法发起渠道打款");
        }
        return new PayeaseTransferExtras(subMerchantId, bankAccountNo, bankAccountName, bankName, bankBranchName,
                bankCode, provinceCode, cityCode, areaCode, address, receiverAddress, mobile, email);
    }

    private static String mandatoryValue(Map<String, String> extras, String key) {
        if (extras == null) {
            return null;
        }
        return extras.get(key);
    }

    private static String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = StrUtil.trim(value);
        return StrUtil.isEmpty(trimmed) ? null : trimmed;
    }

    private static String firstNotBlank(String primary, Map<String, String> extras, String key) {
        if (StrUtil.isNotBlank(primary)) {
            return primary;
        }
        if (extras == null) {
            return null;
        }
        return extras.get(key);
    }
}
