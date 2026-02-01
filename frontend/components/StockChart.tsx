'use client';

import { useMemo } from 'react';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  AreaChart,
  Area,
} from 'recharts';
import type { StockBase } from '@/types';
import { TrendingUp, TrendingDown } from 'lucide-react';

interface StockChartProps {
  data: StockBase[];
  stockCode?: string;
  stockName?: string;
}

export default function StockChart({ data, stockCode, stockName }: StockChartProps) {
  const chartData = useMemo(() => {
    return data
      .map((item) => ({
        date: item.time,
        open: Number(item.open),
        high: Number(item.high),
        low: Number(item.low),
        close: Number(item.close),
        volume: Number(item.volume),
      }))
      .sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime());
  }, [data]);

  const latestPrice = chartData[chartData.length - 1]?.close || 0;
  const previousPrice = chartData[chartData.length - 2]?.close || latestPrice;
  const priceChange = latestPrice - previousPrice;
  const priceChangePercent = previousPrice !== 0 ? (priceChange / previousPrice) * 100 : 0;

  const isPositive = priceChange >= 0;

  return (
    <div className="w-full h-full bg-white dark:bg-gray-900 rounded-lg shadow-lg p-6">
      <div className="mb-6">
        <div className="flex items-center justify-between mb-2">
          <div>
            <h2 className="text-2xl font-bold text-gray-900 dark:text-white">
              {stockName || stockCode || '股票走势'}
            </h2>
            {stockCode && (
              <p className="text-sm text-gray-500 dark:text-gray-400">{stockCode}</p>
            )}
          </div>
          <div className="text-right">
            <div className="text-3xl font-bold text-gray-900 dark:text-white">
              ${latestPrice.toFixed(2)}
            </div>
            <div
              className={`flex items-center gap-1 text-sm ${
                isPositive ? 'text-green-600' : 'text-red-600'
              }`}
            >
              {isPositive ? (
                <TrendingUp className="w-4 h-4" />
              ) : (
                <TrendingDown className="w-4 h-4" />
              )}
              <span>
                {isPositive ? '+' : ''}
                {priceChange.toFixed(2)} ({isPositive ? '+' : ''}
                {priceChangePercent.toFixed(2)}%)
              </span>
            </div>
          </div>
        </div>
      </div>

      <ResponsiveContainer width="100%" height={400}>
        <AreaChart data={chartData}>
          <defs>
            <linearGradient id="colorPrice" x1="0" y1="0" x2="0" y2="1">
              <stop
                offset="5%"
                stopColor={isPositive ? '#10b981' : '#ef4444'}
                stopOpacity={0.3}
              />
              <stop
                offset="95%"
                stopColor={isPositive ? '#10b981' : '#ef4444'}
                stopOpacity={0}
              />
            </linearGradient>
          </defs>
          <CartesianGrid strokeDasharray="3 3" className="opacity-30" />
          <XAxis
            dataKey="date"
            tick={{ fontSize: 12 }}
            tickFormatter={(value) => {
              const date = new Date(value);
              return `${date.getMonth() + 1}/${date.getDate()}`;
            }}
          />
          <YAxis
            tick={{ fontSize: 12 }}
            domain={['auto', 'auto']}
            tickFormatter={(value) => `$${value.toFixed(2)}`}
          />
          <Tooltip
            contentStyle={{
              backgroundColor: 'rgba(255, 255, 255, 0.95)',
              border: '1px solid #e5e7eb',
              borderRadius: '8px',
            }}
            formatter={(value: number, name: string) => {
              if (name === 'volume') {
                return [value.toLocaleString(), '成交量'];
              }
              return [`$${value.toFixed(2)}`, name];
            }}
            labelFormatter={(label) => `日期: ${label}`}
          />
          <Legend />
          <Area
            type="monotone"
            dataKey="close"
            stroke={isPositive ? '#10b981' : '#ef4444'}
            strokeWidth={2}
            fill="url(#colorPrice)"
            name="收盘价"
          />
          <Line
            type="monotone"
            dataKey="open"
            stroke="#3b82f6"
            strokeWidth={1}
            dot={false}
            name="开盘价"
          />
          <Line
            type="monotone"
            dataKey="high"
            stroke="#8b5cf6"
            strokeWidth={1}
            dot={false}
            name="最高价"
          />
          <Line
            type="monotone"
            dataKey="low"
            stroke="#f59e0b"
            strokeWidth={1}
            dot={false}
            name="最低价"
          />
        </AreaChart>
      </ResponsiveContainer>

      <div className="mt-4 grid grid-cols-4 gap-4 text-sm">
        <div>
          <p className="text-gray-500 dark:text-gray-400">开盘</p>
          <p className="font-semibold text-gray-900 dark:text-white">
            ${chartData[chartData.length - 1]?.open.toFixed(2) || '0.00'}
          </p>
        </div>
        <div>
          <p className="text-gray-500 dark:text-gray-400">最高</p>
          <p className="font-semibold text-gray-900 dark:text-white">
            ${Math.max(...chartData.map((d) => d.high)).toFixed(2) || '0.00'}
          </p>
        </div>
        <div>
          <p className="text-gray-500 dark:text-gray-400">最低</p>
          <p className="font-semibold text-gray-900 dark:text-white">
            ${Math.min(...chartData.map((d) => d.low)).toFixed(2) || '0.00'}
          </p>
        </div>
        <div>
          <p className="text-gray-500 dark:text-gray-400">成交量</p>
          <p className="font-semibold text-gray-900 dark:text-white">
            {chartData[chartData.length - 1]?.volume.toLocaleString() || '0'}
          </p>
        </div>
      </div>
    </div>
  );
}
