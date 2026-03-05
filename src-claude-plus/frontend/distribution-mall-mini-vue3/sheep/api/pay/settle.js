import request from '@/sheep/request'

export const getSettleProfile = () =>
  request.get('/app/pay/settle-account/profile')

export const saveSettleDraft = (data) =>
  request.post('/app/pay/settle-account/draft', data, { custom: { showLoading: false } })

export const submitSettleAccount = () =>
  request.post('/app/pay/settle-account/submit', {}, { custom: { showLoading: false } })

export const saveChangeCardDraft = (data) =>
  request.post('/app/pay/settle-account/change-card/draft', data, { custom: { showLoading: false } })

export const submitChangeCard = () =>
  request.post('/app/pay/settle-account/change-card/submit', {}, { custom: { showLoading: false } })
