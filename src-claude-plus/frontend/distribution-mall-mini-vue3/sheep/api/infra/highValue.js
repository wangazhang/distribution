import { baseUrl, apiPath, tenantId } from '@/sheep/config';

const HighValueApi = {
  // 上传身份证图片：subPath 可为 'front' 或 'back'
  uploadIdCard: (filePath, subPath = '') => {
    const token = uni.getStorageSync('token');
    return new Promise((resolve) => {
      uni.uploadFile({
        url: baseUrl + apiPath + '/infra/high-value-pic/card/upload',
        filePath,
        name: 'file',
        formData: { subPath },
        header: {
          Accept: '*/*',
          'tenant-id': tenantId,
          Authorization: token ? `Bearer ${token}` : undefined,
        },
        success: (res) => {
          try {
            const result = JSON.parse(res.data);
            if (result.code === 0) {
              resolve({ url: result.data });
            } else {
              uni.showToast({ icon: 'none', title: result.msg || '上传失败' });
              resolve(false);
            }
          } catch (e) {
            resolve(false);
          }
        },
        fail: () => resolve(false),
      });
    });
  },

  // 上传会员资料图片：用于银行卡等高价值资料
  uploadMemberImage: (filePath, subPath = '') => {
    const token = uni.getStorageSync('token');
    return new Promise((resolve) => {
      uni.uploadFile({
        url: baseUrl + apiPath + '/infra/high-value-pic/member/upload',
        filePath,
        name: 'file',
        formData: { subPath },
        header: {
          Accept: '*/*',
          'tenant-id': tenantId,
          Authorization: token ? `Bearer ${token}` : undefined,
        },
        success: (res) => {
          try {
            const result = JSON.parse(res.data);
            if (result.code === 0) {
              resolve({ url: result.data });
            } else {
              uni.showToast({ icon: 'none', title: result.msg || '上传失败' });
              resolve(false);
            }
          } catch (e) {
            resolve(false);
          }
        },
        fail: () => resolve(false),
      });
    });
  },
};

export default HighValueApi;
