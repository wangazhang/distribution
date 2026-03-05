package com.hissp.distribution.framework.pay.core.client.impl.payease;

import cn.hutool.core.util.StrUtil;
import com.hissp.distribution.framework.pay.core.client.impl.payease.dto.PayeaseSubMerchantDeclareDTO;
import com.upay.sdk.serviceprovider.v3_0.declaration.builder.DeclarationBuilder;
import com.upay.sdk.serviceprovider.v3_0.declaration.entity.BankCardInfo;
import com.upay.sdk.serviceprovider.v3_0.declaration.entity.BaseInfo;
import com.upay.sdk.serviceprovider.v3_0.declaration.entity.CertificateInfo;
import com.upay.sdk.serviceprovider.v3_0.declaration.entity.ContractInfo;
import lombok.experimental.UtilityClass;

/**
 * 将业务字段映射为首信易 SDK 实体
 */
@UtilityClass
public class PayeaseDeclarationMapper {

    public DeclarationBuilder build(PayeaseSubMerchantDeclareDTO dto, String merchantId) {
        DeclarationBuilder builder = new DeclarationBuilder(merchantId);
        builder.setRequestId(dto.getRequestId());
        builder.setOperationType(dto.getOperationType());
        if (StrUtil.isNotBlank(dto.getNotifyUrl())) {
            builder.setNotifyUrl(dto.getNotifyUrl());
        }
        builder.setExtendedParameters("sendActiveEmail:FALSE,sendExpressPayMsg:FALSE");
        builder.setBaseInfo(buildBaseInfo(dto));
        builder.setBankCardInfo(buildBankInfo(dto));
        builder.setCertificateInfo(buildCertificateInfo(dto));
        builder.setContractInfo(buildContractInfo(dto));
        return builder;
    }

    private BaseInfo buildBaseInfo(PayeaseSubMerchantDeclareDTO dto) {
        BaseInfo baseInfo = new BaseInfo();
        baseInfo.setSignedType(blankToDefault(dto.getSignedType(), "BY_SPLIT_BILL"));
        baseInfo.setRegisterRole(blankToDefault(dto.getRegisterRole(), "NATURAL_PERSON"));
        //baseInfo.setMerchantType(blankToDefault(dto.getMerchantType(), "PERSONAL"));
        baseInfo.setCerType(blankToDefault(dto.getCerType(), "ID_CARD"));
        baseInfo.setSignedName(dto.getSignedName());
        baseInfo.setSignedShorthand(StrUtil.blankToDefault(dto.getSignedShortName(), dto.getSignedName()));
        baseInfo.setContactName(dto.getSignedName());
        baseInfo.setContactEmail(dto.getEmail());
        baseInfo.setContactPhone(dto.getMobile());
        baseInfo.setBusinessAddressProvince(dto.getProvinceCode());
        baseInfo.setBusinessAddressCity(dto.getCityCode());
        baseInfo.setBusinessAddressArea(StrUtil.blankToDefault(dto.getAreaCode(), dto.getCityCode()));
        baseInfo.setBusinessAddress(dto.getAddress());
        baseInfo.setBusinessClassification(dto.getBusinessClassification());
        baseInfo.setBelongAgentNumber(dto.getBelongAgentNumber());
        baseInfo.setProductPackage(dto.getProductPackage());
        baseInfo.setDesireAuth(dto.getDesireAuth());
        return baseInfo;
    }

    private BankCardInfo buildBankInfo(PayeaseSubMerchantDeclareDTO dto) {
        BankCardInfo bank = new BankCardInfo();
        bank.setBankName(dto.getBankName());
        bank.setBankBranchName(dto.getBankBranchName());
        bank.setAccountName(dto.getBankAccountName());
        bank.setBankCardNo(dto.getBankAccountNo());
        bank.setProvinceCode(dto.getProvinceCode());
        bank.setCityCode(dto.getCityCode());
        bank.setReservedPhoneNo(dto.getBankReservedPhone());
        bank.setAccountType(blankToDefault(dto.getAccountType(), "PRIVATE"));
        bank.setLiquidationType(blankToDefault(dto.getLiquidationType(), "WITHDRAW"));
        bank.setWithdrawRateType(blankToDefault(dto.getWithdrawRateType(), "RATE"));
        bank.setWithdrawRate(blankToDefault(dto.getWithdrawRate(), "0.6")); //提现按千六
        bank.setReserveDays(dto.getReserveDays());
        return bank;
    }

    private CertificateInfo buildCertificateInfo(PayeaseSubMerchantDeclareDTO dto) {
        CertificateInfo certificate = new CertificateInfo();
        certificate.setLegalPersonName(dto.getSignedName());
        certificate.setProfession("9");//文职类
        certificate.setLegalPersonPhone(dto.getMobile());
        certificate.setLegalPersonIdType("IDCARD");
        certificate.setLegalPersonIdNo(dto.getIdCardNo());
        certificate.setIdEffectiveDateStart(dto.getIdCardValidStart());
        certificate.setIdEffectiveDateEnd(dto.getIdCardValidEnd());
        certificate.setLegalPersonBankCardPath(dto.getBankCardFrontPath());
        certificate.setLegalIdCardProsPath(dto.getIdCardFrontPath());
        certificate.setLegalIdCardConsPath(dto.getIdCardBackPath());
        certificate.setOtherCerPath(dto.getExtra());
        return certificate;
    }

    private ContractInfo buildContractInfo(PayeaseSubMerchantDeclareDTO dto) {
        ContractInfo contractInfo = new ContractInfo();
        contractInfo.setReceiverName(StrUtil.blankToDefault(dto.getContractReceiverName(), dto.getSignedName()));
        contractInfo.setReceiverPhone(StrUtil.blankToDefault(dto.getContractReceiverPhone(), dto.getMobile()));
        contractInfo.setReceiverAddress(StrUtil.blankToDefault(dto.getContractReceiverAddress(), dto.getAddress()));
        if (StrUtil.isNotBlank(dto.getContractType())) {
            contractInfo.setContractType(dto.getContractType());
        }
        return contractInfo;
    }

    private String blankToDefault(String value, String defaultValue) {
        return StrUtil.blankToDefault(value, defaultValue);
    }
}
