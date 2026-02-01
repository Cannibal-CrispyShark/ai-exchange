import { useState, useEffect } from 'react';
import { getStockInfo, getAiIncome } from '@/lib/api';
import type { StockInfoVO, AiIncome, Position } from '@/types';

export function useStockData(stockCode: string, aiCode?: string) {
  const [stockInfo, setStockInfo] = useState<StockInfoVO | null>(null);
  const [aiIncome, setAiIncome] = useState<AiIncome | null>(null);
  const [positions, setPositions] = useState<Position[]>([]);
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

        // 获取AI收益数据（如果提供了aiCode）
        if (aiCode) {
          try {
            const incomeData = await getAiIncome(aiCode);
            setAiIncome(incomeData);

            // 将收益数据转换为持仓信息（示例转换逻辑）
            const positionList: Position[] = incomeData.earnInStocks?.map((stock) => ({
              stockCode: stock.stockCode,
              stockName: stock.stockCode,
              volume: 0, // 这里需要根据实际数据调整
              date: new Date().toISOString().split('T')[0],
              profit: stock.income,
              profitRate: 0, // 需要计算
            })) || [];
            setPositions(positionList);
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
  }, [stockCode, aiCode]);

  return {
    stockInfo,
    aiIncome,
    positions,
    loading,
    error,
    refetch: () => {
      const fetchData = async () => {
        if (!stockCode) return;
        setLoading(true);
        try {
          const stockData = await getStockInfo(stockCode);
          setStockInfo(stockData);
          if (aiCode) {
            const incomeData = await getAiIncome(aiCode);
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
