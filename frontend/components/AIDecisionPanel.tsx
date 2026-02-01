'use client';

import { useState } from 'react';
import { Brain, Loader2, TrendingUp, TrendingDown, Shield, Target, AlertTriangle } from 'lucide-react';
import type { AiDecisionVO } from '@/types';

interface AIDecisionPanelProps {
  aiCode?: string;
  modelId?: number;
}

export default function AIDecisionPanel({ aiCode, modelId }: AIDecisionPanelProps) {
  const [decision, setDecision] = useState<AiDecisionVO | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleMakeDecision = async () => {
    if (!modelId) {
      setError('请先选择AI模型');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const response = await fetch(`/api/investment/${modelId}/decide`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error('决策请求失败');
      }

      const result = await response.json();
      // 后端返回格式: { body: {...}, message: "success" }
      if (result.body) {
        setDecision(result.body);
      } else {
        setError(result.message || '决策失败');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : '决策过程出错');
    } finally {
      setLoading(false);
    }
  };

  const getRiskLevelColor = (level?: string) => {
    switch (level?.toUpperCase()) {
      case 'LOW':
        return 'text-green-600 bg-green-50 dark:bg-green-900/20';
      case 'MEDIUM':
        return 'text-yellow-600 bg-yellow-50 dark:bg-yellow-900/20';
      case 'HIGH':
        return 'text-red-600 bg-red-50 dark:bg-red-900/20';
      default:
        return 'text-gray-600 bg-gray-50 dark:bg-gray-800';
    }
  };

  const getActionColor = (action?: string) => {
    switch (action?.toUpperCase()) {
      case 'BUY':
        return 'text-green-600 bg-green-50 dark:bg-green-900/20 border-green-200';
      case 'SELL':
        return 'text-red-600 bg-red-50 dark:bg-red-900/20 border-red-200';
      case 'HOLD':
        return 'text-blue-600 bg-blue-50 dark:bg-blue-900/20 border-blue-200';
      default:
        return 'text-gray-600 bg-gray-50 dark:bg-gray-800 border-gray-200';
    }
  };

  const getActionText = (action?: string) => {
    switch (action?.toUpperCase()) {
      case 'BUY':
        return '买入';
      case 'SELL':
        return '卖出';
      case 'HOLD':
        return '持有';
      default:
        return action || '未知';
    }
  };

  return (
    <div className="w-full bg-white dark:bg-gray-900 rounded-lg shadow-lg p-6">
      {/* 标题栏 */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-purple-100 dark:bg-purple-900/30 rounded-lg">
            <Brain className="w-6 h-6 text-purple-600 dark:text-purple-400" />
          </div>
          <div>
            <h2 className="text-xl font-bold text-gray-900 dark:text-white">
              AI投资决策
            </h2>
            {aiCode && (
              <p className="text-sm text-gray-500 dark:text-gray-400">模型: {aiCode}</p>
            )}
          </div>
        </div>
        <button
          onClick={handleMakeDecision}
          disabled={loading || !modelId}
          className="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {loading ? (
            <>
              <Loader2 className="w-4 h-4 animate-spin" />
              决策中...
            </>
          ) : (
            <>
              <Brain className="w-4 h-4" />
              生成决策
            </>
          )}
        </button>
      </div>

      {/* 错误提示 */}
      {error && (
        <div className="mb-4 p-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
          <div className="flex items-center gap-2">
            <AlertTriangle className="w-5 h-5 text-red-600" />
            <p className="text-red-800 dark:text-red-200">{error}</p>
          </div>
        </div>
      )}

      {/* 决策结果展示 */}
      {decision && (
        <div className="space-y-6">
          {/* 决策总结 */}
          <div className="p-4 bg-gradient-to-r from-purple-50 to-indigo-50 dark:from-purple-900/20 dark:to-indigo-900/20 rounded-lg">
            <h3 className="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-2">
              决策总结
            </h3>
            <p className="text-gray-900 dark:text-white">{decision.summary}</p>
          </div>

          {/* 交易决策 */}
          {decision.tradeDecisions && decision.tradeDecisions.length > 0 && (
            <div>
              <h3 className="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-3 flex items-center gap-2">
                <Target className="w-4 h-4" />
                交易决策
              </h3>
              <div className="space-y-3">
                {decision.tradeDecisions.map((trade, index) => (
                  <div
                    key={index}
                    className={`p-4 rounded-lg border-2 ${getActionColor(trade.action)}`}
                  >
                    <div className="flex items-center justify-between mb-2">
                      <div className="flex items-center gap-3">
                        <span className="px-3 py-1 rounded-full text-sm font-semibold bg-white dark:bg-gray-800">
                          {getActionText(trade.action)}
                        </span>
                        <span className="font-semibold text-gray-900 dark:text-white">
                          {trade.stockName} ({trade.stockCode})
                        </span>
                      </div>
                      {trade.action?.toUpperCase() === 'BUY' ? (
                        <TrendingUp className="w-5 h-5 text-green-600" />
                      ) : trade.action?.toUpperCase() === 'SELL' ? (
                        <TrendingDown className="w-5 h-5 text-red-600" />
                      ) : null}
                    </div>
                    <div className="grid grid-cols-2 gap-4 mb-2">
                      <div>
                        <p className="text-xs text-gray-500 dark:text-gray-400">交易数量</p>
                        <p className="text-lg font-semibold">{trade.amount} 股</p>
                      </div>
                      <div>
                        <p className="text-xs text-gray-500 dark:text-gray-400">执行状态</p>
                        <p className={`text-sm font-semibold ${trade.executed ? 'text-green-600' : 'text-red-600'}`}>
                          {trade.executed ? '✓ 已执行' : '✗ 未执行'}
                        </p>
                      </div>
                    </div>
                    <p className="text-sm text-gray-700 dark:text-gray-300">
                      <span className="font-semibold">决策原因：</span>{trade.reason}
                    </p>
                    {trade.executionMessage && (
                      <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                        {trade.executionMessage}
                      </p>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* 市场分析 */}
          {decision.marketAnalysis && (
            <div>
              <h3 className="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-3 flex items-center gap-2">
                <TrendingUp className="w-4 h-4" />
                市场分析
              </h3>
              <div className="p-4 bg-gray-50 dark:bg-gray-800 rounded-lg">
                <p className="text-sm text-gray-700 dark:text-gray-300 mb-3">
                  <span className="font-semibold">整体趋势：</span>
                  {decision.marketAnalysis.overallTrend}
                </p>
                {decision.marketAnalysis.stockAnalyses && decision.marketAnalysis.stockAnalyses.length > 0 && (
                  <div className="space-y-3">
                    {decision.marketAnalysis.stockAnalyses.map((analysis, index) => (
                      <div key={index} className="p-3 bg-white dark:bg-gray-700 rounded-lg">
                        <div className="flex items-center justify-between mb-2">
                          <span className="font-semibold text-gray-900 dark:text-white">
                            {analysis.stockName} ({analysis.stockCode})
                          </span>
                          <span className="text-sm text-gray-600 dark:text-gray-400">
                            {analysis.trend}
                          </span>
                        </div>
                        <p className="text-sm text-gray-600 dark:text-gray-400 mb-1">
                          {analysis.technicalAnalysis}
                        </p>
                        <div className="flex gap-4 text-xs text-gray-500 dark:text-gray-400">
                          <span>支撑位: {analysis.supportLevel}</span>
                          <span>阻力位: {analysis.resistanceLevel}</span>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          )}

          {/* 风险评估 */}
          {decision.riskAssessment && (
            <div>
              <h3 className="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-3 flex items-center gap-2">
                <Shield className="w-4 h-4" />
                风险评估
              </h3>
              <div className={`p-4 rounded-lg ${getRiskLevelColor(decision.riskAssessment.riskLevel)}`}>
                <div className="flex items-center justify-between mb-3">
                  <span className="font-semibold">风险等级</span>
                  <span className="px-3 py-1 rounded-full text-sm font-semibold bg-white dark:bg-gray-800">
                    {decision.riskAssessment.riskLevel}
                  </span>
                </div>
                <div className="space-y-2 text-sm">
                  <p>
                    <span className="font-semibold">风险说明：</span>
                    {decision.riskAssessment.riskDescription}
                  </p>
                  <p>
                    <span className="font-semibold">风控措施：</span>
                    {decision.riskAssessment.riskControlMeasures}
                  </p>
                  <p>
                    <span className="font-semibold">预期收益：</span>
                    {decision.riskAssessment.expectedReturn}
                  </p>
                  <p>
                    <span className="font-semibold">最大可承受损失：</span>
                    {decision.riskAssessment.maxAcceptableLoss}
                  </p>
                </div>
              </div>
            </div>
          )}
        </div>
      )}

      {/* 空状态 */}
      {!decision && !loading && !error && (
        <div className="text-center py-12 text-gray-500 dark:text-gray-400">
          <Brain className="w-16 h-16 mx-auto mb-4 opacity-50" />
          <p>点击"生成决策"按钮，让AI为您分析并做出投资决策</p>
        </div>
      )}
    </div>
  );
}
