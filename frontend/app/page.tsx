'use client';

import { useState, useEffect } from 'react';
import StockChart from '@/components/StockChart';
import AIPositionPanel from '@/components/AIPositionPanel';
import ChatInterface from '@/components/ChatInterface';
import AIDecisionPanel from '@/components/AIDecisionPanel';
import { useStockData } from '@/hooks/useStockData';
import { Search, RefreshCw, Loader2, ChevronDown } from 'lucide-react';

// AI模型类型
interface AiModel {
  id: number;
  modelName: string;
  deposit: number;
  temperature: number;
}

export default function Home() {
  const [stockCode, setStockCode] = useState('AAPL'); // 默认股票代码
  const [aiCode, setAiCode] = useState('AI001'); // 默认AI模型代码
  const [selectedModelId, setSelectedModelId] = useState<number>(1); // 选中的模型ID
  const [inputStockCode, setInputStockCode] = useState('AAPL');
  const [aiModels, setAiModels] = useState<AiModel[]>([]); // AI模型列表
  const [modelsLoading, setModelsLoading] = useState(false);

  const { stockInfo, aiIncome, positions, loading, error, refetch } = useStockData(
    stockCode,
    aiCode
  );

  // 获取AI模型列表
  useEffect(() => {
    fetchAiModels();
  }, []);

  const fetchAiModels = async () => {
    setModelsLoading(true);
    try {
      const response = await fetch('/api/ai-model/list');
      const result = await response.json();
      if (result.body) {
        setAiModels(result.body);
        // 如果有模型，默认选中第一个
        if (result.body.length > 0 && !selectedModelId) {
          setSelectedModelId(result.body[0].id);
          setAiCode(result.body[0].modelName);
        }
      }
    } catch (err) {
      console.error('获取AI模型列表失败:', err);
    } finally {
      setModelsLoading(false);
    }
  };

  const handleSearch = () => {
    if (inputStockCode.trim()) {
      setStockCode(inputStockCode.trim().toUpperCase());
    }
  };

  const handleModelChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const modelId = parseInt(e.target.value);
    setSelectedModelId(modelId);
    const selectedModel = aiModels.find(m => m.id === modelId);
    if (selectedModel) {
      setAiCode(selectedModel.modelName);
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
              {/* AI模型选择下拉框 */}
              <div className="flex items-center gap-2">
                <label className="text-sm text-gray-600 dark:text-gray-400">AI模型:</label>
                <div className="relative">
                  <select
                    value={selectedModelId}
                    onChange={handleModelChange}
                    disabled={modelsLoading}
                    className="px-3 py-1.5 pr-8 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-800 dark:text-white text-sm appearance-none cursor-pointer min-w-[120px]"
                  >
                    {aiModels.map((model) => (
                      <option key={model.id} value={model.id}>
                        {model.modelName} (${model.deposit.toFixed(2)})
                      </option>
                    ))}
                  </select>
                  <ChevronDown className="absolute right-2 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500 pointer-events-none" />
                </div>
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
            modelId={selectedModelId}
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
