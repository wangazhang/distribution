// 第三方码表（地址）查询 API
import request from '@/sheep/request'

export function listProvinces(provider = 'PAYEASE') {
  return request({
    url: '/infra/third-codes/address/provinces',
    method: 'GET',
    params: { provider },
  })
}

export function listCities(parentCode, provider = 'PAYEASE') {
  return request({
    url: '/infra/third-codes/address/cities',
    method: 'GET',
    params: { parentCode, provider },
  })
}

export function listDistricts(parentCode, provider = 'PAYEASE') {
  return request({
    url: '/infra/third-codes/address/districts',
    method: 'GET',
    params: { parentCode, provider },
  })
}

const ThirdCodesApi = { listProvinces, listCities, listDistricts }
export default ThirdCodesApi

