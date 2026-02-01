'use client';

import { useMemo } from 'react';
import { TrendingUp, TrendingDown, DollarSign, BarChart3 } from 'lucide-react';
import type { AiIncome, Position } from '@/types';

interface AIPositionPanelProps {
  aiIncome?: AiIncome;
  positions?: Position[];
  aiCode?: string;
  isLoading?: boolean;
}

export default function AIPositionPanel({
  aiIncome,
  positions = [],
  aiCode,
  isLoading,
}: AIPositionPanelProps) {
  const totalProfit = useMemo(() => {
    if (!positions.length) return 0;
    return positions.reduce((sum, pos) => sum + (pos.profit || 0), 0);
  }, [positions]);

  const totalProfitRate = useMemo(() => {
    if (!positions.length) return 0;
    const totalCost = positions.reduce(
      (sum, pos) => sum + Math.abs(pos.volume) * (pos.currentPrice || 0),
      0
    );
    return totalCost !== 0 ? (totalProfit / totalCost) * 100 : 0;
  }, [positions, totalProfit]);

  if (isLoading) {
    return (
      <div className="w-full h-full bg-white dark:bg-gray-900 rounded-lg shadow-lg p-6">
        <div className="animate-pulse">
          <div className="h-6 bg-gray-200 dark:bg-gray-700 rounded w-1/3 mb-4"></div>
          <div className="space-y-3">
            <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded"></div>
            <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded"></div>
            <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded"></div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="w-full h-full bg-white dark:bg-gray-900 rounded-lg shadow-lg p-6 flex flex-col">
      <div className="mb-6">
        <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-2">
          AI持仓分析
        </h2>
        {aiCode && (
          <p className="text-sm text-gray-500 dark:text-gray-400">模型: {aiCode}</p>
        )}
      </div>

      {/* 总收益卡片 */}
      <div className="mb-6 p-4 bg-gradient-to-r from-blue-50 to-indigo-50 dark:from-blue-900/20 dark:to-indigo-900/20 rounded-lg">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm text-gray-600 dark:text-gray-400 mb-1">总收益</p>
            <p className="text-3xl font-bold text-gray-900 dark:text-white">
              ${aiIncome?.incomeTotal?.toFixed(2) || totalProfit.toFixed(2)}
            </p>
          </div>
          <div className="text-right">
            <p className="text-sm text-gray-600 dark:text-gray-400 mb-1">收益率</p>
            <p
              className={`text-2xl font-bold ${
                totalProfitRate >= 0 ? 'text-green-600' : 'text-red-600'
              }`}
            >
              {totalProfitRate >= 0 ? '+' : ''}
              {totalProfitRate.toFixed(2)}%
            </p>
          </div>
        </div>
      </div>

      {/* 统计卡片 */}
      <div className="grid grid-cols-2 gap-4 mb-6">
        <div className="p-4 bg-gray-50 dark:bg-gray-800 rounded-lg">
          <div className="flex items-center gap-2 mb-2">
            <BarChart3 className="w-5 h-5 text-blue-600" />
            <p className="text-sm text-gray-600 dark:text-gray-400">持仓数量</p>
          </div>
          <p className="text-2xl font-bold text-gray-900 dark:text-white">
            {positions.length}
          </p>
        </div>
        <div className="p-4 bg-gray-50 dark:bg-gray-800 rounded-lg">
          <div className="flex items-center gap-2 mb-2">
            <DollarSign className="w-5 h-5 text-green-600" />
            <p className="text-sm text-gray-600 dark:text-gray-400">盈利股票</p>
          </div>
          <p className="text-2xl font-bold text-gray-900 dark:text-white">
            {positions.filter((p) => (p.profit || 0) > 0).length}
          </p>
        </div>
      </div>

      {/* 持仓列表 */}
      <div className="flex-1 overflow-y-auto">
        <h3 className="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-3">
          持仓明细
        </h3>
        <div className="space-y-3">
          {positions.length === 0 ? (
            <div className="text-center py-8 text-gray-500 dark:text-gray-400">
              <p>暂无持仓数据</p>
            </div>
          ) : (
            positions.map((position, index) => {
              const profit = position.profit || 0;
              const profitRate = position.profitRate || 0;
              const isPositive = profit >= 0;

              return (
                <div
                  key={index}
                  className="p-3 bg-gray-50 dark:bg-gray-800 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
                >
                  <div className="flex items-center justify-between mb-2">
                    <div>
                      <p className="font-semibold text-gray-900 dark:text-white">
                        {position.stockName || position.stockCode}
                      </p>
                      <p className="text-xs text-gray-500 dark:text-gray-400">
                        {position.stockCode} · {position.date}
                      </p>
                    </div>
                    {isPositive ? (
                      <TrendingUp className="w-5 h-5 text-green-600" />
                    ) : (
                      <TrendingDown className="w-5 h-5 text-red-600" />
                    )}
                  </div>
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-xs text-gray-500 dark:text-gray-400">持仓量</p>
                      <p className="text-sm font-medium text-gray-900 dark:text-white">
                        {Math.abs(position.volume).toFixed(2)}
                      </p>
                    </div>
                    <div className="text-right">
                      <p className="text-xs text-gray-500 dark:text-gray-400">盈亏</p>
                      <p
                        className={`text-sm font-semibold ${
                          isPositive ? 'text-green-600' : 'text-red-600'
                        }`}
                      >
                        {isPositive ? '+' : ''}${profit.toFixed(2)} ({isPositive ? '+' : ''}
                        {profitRate.toFixed(2)}%)
                      </p>
                    </div>
                  </div>
                </div>
              );
            })
          )}
        </div>
      </div>

      {/* 按股票收益统计 */}
      {aiIncome?.earnInStocks && aiIncome.earnInStocks.length > 0 && (
        <div className="mt-6 pt-6 border-t border-gray-200 dark:border-gray-700">
          <h3 className="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-3">
            股票收益排行
          </h3>
          <div className="space-y-2">
            {aiIncome.earnInStocks
              .sort((a, b) => b.income - a.income)
              .slice(0, 5)
              .map((stock, index) => (
                <div
                  key={index}
                  className="flex items-center justify-between p-2 bg-gray-50 dark:bg-gray-800 rounded"
                >
                  <span className="text-sm text-gray-700 dark:text-gray-300">
                    {stock.stockCode}
                  </span>
                  <span
                    className={`text-sm font-semibold ${
                      stock.income >= 0 ? 'text-green-600' : 'text-red-600'
                    }`}
                  >
                    {stock.income >= 0 ? '+' : ''}${stock.income.toFixed(2)}
                  </span>
                </div>
              ))}
          </div>
        </div>
      )}
    </div>
  );
}
