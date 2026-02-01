'use client';

import { useState } from 'react';
import StockChart from '@/components/StockChart';
import AIPositionPanel from '@/components/AIPositionPanel';
import ChatInterface from '@/components/ChatInterface';
import AIDecisionPanel from '@/components/AIDecisionPanel';
import { useStockData } from '@/hooks/useStockData';
import { Search, RefreshCw, Loader2 } from 'lucide-react';

export default function Home() {
  const [stockCode, setStockCode] = useState('AAPL'); // 默认股票代码
  const [aiCode, setAiCode] = useState('AI001'); // 默认AI模型代码
  const [inputStockCode, setInputStockCode] = useState('AAPL');
  const [inputAiCode, setInputAiCode] = useState('AI001');

  const { stockInfo, aiIncome, positions, loading, error, refetch } = useStockData(
    stockCode,
    aiCode
  );

  const handleSearch = () => {
    if (inputStockCode.trim()) {
      setStockCode(inputStockCode.trim().toUpperCase());
    }
    if (inputAiCode.trim()) {
      setAiCode(inputAiCode.trim());
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-950">
      {/* 顶部导航栏 */}
      <header className="bg-white dark:bg-gray-900 shadow-sm border-b border-gray-200 dark:border-gray-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
                AI模拟投资分析平台
              </h1>
              <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
                智能股票分析与AI投资顾问
              </p>
            </div>
            <div className="flex items-center gap-4">
              {/* 股票代码输入 */}
              <div className="flex items-center gap-2">
                <label className="text-sm text-gray-600 dark:text-gray-400">股票代码:</label>
                <input
                  type="text"
                  value={inputStockCode}
                  onChange={(e) => setInputStockCode(e.target.value.toUpperCase())}
                  onKeyPress={handleKeyPress}
                  placeholder="如: AAPL"
                  className="px-3 py-1.5 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-800 dark:text-white text-sm"
                />
              </div>
              {/* AI模型代码输入 */}
              <div className="flex items-center gap-2">
                <label className="text-sm text-gray-600 dark:text-gray-400">AI模型:</label>
                <input
                  type="text"
                  value={inputAiCode}
                  onChange={(e) => setInputAiCode(e.target.value)}
                  onKeyPress={handleKeyPress}
                  placeholder="如: AI001"
                  className="px-3 py-1.5 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-800 dark:text-white text-sm"
                />
              </div>
              <button
                onClick={handleSearch}
                className="px-4 py-1.5 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors flex items-center gap-2 text-sm"
              >
                <Search className="w-4 h-4" />
                查询
              </button>
              <button
                onClick={refetch}
                disabled={loading}
                className="px-4 py-1.5 bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-md hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors flex items-center gap-2 text-sm disabled:opacity-50"
              >
                {loading ? (
                  <Loader2 className="w-4 h-4 animate-spin" />
                ) : (
                  <RefreshCw className="w-4 h-4" />
                )}
                刷新
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* 主内容区域 */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        {error && (
          <div className="mb-4 p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
            <p className="text-red-800 dark:text-red-200">{error}</p>
          </div>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6">
          {/* 左侧：股票图表（占2列） */}
          <div className="lg:col-span-2">
            {loading ? (
              <div className="w-full h-[600px] bg-white dark:bg-gray-900 rounded-lg shadow-lg flex items-center justify-center">
                <div className="text-center">
                  <Loader2 className="w-12 h-12 animate-spin text-blue-600 mx-auto mb-4" />
                  <p className="text-gray-600 dark:text-gray-400">加载股票数据中...</p>
                </div>
              </div>
            ) : stockInfo?.stockBase && stockInfo.stockBase.length > 0 ? (
              <StockChart
                data={stockInfo.stockBase}
                stockCode={stockCode}
                stockName={stockInfo.metaData?.symbol}
              />
            ) : (
              <div className="w-full h-[600px] bg-white dark:bg-gray-900 rounded-lg shadow-lg flex items-center justify-center">
                <p className="text-gray-500 dark:text-gray-400">
                  暂无数据，请输入股票代码查询
                </p>
              </div>
            )}
          </div>

          {/* 右侧：AI持仓面板（占1列） */}
          <div className="lg:col-span-1">
            <AIPositionPanel
              aiIncome={aiIncome || undefined}
              positions={positions}
              aiCode={aiCode}
              isLoading={loading}
            />
          </div>
        </div>

        {/* AI投资决策模块 */}
        <div className="mb-6">
          <AIDecisionPanel
            aiCode={aiCode}
            modelId={1} // TODO: 从aiCode解析出实际的modelId
          />
        </div>

        {/* 底部：智能对话界面 */}
        <div className="h-[400px]">
          <ChatInterface
            stockCode={stockCode}
            stockName={stockInfo?.metaData?.symbol}
          />
        </div>
      </main>

      {/* 页脚 */}
      <footer className="mt-12 py-6 border-t border-gray-200 dark:border-gray-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center text-sm text-gray-500 dark:text-gray-400">
          <p>© 2026 AI模拟投资分析平台. 仅供学习研究使用，不构成投资建议。</p>
        </div>
      </footer>
    </div>
  );
}
