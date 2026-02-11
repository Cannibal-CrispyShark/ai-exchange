'use client';

import { useState, useRef, useCallback, useEffect } from 'react';
import { 
  Brain, Loader2, TrendingUp, Shield, Target, 
  AlertTriangle, ChevronDown, CheckCircle2, Sparkles, 
  Zap, BarChart3, Lightbulb, X,
  Activity, PieChart, TrendingUpIcon, DollarSign
} from 'lucide-react';
import type { AiDecisionVO } from '@/types';

interface AIDecisionPanelProps {
  aiCode?: string;
  modelId?: number;
}

const riskPreferences = [
  { code: 'conservative', name: '保守型', description: '注重本金安全', color: 'emerald', gradient: 'from-emerald-500 to-teal-600' },
  { code: 'moderate', name: '稳健型', description: '平衡风险收益', color: 'blue', gradient: 'from-blue-500 to-indigo-600' },
  { code: 'aggressive', name: '激进型', description: '追求高收益', color: 'violet', gradient: 'from-violet-500 to-purple-600' },
];

type StepStatus = 'pending' | 'active' | 'completed';

interface StreamState {
  isLoading: boolean;
  progress: number;
  currentMessage: string;
  error: string | null;
}

export default function AIDecisionPanel({ aiCode, modelId }: AIDecisionPanelProps) {
  const [decision, setDecision] = useState<AiDecisionVO | null>(null);
  const [riskPreference, setRiskPreference] = useState('moderate');
  
  const [streamState, setStreamState] = useState<StreamState>({
    isLoading: false,
    progress: 0,
    currentMessage: '',
    error: null,
  });

  const [steps, setSteps] = useState<Record<string, StepStatus>>({
    start: 'pending', model: 'pending', risk: 'pending', features: 'pending',
    positions: 'pending', prompt: 'pending', thinking: 'pending',
    analysis: 'pending', evaluation: 'pending', strategy: 'pending',
    decision: 'pending', trade: 'pending', complete: 'pending',
  });

  const [thinkingSteps, setThinkingSteps] = useState<string[]>([]);
  const [tradeResults, setTradeResults] = useState<Array<{
    action: string; stockCode: string; success: boolean; message: string;
  }>>([]);

  const loadingTimerRef = useRef<NodeJS.Timeout | null>(null);
  const progressTimerRef = useRef<NodeJS.Timeout | null>(null);

  const currentRisk = riskPreferences.find(r => r.code === riskPreference);

  // 模拟加载步骤 - 延长到90秒基础时间，给AI更多思考时间
  const loadingSteps = [
    { id: 'start', message: '初始化分析系统...', delay: 0 },
    { id: 'model', message: '获取AI模型信息...', delay: 3000 },
    { id: 'risk', message: '加载风险偏好配置...', delay: 6000 },
    { id: 'features', message: '获取股票特征数据...', delay: 10000 },
    { id: 'positions', message: '获取当前持仓数据...', delay: 14000 },
    { id: 'prompt', message: '构建投资决策提示词...', delay: 18000 },
    { id: 'thinking', message: 'AI正在深度思考...', delay: 24000 },
    { id: 'analysis', message: '分析市场趋势...', delay: 36000 },
    { id: 'evaluation', message: '评估风险收益比...', delay: 48000 },
    { id: 'strategy', message: '制定投资策略...', delay: 60000 },
    { id: 'decision', message: '生成投资决策...', delay: 72000 },
    { id: 'trade', message: '执行交易决策...', delay: 84000 },
    { id: 'complete', message: '分析完成', delay: 90000 },
  ];

  const updateStep = useCallback((stepId: string, status: StepStatus) => {
    setSteps(prev => ({ ...prev, [stepId]: status }));
  }, []);

  // 开始模拟加载动画
  const startLoadingAnimation = useCallback(() => {
    setStreamState({
      isLoading: true,
      progress: 0,
      currentMessage: '正在初始化...',
      error: null,
    });

    // 重置步骤
    setSteps({
      start: 'pending', model: 'pending', risk: 'pending', features: 'pending',
      positions: 'pending', prompt: 'pending', thinking: 'pending',
      analysis: 'pending', evaluation: 'pending', strategy: 'pending',
      decision: 'pending', trade: 'pending', complete: 'pending',
    });

    // 模拟思考过程
    setThinkingSteps([
      '正在分析市场趋势...',
      '正在评估股票基本面...',
      '正在计算技术指标...',
      '正在评估风险收益比...',
      '正在制定投资策略...',
      '正在生成交易建议...',
    ]);

    // 进度条动画 - 延长到90秒达到90%
    let progress = 0;
    progressTimerRef.current = setInterval(() => {
      progress += 1;
      if (progress <= 90) {
        setStreamState(prev => ({ ...prev, progress }));
      }
    }, 1000); // 约90秒达到90%

    // 步骤动画
    loadingSteps.forEach((step, index) => {
      loadingTimerRef.current = setTimeout(() => {
        // 将之前的步骤设为completed
        for (let i = 0; i < index; i++) {
          updateStep(loadingSteps[i].id, 'completed');
        }
        // 当前步骤设为active
        updateStep(step.id, 'active');
        setStreamState(prev => ({ ...prev, currentMessage: step.message }));
      }, step.delay);
    });
  }, [updateStep]);

  // 停止加载动画
  const stopLoadingAnimation = useCallback(() => {
    if (progressTimerRef.current) clearInterval(progressTimerRef.current);
    if (loadingTimerRef.current) clearTimeout(loadingTimerRef.current);
  }, []);

  // 调用非流式接口获取决策数据
  const fetchDecision = useCallback(async (targetModelId: number) => {
    try {
      const url = `/api/investment/${targetModelId}/decide?riskPreference=${riskPreference}`;
      
      // 设置6分钟超时（比后端5分钟稍长）
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 360000);
      
      const response = await fetch(url, { signal: controller.signal });
      clearTimeout(timeoutId);
      
      // 先获取原始文本响应
      const responseText = await response.text();
      
      // 检查响应状态
      if (!response.ok) {
        // HTTP 错误状态
        console.error('HTTP错误:', response.status, responseText);
        setStreamState(prev => ({ 
          ...prev, 
          error: `服务器错误 (${response.status}): ${responseText.substring(0, 100)}`,
          isLoading: false 
        }));
        return;
      }
      
      // 尝试解析JSON
      let result;
      try {
        result = JSON.parse(responseText);
      } catch (parseErr) {
        // 返回的不是JSON
        console.error('JSON解析失败，原始响应:', responseText);
        setStreamState(prev => ({ 
          ...prev, 
          error: `服务器返回格式错误: ${responseText.substring(0, 100)}`,
          isLoading: false 
        }));
        return;
      }
      
      // 解析后端返回的数据格式 Response<AiDecisionVO>
      if (result.body) {
        setDecision(result.body);
        
        // 如果后端返回了交易执行结果，也保存起来
        if (result.body.tradeDecisions) {
          const trades = result.body.tradeDecisions
            .filter((t: any) => t.executed !== undefined)
            .map((t: any) => ({
              action: t.action,
              stockCode: t.stockCode,
              success: t.executed,
              message: t.executionMessage || (t.executed ? '执行成功' : '未执行'),
            }));
          setTradeResults(trades);
        }
      } else {
        setStreamState(prev => ({ 
          ...prev, 
          error: result.message || '获取决策失败',
          isLoading: false 
        }));
      }
    } catch (err: any) {
      console.error('获取决策失败:', err);
      // 更详细的错误信息
      let errorMessage = '网络请求失败，请稍后重试';
      if (err.name === 'AbortError') {
        errorMessage = '请求超时（6分钟），AI思考时间过长，请稍后重试';
      } else if (err.message) {
        errorMessage = `请求失败: ${err.message}`;
      }
      setStreamState(prev => ({ 
        ...prev, 
        error: errorMessage,
        isLoading: false 
      }));
    }
  }, [riskPreference]);

  // 快速完成加载动画
  const completeLoadingAnimation = useCallback(() => {
    // 快速完成剩余进度
    setStreamState(prev => ({ 
      ...prev, 
      progress: 100,
      currentMessage: '分析完成'
    }));
    // 所有步骤设为完成
    loadingSteps.forEach(step => updateStep(step.id, 'completed'));
  }, [updateStep]);

  const handleMakeDecision = useCallback(async () => {
    if (!modelId) {
      setStreamState(prev => ({ ...prev, error: '请先选择AI模型' }));
      return;
    }

    // 重置状态
    setDecision(null);
    setTradeResults([]);
    
    // 开始加载动画
    startLoadingAnimation();
    
    // 调用非流式接口获取数据
    await fetchDecision(modelId);
    
    // 数据到达后，快速完成加载动画
    completeLoadingAnimation();
    
    // 短暂延迟后停止加载并显示结果
    setTimeout(() => {
      stopLoadingAnimation();
      setStreamState(prev => ({ 
        ...prev, 
        isLoading: false
      }));
    }, 500);
  }, [modelId, startLoadingAnimation, fetchDecision, stopLoadingAnimation, completeLoadingAnimation]);

  const handleCancel = useCallback(() => {
    stopLoadingAnimation();
    setStreamState(prev => ({ 
      ...prev, 
      isLoading: false,
      currentMessage: '已取消'
    }));
  }, [stopLoadingAnimation]);

  useEffect(() => {
    return () => {
      stopLoadingAnimation();
    };
  }, [stopLoadingAnimation]);

  const stepConfig = [
    { id: 'start', label: '初始化', icon: Sparkles },
    { id: 'model', label: '获取模型', icon: Brain },
    { id: 'risk', label: '风险偏好', icon: Shield },
    { id: 'features', label: '股票特征', icon: BarChart3 },
    { id: 'positions', label: '持仓数据', icon: PieChart },
    { id: 'prompt', label: '构建提示词', icon: Zap },
    { id: 'thinking', label: 'AI思考', icon: Lightbulb },
    { id: 'analysis', label: '市场分析', icon: TrendingUp },
    { id: 'evaluation', label: '风险评估', icon: Shield },
    { id: 'strategy', label: '策略制定', icon: Target },
    { id: 'decision', label: '生成决策', icon: Activity },
    { id: 'trade', label: '执行交易', icon: TrendingUpIcon },
    { id: 'complete', label: '完成', icon: CheckCircle2 },
  ];

  const renderStepIcon = (status: StepStatus, Icon: any) => {
    if (status === 'completed') return <CheckCircle2 className="w-4 h-4" />;
    if (status === 'active') return <Loader2 className="w-4 h-4 animate-spin" />;
    return <Icon className="w-4 h-4 opacity-50" />;
  };

  return (
    <div className="w-full bg-slate-50 dark:bg-slate-900 rounded-2xl shadow-xl overflow-hidden">
      {/* 渐变头部 */}
      <div className={`bg-gradient-to-r ${currentRisk?.gradient || 'from-slate-600 to-slate-700'} p-6 text-white`}>
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            <div className="p-3 bg-white/20 backdrop-blur rounded-xl">
              <Brain className="w-7 h-7" />
            </div>
            <div>
              <h2 className="text-2xl font-bold">AI投资决策</h2>
              {aiCode && <p className="text-white/80 text-sm">模型: {aiCode}</p>}
            </div>
          </div>

          <div className="flex items-center gap-3">
            {streamState.isLoading && (
              <div className="flex items-center gap-2 px-4 py-2 rounded-full text-sm font-medium backdrop-blur bg-white/20 text-white">
                <Loader2 className="w-4 h-4 animate-spin" />
                分析中
              </div>
            )}

            <div className="relative">
              <select
                value={riskPreference}
                onChange={(e) => setRiskPreference(e.target.value)}
                disabled={streamState.isLoading}
                className="px-4 py-2 pr-10 bg-white/20 backdrop-blur border border-white/30 rounded-xl text-white text-sm appearance-none cursor-pointer disabled:opacity-50 focus:outline-none focus:ring-2 focus:ring-white/50"
              >
                {riskPreferences.map(r => (
                  <option key={r.code} value={r.code} className="text-slate-800">{r.name}</option>
                ))}
              </select>
              <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-white/70 pointer-events-none" />
            </div>

            {streamState.isLoading ? (
              <button onClick={handleCancel} className="px-4 py-2 bg-rose-500 hover:bg-rose-600 text-white rounded-xl flex items-center gap-2 text-sm font-medium transition-colors">
                <X className="w-4 h-4" /> 取消
              </button>
            ) : (
              <button 
                onClick={handleMakeDecision} 
                disabled={!modelId}
                className="px-5 py-2 bg-white text-slate-800 hover:bg-slate-100 disabled:opacity-50 disabled:cursor-not-allowed rounded-xl flex items-center gap-2 text-sm font-semibold transition-colors shadow-lg"
              >
                <Brain className="w-4 h-4" /> 生成决策
              </button>
            )}
          </div>
        </div>
      </div>

      <div className="p-6">
        {/* 错误提示 */}
        {streamState.error && (
          <div className="mb-6 p-4 bg-rose-50 border border-rose-200 rounded-xl flex items-center gap-3 text-rose-700">
            <AlertTriangle className="w-5 h-5 flex-shrink-0" />
            <span className="font-medium">{streamState.error}</span>
          </div>
        )}

        {/* 加载状态 - 独立的加载动画 */}
        {streamState.isLoading && (
          <div className="mb-6 space-y-6">
            {/* 进度卡片 */}
            <div className="bg-white dark:bg-slate-800 rounded-xl p-5 shadow-sm border border-slate-200 dark:border-slate-700">
              <div className="flex items-center justify-between mb-3">
                <div className="flex items-center gap-3">
                  <div className={`p-2 rounded-lg bg-gradient-to-r ${currentRisk?.gradient || 'from-slate-500 to-slate-600'}`}>
                    <Activity className="w-5 h-5 text-white" />
                  </div>
                  <span className="font-semibold text-slate-700 dark:text-slate-200">{streamState.currentMessage}</span>
                </div>
                <span className={`text-2xl font-bold bg-gradient-to-r ${currentRisk?.gradient || 'from-slate-600 to-slate-700'} bg-clip-text text-transparent`}>
                  {streamState.progress}%
                </span>
              </div>
              <div className="w-full bg-slate-200 dark:bg-slate-700 rounded-full h-3 overflow-hidden">
                <div 
                  className={`h-full rounded-full bg-gradient-to-r ${currentRisk?.gradient || 'from-slate-500 to-slate-600'} transition-all duration-100`}
                  style={{ width: `${streamState.progress}%` }}
                />
              </div>
            </div>

            {/* 步骤网格 */}
            <div className="grid grid-cols-5 gap-3">
              {stepConfig.map(step => {
                const status = steps[step.id];
                const isCompleted = status === 'completed';
                const isActive = status === 'active';
                return (
                  <div key={step.id} className={`p-3 rounded-xl border-2 transition-all duration-300 ${
                    isCompleted ? 'bg-emerald-50 border-emerald-200 dark:bg-emerald-900/20 dark:border-emerald-800' :
                    isActive ? `bg-${currentRisk?.color}-50 border-${currentRisk?.color}-300 dark:bg-${currentRisk?.color}-900/20 shadow-md` :
                    'bg-white border-slate-200 dark:bg-slate-800 dark:border-slate-700'
                  }`}>
                    <div className={`flex items-center gap-2 ${
                      isCompleted ? 'text-emerald-600' :
                      isActive ? `text-${currentRisk?.color}-600` :
                      'text-slate-400'
                    }`}>
                      {renderStepIcon(status, step.icon)}
                      <span className="text-xs font-semibold truncate">{step.label}</span>
                    </div>
                  </div>
                );
              })}
            </div>

            {/* AI思考过程 */}
            <div className="bg-amber-50 dark:bg-amber-900/20 rounded-xl p-5 border border-amber-200 dark:border-amber-800">
              <h4 className="text-sm font-bold text-amber-800 dark:text-amber-300 mb-3 flex items-center gap-2">
                <Lightbulb className="w-4 h-4" /> AI思考过程
              </h4>
              <div className="space-y-2">
                {thinkingSteps.map((step, idx) => (
                  <div key={idx} className="flex items-start gap-3 text-sm text-amber-700 dark:text-amber-400">
                    <span className="w-6 h-6 flex items-center justify-center bg-amber-200 dark:bg-amber-800 rounded-full text-xs font-bold flex-shrink-0">{idx + 1}</span>
                    <span className="leading-relaxed">{step}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* 决策结果 */}
        {decision && !streamState.isLoading && (
          <div className="space-y-6">
            {/* 决策总结 */}
            <div className={`bg-gradient-to-r ${currentRisk?.gradient || 'from-slate-600 to-slate-700'} rounded-xl p-6 text-white`}>
              <h3 className="text-lg font-bold mb-3 flex items-center gap-2">
                <Sparkles className="w-5 h-5" /> 决策总结
              </h3>
              <p className="text-white/90 leading-relaxed">{decision.summary}</p>
            </div>

            {/* 交易决策 */}
            {decision.tradeDecisions && decision.tradeDecisions.length > 0 && (
              <div>
                <h3 className="text-lg font-bold text-slate-800 dark:text-slate-200 mb-4 flex items-center gap-2">
                  <Target className="w-5 h-5 text-slate-600" /> 交易决策
                </h3>
                <div className="space-y-4">
                  {decision.tradeDecisions.map((trade, idx) => (
                    <div key={idx} className={`rounded-xl p-5 border-l-4 shadow-sm ${
                      trade.action === 'BUY' ? 'bg-emerald-50 border-emerald-500 dark:bg-emerald-900/20' :
                      trade.action === 'SELL' ? 'bg-rose-50 border-rose-500 dark:bg-rose-900/20' :
                      'bg-blue-50 border-blue-500 dark:bg-blue-900/20'
                    }`}>
                      <div className="flex items-center justify-between mb-3">
                        <div className="flex items-center gap-3">
                          <span className={`px-3 py-1 rounded-full text-sm font-bold ${
                            trade.action === 'BUY' ? 'bg-emerald-500 text-white' :
                            trade.action === 'SELL' ? 'bg-rose-500 text-white' :
                            'bg-blue-500 text-white'
                          }`}>
                            {trade.action === 'BUY' ? '买入' : trade.action === 'SELL' ? '卖出' : '持有'}
                          </span>
                          <span className="text-lg font-bold text-slate-800 dark:text-slate-200">{trade.stockName}</span>
                          <span className="text-slate-500 font-mono">{trade.stockCode}</span>
                        </div>
                        {trade.executed !== undefined && (
                          <span className={`flex items-center gap-1 font-semibold ${trade.executed ? 'text-emerald-600' : 'text-rose-600'}`}>
                            {trade.executed ? <CheckCircle2 className="w-5 h-5" /> : <AlertTriangle className="w-5 h-5" />}
                            {trade.executed ? '已执行' : '未执行'}
                          </span>
                        )}
                      </div>
                      <p className="text-slate-600 dark:text-slate-400 leading-relaxed">{trade.reason}</p>
                      {trade.executionMessage && (
                        <p className="text-sm text-slate-500 mt-2 italic">{trade.executionMessage}</p>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* 交易执行结果 */}
            {tradeResults.length > 0 && (
              <div className="bg-white dark:bg-slate-800 rounded-xl p-5 border border-slate-200 dark:border-slate-700">
                <h4 className="text-sm font-bold text-slate-700 dark:text-slate-300 mb-3 flex items-center gap-2">
                  <DollarSign className="w-4 h-4" /> 交易执行结果
                </h4>
                <div className="space-y-2">
                  {tradeResults.map((result, idx) => (
                    <div key={idx} className={`flex items-center gap-3 p-3 rounded-lg ${
                      result.success ? 'bg-emerald-50 text-emerald-700' : 'bg-rose-50 text-rose-700'
                    }`}>
                      {result.success ? <CheckCircle2 className="w-5 h-5" /> : <AlertTriangle className="w-5 h-5" />}
                      <div>
                        <span className="font-semibold">{result.action}</span>
                        <span className="text-slate-500 mx-2">{result.stockCode}</span>
                        <span className="text-sm">{result.message}</span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* 市场分析 */}
            {decision.marketAnalysis && (
              <div>
                <h3 className="text-lg font-bold text-slate-800 dark:text-slate-200 mb-4 flex items-center gap-2">
                  <BarChart3 className="w-5 h-5 text-slate-600" /> 市场分析
                </h3>
                <div className="bg-white dark:bg-slate-800 rounded-xl p-5 shadow-sm border border-slate-200 dark:border-slate-700">
                  <p className="text-slate-700 dark:text-slate-300 mb-4 flex items-center gap-2">
                    <TrendingUp className="w-5 h-5 text-blue-500" />
                    <strong>整体趋势：</strong>{decision.marketAnalysis.overallTrend}
                  </p>
                  {decision.marketAnalysis.stockAnalyses?.map((analysis, idx) => (
                    <div key={idx} className="p-4 bg-slate-50 dark:bg-slate-700/50 rounded-lg mb-3">
                      <div className="flex items-center justify-between mb-2">
                        <span className="font-bold text-slate-800 dark:text-slate-200">{analysis.stockName}</span>
                        <span className={`px-3 py-1 rounded-full text-sm font-medium ${
                          analysis.trend?.includes('上') ? 'bg-emerald-100 text-emerald-700' :
                          analysis.trend?.includes('下') ? 'bg-rose-100 text-rose-700' :
                          'bg-slate-100 text-slate-700'
                        }`}>{analysis.trend}</span>
                      </div>
                      <p className="text-sm text-slate-600 dark:text-slate-400">{analysis.technicalAnalysis}</p>
                      <div className="flex gap-6 mt-3 text-sm">
                        <span className="text-slate-500">支撑: <span className="text-emerald-600 font-semibold">{analysis.supportLevel}</span></span>
                        <span className="text-slate-500">阻力: <span className="text-rose-600 font-semibold">{analysis.resistanceLevel}</span></span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* 风险评估 */}
            {decision.riskAssessment && (
              <div>
                <h3 className="text-lg font-bold text-slate-800 dark:text-slate-200 mb-4 flex items-center gap-2">
                  <Shield className="w-5 h-5 text-slate-600" /> 风险评估
                </h3>
                <div className={`rounded-xl p-5 border-l-4 ${
                  decision.riskAssessment.riskLevel === 'LOW' ? 'bg-emerald-50 border-emerald-500' :
                  decision.riskAssessment.riskLevel === 'HIGH' ? 'bg-rose-50 border-rose-500' :
                  'bg-amber-50 border-amber-500'
                }`}>
                  <div className="flex items-center gap-3 mb-3">
                    <Shield className={`w-6 h-6 ${
                      decision.riskAssessment.riskLevel === 'LOW' ? 'text-emerald-600' :
                      decision.riskAssessment.riskLevel === 'HIGH' ? 'text-rose-600' :
                      'text-amber-600'
                    }`} />
                    <span className="text-xl font-bold text-slate-800">风险等级: {decision.riskAssessment.riskLevel}</span>
                  </div>
                  <p className="text-slate-700 leading-relaxed">{decision.riskAssessment.riskDescription}</p>
                </div>
              </div>
            )}
          </div>
        )}

        {/* 空状态 */}
        {!decision && !streamState.isLoading && !streamState.error && (
          <div className="text-center py-16">
            <div className={`w-24 h-24 mx-auto mb-6 rounded-full bg-gradient-to-r ${currentRisk?.gradient || 'from-slate-400 to-slate-600'} flex items-center justify-center shadow-lg`}>
              <Brain className="w-12 h-12 text-white" />
            </div>
            <h3 className="text-xl font-bold text-slate-700 dark:text-slate-300 mb-2">开始AI投资决策</h3>
            <p className="text-slate-500">选择风险偏好并点击"生成决策"按钮，让AI为您分析市场</p>
          </div>
        )}
      </div>
    </div>
  );
}
