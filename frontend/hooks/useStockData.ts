import { useState, useEffect } from 'react';
import { getStockInfo, getAiIncome } from '@/lib/api';
import type { StockInfoVO, AiIncome } from '@/types';

export function useStockData(stockCode: string, modelId?: number) {
  const [stockInfo, setStockInfo] = useState<StockInfoVO | null>(null);
  const [aiIncome, setAiIncome] = useState<AiIncome | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      if (!stockCode) return;

      setLoading(true);
      setError(null);

      try {
        // 获取股票数据
        const stockData = await getStockInfo(stockCode);
        setStockInfo(stockData);

        // 获取AI收益数据（如果提供了modelId）
        if (modelId) {
          try {
            const incomeData = await getAiIncome(modelId);
            setAiIncome(incomeData);
          } catch (err) {
            console.warn('获取AI收益数据失败:', err);
            // 不阻止页面加载，只是不显示AI数据
          }
        }
      } catch (err) {
        setError(err instanceof Error ? err.message : '获取数据失败');
        console.error('获取股票数据失败:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [stockCode, modelId]);

  return {
    stockInfo,
    aiIncome,
    loading,
    error,
    refetch: () => {
      const fetchData = async () => {
        if (!stockCode) return;
        setLoading(true);
        try {
          const stockData = await getStockInfo(stockCode);
          setStockInfo(stockData);
          if (modelId) {
            const incomeData = await getAiIncome(modelId);
            setAiIncome(incomeData);
          }
        } catch (err) {
          setError(err instanceof Error ? err.message : '获取数据失败');
        } finally {
          setLoading(false);
        }
      };
      fetchData();
    },
  };
}
