'use client';

import { useRef, useState, useEffect } from 'react';
import { TrendingUp, TrendingDown, DollarSign, ChevronLeft, ChevronRight } from 'lucide-react';
import type { AiIncome, StockPositionDetail } from '@/types';

interface AIPositionPanelProps {
  aiIncome?: AiIncome;
  aiCode?: string;
  isLoading?: boolean;
}

export default function AIPositionPanel({
  aiIncome,
  aiCode,
  isLoading,
}: AIPositionPanelProps) {
  const scrollContainerRef = useRef<HTMLDivElement>(null);
  const [canScrollLeft, setCanScrollLeft] = useState(false);
  const [canScrollRight, setCanScrollRight] = useState(false);

  const checkScrollability = () => {
    const container = scrollContainerRef.current;
    if (container) {
      setCanScrollLeft(container.scrollLeft > 0);
      setCanScrollRight(
        container.scrollLeft < container.scrollWidth - container.clientWidth - 10
      );
    }
  };

  useEffect(() => {
    checkScrollability();
    const container = scrollContainerRef.current;
    if (container) {
      container.addEventListener('scroll', checkScrollability);
      window.addEventListener('resize', checkScrollability);
      return () => {
        container.removeEventListener('scroll', checkScrollability);
        window.removeEventListener('resize', checkScrollability);
      };
    }
  }, [aiIncome?.positionDetails]);

  const scroll = (direction: 'left' | 'right') => {
    const container = scrollContainerRef.current;
    if (container) {
      const cardWidth = 240; // 卡片宽度
      const gap = 12; // 间距
      const scrollAmount = cardWidth + gap;
      container.scrollBy({
        left: direction === 'left' ? -scrollAmount : scrollAmount,
        behavior: 'smooth',
      });
    }
  };

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

  const positions = aiIncome?.positionDetails || [];
  const totalProfit = aiIncome?.income || 0;
  const yieldRate = (aiIncome?.yieldRate || 0) * 100;
  const realizedProfit = aiIncome?.realizedProfit || 0;
  const unrealizedProfit = aiIncome?.unrealizedProfit || 0;
  const totalCost = aiIncome?.totalCost || 0;

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
      <div className="mb-4 p-4 bg-gradient-to-r from-blue-50 to-indigo-50 dark:from-blue-900/20 dark:to-indigo-900/20 rounded-lg">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm text-gray-600 dark:text-gray-400 mb-1">总收益</p>
            <p className={`text-2xl font-bold ${totalProfit >= 0 ? 'text-green-600' : 'text-red-600'}`}>
              {totalProfit >= 0 ? '+' : ''}${totalProfit.toFixed(2)}
            </p>
          </div>
          <div className="text-right">
            <p className="text-sm text-gray-600 dark:text-gray-400 mb-1">整体收益率</p>
            <p className={`text-xl font-bold ${yieldRate >= 0 ? 'text-green-600' : 'text-red-600'}`}>
              {yieldRate >= 0 ? '+' : ''}{yieldRate.toFixed(2)}%
            </p>
          </div>
        </div>
      </div>

      {/* 收益分解 */}
      <div className="grid grid-cols-2 gap-3 mb-4">
        <div className="p-3 bg-green-50 dark:bg-green-900/20 rounded-lg">
          <div className="flex items-center gap-2 mb-1">
            <TrendingUp className="w-4 h-4 text-green-600" />
            <p className="text-xs text-gray-600 dark:text-gray-400">已实现</p>
          </div>
          <p className="text-lg font-bold text-green-600">
            +${realizedProfit.toFixed(2)}
          </p>
        </div>
        <div className={`p-3 rounded-lg ${unrealizedProfit >= 0 ? 'bg-green-50 dark:bg-green-900/20' : 'bg-red-50 dark:bg-red-900/20'}`}>
          <div className="flex items-center gap-2 mb-1">
            {unrealizedProfit >= 0 ? (
              <TrendingUp className="w-4 h-4 text-green-600" />
            ) : (
              <TrendingDown className="w-4 h-4 text-red-600" />
            )}
            <p className="text-xs text-gray-600 dark:text-gray-400">未实现</p>
          </div>
          <p className={`text-lg font-bold ${unrealizedProfit >= 0 ? 'text-green-600' : 'text-red-600'}`}>
            {unrealizedProfit >= 0 ? '+' : ''}${unrealizedProfit.toFixed(2)}
          </p>
        </div>
      </div>

      {/* 总成本 */}
      <div className="p-3 bg-gray-50 dark:bg-gray-800 rounded-lg mb-4">
        <div className="flex items-center gap-2 mb-1">
          <DollarSign className="w-4 h-4 text-orange-600" />
          <p className="text-xs text-gray-600 dark:text-gray-400">总成本</p>
        </div>
        <p className="text-xl font-bold text-gray-900 dark:text-white">
          ${totalCost.toFixed(2)}
        </p>
      </div>

      {/* 持仓明细 - 左右滑动 */}
      <div className="flex-1 min-h-0">
        <div className="flex items-center justify-between mb-2">
          <h3 className="text-sm font-semibold text-gray-700 dark:text-gray-300">
            持仓明细
          </h3>
          {positions.length > 0 && (
            <div className="flex items-center gap-1">
              <button
                onClick={() => scroll('left')}
                disabled={!canScrollLeft}
                className={`p-1.5 rounded-full transition-all ${
                  canScrollLeft
                    ? 'bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300'
                    : 'bg-gray-100 dark:bg-gray-800 text-gray-400 dark:text-gray-600 cursor-not-allowed'
                }`}
              >
                <ChevronLeft className="w-4 h-4" />
              </button>
              <button
                onClick={() => scroll('right')}
                disabled={!canScrollRight}
                className={`p-1.5 rounded-full transition-all ${
                  canScrollRight
                    ? 'bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300'
                    : 'bg-gray-100 dark:bg-gray-800 text-gray-400 dark:text-gray-600 cursor-not-allowed'
                }`}
              >
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          )}
        </div>

        {positions.length === 0 ? (
          <div className="text-center py-8 text-gray-500 dark:text-gray-400">
            <p>暂无持仓数据</p>
          </div>
        ) : (
          <div className="relative">
            {/* 左渐变遮罩 */}
            {canScrollLeft && (
              <div className="absolute left-0 top-0 bottom-0 w-8 bg-gradient-to-r from-white dark:from-gray-900 to-transparent z-10 pointer-events-none" />
            )}
            {/* 右渐变遮罩 */}
            {canScrollRight && (
              <div className="absolute right-0 top-0 bottom-0 w-8 bg-gradient-to-l from-white dark:from-gray-900 to-transparent z-10 pointer-events-none" />
            )}
            
            <div
              ref={scrollContainerRef}
              className="flex gap-3 overflow-x-auto pb-2 snap-x snap-mandatory"
              style={{ 
                scrollbarWidth: 'none', 
                msOverflowStyle: 'none',
                WebkitOverflowScrolling: 'touch'
              }}
            >
              {positions.map((position: StockPositionDetail, index: number) => {
                const isPositive = position.totalProfit >= 0;
                const returnRatePercent = (position.returnRate - 1) * 100;

                return (
                  <div
                    key={index}
                    className="flex-shrink-0 w-[240px] snap-start p-4 bg-gray-50 dark:bg-gray-800 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-all duration-200 hover:shadow-md border border-transparent hover:border-gray-200 dark:hover:border-gray-700"
                  >
                    {/* 股票头部 */}
                    <div className="flex items-center justify-between mb-2">
                      <div className="min-w-0 flex-1">
                        <p className="font-semibold text-gray-900 dark:text-white text-sm truncate">
                          {position.stockName || position.stockCode}
                        </p>
                        <p className="text-[10px] text-gray-500 dark:text-gray-400">
                          {position.stockCode}
                        </p>
                      </div>
                      {isPositive ? (
                        <TrendingUp className="w-4 h-4 text-green-600 flex-shrink-0 ml-1" />
                      ) : (
                        <TrendingDown className="w-4 h-4 text-red-600 flex-shrink-0 ml-1" />
                      )}
                    </div>

                    {/* 持仓量和收益率 */}
                    <div className="flex items-center justify-between mb-2">
                      <div>
                        <p className="text-[10px] text-gray-500 dark:text-gray-400">持仓</p>
                        <p className="text-base font-bold text-gray-900 dark:text-white">
                          {position.position}
                        </p>
                      </div>
                      <div className="text-right">
                        <p className="text-[10px] text-gray-500 dark:text-gray-400">收益率</p>
                        <p className={`text-sm font-bold ${returnRatePercent >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                          {returnRatePercent >= 0 ? '+' : ''}{returnRatePercent.toFixed(1)}%
                        </p>
                      </div>
                    </div>

                    {/* 成本和价格 */}
                    <div className="grid grid-cols-2 gap-2 mb-2 p-1.5 bg-white dark:bg-gray-900 rounded text-xs">
                      <div>
                        <p className="text-[10px] text-gray-500 dark:text-gray-400">成本</p>
                        <p className="font-semibold text-gray-900 dark:text-white">
                          ${position.averageCost.toFixed(1)}
                        </p>
                      </div>
                      <div className="text-right">
                        <p className="text-[10px] text-gray-500 dark:text-gray-400">现价</p>
                        <p className="font-semibold text-gray-900 dark:text-white">
                          ${position.currentPrice.toFixed(1)}
                        </p>
                      </div>
                    </div>

                    {/* 收益分解 */}
                    <div className="border-t border-gray-200 dark:border-gray-700 pt-2">
                      <div className="grid grid-cols-3 gap-1 text-center">
                        <div>
                          <p className="text-[9px] text-gray-500 dark:text-gray-400">已实</p>
                          <p className="text-[10px] font-semibold text-green-600">
                            +{position.realizedProfit.toFixed(0)}
                          </p>
                        </div>
                        <div>
                          <p className="text-[9px] text-gray-500 dark:text-gray-400">未实</p>
                          <p className={`text-[10px] font-semibold ${position.unrealizedProfit >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                            {position.unrealizedProfit >= 0 ? '+' : ''}{position.unrealizedProfit.toFixed(0)}
                          </p>
                        </div>
                        <div>
                          <p className="text-[9px] text-gray-500 dark:text-gray-400">总盈亏</p>
                          <p className={`text-xs font-bold ${isPositive ? 'text-green-600' : 'text-red-600'}`}>
                            {isPositive ? '+' : ''}{position.totalProfit.toFixed(0)}
                          </p>
                        </div>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
