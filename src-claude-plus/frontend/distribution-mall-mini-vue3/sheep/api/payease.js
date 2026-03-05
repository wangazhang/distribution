import request from '@/sheep/utils/request.js'

export const getSubMerchant = () =>
  request.get('/app/pay/payease/sub-merchant/get')

export const saveSubMerchantDraft = (data) =>
  request.post('/app/pay/payease/sub-merchant/draft', data)

export const submitSubMerchant = () =>
  request.post('/app/pay/payease/sub-merchant/submit')
