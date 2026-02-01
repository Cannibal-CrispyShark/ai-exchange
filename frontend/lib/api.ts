import axios from 'axios';
import type { ApiResponse, StockInfoVO, AiIncome } from '@/types';

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    // 后端返回的是Response对象，直接返回
    return response.data;
  },
  (error) => {
    console.error('API Error:', error);
    return Promise.reject(error);
  }
);

// 获取股票基础数据
export const getStockInfo = async (stockCode: string): Promise<StockInfoVO> => {
  const response = await api.get<ApiResponse<StockInfoVO>>(
    `/stocks/${stockCode}/get`
  );
  if (response.body && response.message === 'success') {
    return response.body;
  }
  throw new Error(response.message || '获取股票数据失败');
};

// 获取AI收益数据
export const getAiIncome = async (aiCode: string): Promise<AiIncome> => {
  const response = await api.get<ApiResponse<AiIncome>>(
    `/ai/${aiCode}/income`
  );
  if (response.body && response.message === 'success') {
    return response.body;
  }
  throw new Error(response.message || '获取AI收益数据失败');
};

// 发送对话消息（如果需要后端支持）
export const sendChatMessage = async (message: string): Promise<string> => {
  // 这里可以根据实际后端API进行调整
  const response = await api.post<ApiResponse<string>>('/chat', {
    message,
  });
  if (response.body && response.message === 'success') {
    return response.body;
  }
  throw new Error(response.message || '发送消息失败');
};

export default api;
