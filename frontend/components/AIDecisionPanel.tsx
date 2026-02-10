'use client';

import { useState, useRef, useCallback, useEffect } from 'react';
import { Brain, Loader2, TrendingUp, TrendingDown, Shield, Target, AlertTriangle, ChevronDown, CheckCircle2, Circle, Wifi, WifiOff, Sparkles, Zap, BarChart3, Lightbulb } from 'lucide-react';
import type { AiDecisionVO } from '@/types';

interface AIDecisionPanelProps {
  aiCode?: string;
  modelId?: number;
}

// 风险偏好选项
const riskPreferences = [
  { code: 'conservative', name: '保守型', description: '注重本金安全', color: 'green' },
  { code: 'moderate', name: '稳健型', description: '平衡风险收益', color: 'blue' },
  { code: 'aggressive', name: '激进型', description: '追求高收益', color: 'purple' },
];

// 进度步骤
interface ProgressStep {
  id: string;
  message: string;
  status: 'pending' | 'active' | 'completed';
  icon: React.ReactNode;
}

// 思考步骤
interface ThinkingStep {
  id: number;
  message: string;
  timestamp: number;
}

export default function AIDecisionPanel({ aiCode, modelId }: AIDecisionPanelProps) {
  const [decision, setDecision] = useState<AiDecisionVO | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [riskPreference, setRiskPreference] = useState('moderate');
  
  // 流式状态
  const [progress, setProgress] = useState(0);
  const [currentMessage, setCurrentMessage] = useState('');
  const [thinkingSteps, setThinkingSteps] = useState<ThinkingStep[]>([]);
  const [progressSteps, setProgressSteps] = useState<ProgressStep[]>([
    { id: 'start', message: '初始化分析', status: 'pending', icon: <Sparkles className="w-4 h-4" /> },
    { id: 'model', message: '获取模型信息', status: 'pending', icon: <Brain className="w-4 h-4" /> },
    { id: 'risk', message: '加载风险偏好', status: 'pending', icon: <Shield className="w-4 h-4" /> },
    { id: 'features', message: '获取股票特征', status: 'pending', icon: <BarChart3 className="w-4 h-4" /> },
    { id: 'positions', message: '获取持仓数据', status: 'pending', icon: <Target className="w-4 h-4" /> },
    { id: 'prompt', message: '构建提示词', status: 'pending', icon: <Zap className="w-4 h-4" /> },
    { id: 'thinking', message: 'AI深度思考', status: 'pending', icon: <Lightbulb className="w-4 h-4" /> },
    { id: 'decision', message: '生成决策', status: 'pending', icon: <Brain className="w-4 h-4" /> },
    { id: 'trade', message: '执行交易', status: 'pending', icon: <TrendingUp className="w-4 h-4" /> },
    { id: 'complete', message: '分析完成', status: 'pending', icon: <CheckCircle2 className="w-4 h-4" /> },
  ]);
  const [tradeResults, setTradeResults] = useState<Array<{action: string, stockCode: string, success: boolean, message: string}>>([]);
  
  // 连接状态
  const [isConnected, setIsConnected] = useState(false);
  const [lastHeartbeat, setLastHeartbeat] = useState<number>(0);
  const [reconnectCount, setReconnectCount] = useState(0);
  const [logs, setLogs] = useState<Array<{time: string, type: string, message: string}>>([]);
  
  const eventSourceRef = useRef<EventSource | null>(null);
  const heartbeatTimerRef = useRef<NodeJS.Timeout | null>(null);
  const reconnectTimerRef = useRef<NodeJS.Timeout | null>(null);
  const maxReconnectAttempts = 3;
  const thinkingIdRef = useRef(0);

  // 添加日志
  const addLog = useCallback((type: string, message: string) => {
    const time = new Date().toLocaleTimeString('zh-CN', { hour12: false });
    setLogs(prev => [...prev.slice(-20), { time, type, message }]);
  }, []);

  const updateStepStatus = useCallback((stepId: string, status: 'pending' | 'active' | 'completed') => {
    setProgressSteps(prev => prev.map(step => 
      step.id === stepId ? { ...step, status } : step
    ));
  }, []);

  // 检查心跳超时
  const checkHeartbeat = useCallback(() => {
    const now = Date.now();
    if (lastHeartbeat > 0 && now - lastHeartbeat > 45000) {
      console.warn('心跳超时，连接可能已断开');
      addLog('warning', '心跳超时，尝试重连...');
      setIsConnected(false);
      if (reconnectCount < maxReconnectAttempts && loading) {
        attemptReconnect();
      }
    }
  }, [lastHeartbeat, reconnectCount, loading, addLog]);

  // 尝试重连
  const attemptReconnect = useCallback(() => {
    if (reconnectCount >= maxReconnectAttempts) {
      setError('连接已断开，请重试');
      setLoading(false);
      addLog('error', '重连次数超限，连接失败');
      return;
    }

    setReconnectCount(prev => prev + 1);
    setCurrentMessage(`正在重连... (${reconnectCount + 1}/${maxReconnectAttempts})`);
    addLog('info', `第 ${reconnectCount + 1} 次重连...`);
    
    reconnectTimerRef.current = setTimeout(() => {
      if (modelId) {
        connectEventSource(modelId);
      }
    }, 2000);
  }, [reconnectCount, modelId, addLog]);

  // 建立EventSource连接
  const connectEventSource = useCallback((targetModelId: number) => {
    if (eventSourceRef.current) {
      eventSourceRef.current.close();
    }

    const eventSource = new EventSource(`/api/investment/${targetModelId}/decide-stream?riskPreference=${riskPreference}`);
    eventSourceRef.current = eventSource;
    addLog('info', '正在建立SSE连接...');

    // connected 事件 - 连接成功
    eventSource.addEventListener('connected', (e) => {
      try {
        const data = JSON.parse((e as MessageEvent).data);
        console.log('✅ connected事件:', data);
        addLog('success', data.message || 'SSE连接成功');
        setIsConnected(true);
        setLastHeartbeat(Date.now());
        setReconnectCount(0);
        
        if (heartbeatTimerRef.current) {
          clearInterval(heartbeatTimerRef.current);
        }
        heartbeatTimerRef.current = setInterval(checkHeartbeat, 10000);
      } catch (err) {
        console.error('解析connected事件失败:', err);
      }
    });

    // heartbeat 事件 - 心跳
    eventSource.addEventListener('heartbeat', (e) => {
      try {
        const data = JSON.parse((e as MessageEvent).data);
        console.log('💓 heartbeat事件:', data);
        setLastHeartbeat(Date.now());
        setIsConnected(true);
      } catch (err) {
        console.error('解析heartbeat事件失败:', err);
      }
    });

    // start 事件 - 开始分析
    eventSource.addEventListener('start', (e) => {
      try {
        const data = JSON.parse((e as MessageEvent).data);
        console.log('🚀 start事件:', data);
        addLog('info', data.message || '开始分析');
        updateStepStatus('start', 'completed');
        updateStepStatus('model', 'active');
        setProgress(5);
      } catch (err) {
        console.error('解析start事件失败:', err);
      }
    });

    // progress 事件 - 进度更新
    eventSource.addEventListener('progress', (e) => {
      try {
        const data = JSON.parse((e as MessageEvent).data);
        console.log('📊 progress事件:', data.progress, data.message);
        setProgress(data.progress || 0);
        setCurrentMessage(data.message || '');
        
        // 根据进度更新步骤状态
        if (data.progress >= 10) {
          updateStepStatus('model', 'completed');
          updateStepStatus('risk', 'active');
        }
        if (data.progress >= 20) {
          updateStepStatus('risk', 'completed');
          updateStepStatus('features', 'active');
        }
        if (data.progress >= 30) {
          updateStepStatus('features', 'completed');
          updateStepStatus('positions', 'active');
        }
        if (data.progress >= 40) {
          updateStepStatus('positions', 'completed');
          updateStepStatus('prompt', 'active');
        }
        if (data.progress >= 50) {
          updateStepStatus('prompt', 'completed');
          updateStepStatus('thinking', 'active');
        }
        if (data.progress >= 70) {
          updateStepStatus('thinking', 'completed');
        }
        if (data.progress >= 80) {
          updateStepStatus('decision', 'active');
        }
        if (data.progress >= 90) {
          updateStepStatus('decision', 'completed');
          updateStepStatus('trade', 'active');
        }
      } catch (err) {
        console.error('解析progress事件失败:', err);
      }
    });

    // thinking 事件 - AI思考过程
    eventSource.addEventListener('thinking', (e) => {
      try {
        const data = JSON.parse((e as MessageEvent).data);
        console.log('💡 thinking事件:', data.message);
        if (data.message) {
          thinkingIdRef.current += 1;
          setThinkingSteps(prev => [...prev, { 
            id: thinkingIdRef.current, 
            message: data.message,
            timestamp: Date.now()
          }]);
        }
      } catch (err) {
        console.error('解析thinking事件失败:', err);
      }
    });

    // decision 事件 - 决策结果
    eventSource.addEventListener('decision', (e) => {
      try {
        const data = JSON.parse((e as MessageEvent).data);
        console.log('🎯 decision事件:', data);
        addLog('success', '收到投资决策结果');
        setDecision(data);
        updateStepStatus('trade', 'active');
        setProgress(95);
      } catch (err) {
        console.error('解析decision事件失败:', err);
        addLog('error', '解析决策结果失败');
      }
    });

    // trade 事件 - 交易执行结果
    eventSource.addEventListener('trade', (e) => {
      try {
        const data = JSON.parse((e as MessageEvent).data);
        console.log('💰 trade事件:', data);
        addLog('info', `${data.action} ${data.stockCode}: ${data.message}`);
        setTradeResults(prev => [...prev, data]);
      } catch (err) {
        console.error('解析trade事件失败:', err);
      }
    });

    // complete 事件 - 完成
    eventSource.addEventListener('complete', (e) => {
      try {
        const data = JSON.parse((e as MessageEvent).data);
        console.log('✨ complete事件:', data);
        addLog('success', data.message || '分析完成');
        updateStepStatus('trade', 'completed');
        updateStepStatus('complete', 'completed');
        setLoading(false);
        setProgress(100);
        setIsConnected(false);
        
        if (heartbeatTimerRef.current) {
          clearInterval(heartbeatTimerRef.current);
          heartbeatTimerRef.current = null;
        }
        
        eventSource.close();
      } catch (err) {
        console.error('解析complete事件失败:', err);
      }
    });

    // error 事件 - 错误
    eventSource.addEventListener('error', (e) => {
      try {
        const data = JSON.parse((e as MessageEvent).data);
        console.error('❌ error事件:', data);
        addLog('error', data.message || '分析过程出错');
        setError(data.message || '决策过程出错');
        setLoading(false);
        setIsConnected(false);
      } catch (err) {
        console.error('SSE连接错误:', err);
        addLog('error', '连接异常');
        setIsConnected(false);
        if (loading && reconnectCount < maxReconnectAttempts) {
          attemptReconnect();
        } else if (loading) {
          setError('连接已断开，请重试');
          setLoading(false);
        }
      }
    });

    // onerror 处理连接错误
    eventSource.onerror = (err) => {
      console.error('SSE连接错误:', err);
      addLog('error', 'SSE连接异常');
      setIsConnected(false);
      
      if (loading && reconnectCount < maxReconnectAttempts) {
        attemptReconnect();
      } else if (loading) {
        setError('连接已断开，请重试');
        setLoading(false);
      }
    };

    return eventSource;
  }, [riskPreference, updateStepStatus, checkHeartbeat, attemptReconnect, reconnectCount, loading, addLog]);

  const handleMakeDecision = async () => {
    if (!modelId) {
      setError('请先选择AI模型');
      return;
    }

    // 重置状态
    setLoading(true);
    setError(null);
    setDecision(null);
    setProgress(0);
    setCurrentMessage('');
    setThinkingSteps([]);
    setTradeResults([]);
    setReconnectCount(0);
    setIsConnected(false);
    setLastHeartbeat(0);
    setLogs([]);
    thinkingIdRef.current = 0;
    
    setProgressSteps([
      { id: 'start', message: '初始化分析', status: 'pending', icon: <Sparkles className="w-4 h-4" /> },
      { id: 'model', message: '获取模型信息', status: 'pending', icon: <Brain className="w-4 h-4" /> },
      { id: 'risk', message: '加载风险偏好', status: 'pending', icon: <Shield className="w-4 h-4" /> },
      { id: 'features', message: '获取股票特征', status: 'pending', icon: <BarChart3 className="w-4 h-4" /> },
      { id: 'positions', message: '获取持仓数据', status: 'pending', icon: <Target className="w-4 h-4" /> },
      { id: 'prompt', message: '构建提示词', status: 'pending', icon: <Zap className="w-4 h-4" /> },
      { id: 'thinking', message: 'AI深度思考', status: 'pending', icon: <Lightbulb className="w-4 h-4" /> },
      { id: 'decision', message: '生成决策', status: 'pending', icon: <Brain className="w-4 h-4" /> },
      { id: 'trade', message: '执行交易', status: 'pending', icon: <TrendingUp className="w-4 h-4" /> },
      { id: 'complete', message: '分析完成', status: 'pending', icon: <CheckCircle2 className="w-4 h-4" /> },
    ]);

    connectEventSource(modelId);
  };

  const handleCancel = () => {
    if (heartbeatTimerRef.current) {
      clearInterval(heartbeatTimerRef.current);
      heartbeatTimerRef.current = null;
    }
    if (reconnectTimerRef.current) {
      clearTimeout(reconnectTimerRef.current);
      reconnectTimerRef.current = null;
    }
    
    if (eventSourceRef.current) {
      eventSourceRef.current.close();
      eventSourceRef.current = null;
    }
    setLoading(false);
    setIsConnected(false);
    setCurrentMessage('已取消');
    addLog('warning', '用户取消分析');
  };

  useEffect(() => {
    return () => {
      if (heartbeatTimerRef.current) {
        clearInterval(heartbeatTimerRef.current);
      }
      if (reconnectTimerRef.current) {
        clearTimeout(reconnectTimerRef.current);
      }
      if (eventSourceRef.current) {
        eventSourceRef.current.close();
      }
    };
  }, []);

  const getRiskLevelColor = (level?: string) => {
    switch (level?.toUpperCase()) {
      case 'LOW':
        return 'text-green-600 bg-green-50 dark:bg-green-900/20 border-green-200';
      case 'MEDIUM':
        return 'text-yellow-600 bg-yellow-50 dark:bg-yellow-900/20 border-yellow-200';
      case 'HIGH':
        return 'text-red-600 bg-red-50 dark:bg-red-900/20 border-red-200';
      default:
        return 'text-gray-600 bg-gray-50 dark:bg-gray-800 border-gray-200';
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

  const getRiskPreferenceName = (code: string) => {
    return riskPreferences.find(r => r.code === code)?.name || code;
  };

  const getRiskPreferenceColor = (code: string) => {
    return riskPreferences.find(r => r.code === code)?.color || 'gray';
  };

  const getLogTypeColor = (type: string) => {
    switch (type) {
      case 'success':
        return 'text-green-600';
      case 'error':
        return 'text-red-600';
      case 'warning':
        return 'text-yellow-600';
      default:
        return 'text-blue-600';
    }
  };

  return (
    <div className="w-full bg-white dark:bg-gray-900 rounded-xl shadow-xl p-6">
      {/* 标题栏 */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <div className="p-3 bg-gradient-to-br from-purple-500 to-indigo-600 rounded-xl shadow-lg">
            <Brain className="w-6 h-6 text-white" />
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
        
        {/* 风险偏好选择和生成决策按钮 */}
        <div className="flex items-center gap-3">
          {/* 连接状态指示器 */}
          {loading && (
            <div className={`flex items-center gap-2 px-3 py-1.5 rounded-full text-xs font-medium transition-all ${
              isConnected 
                ? 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400 shadow-sm' 
                : 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-400 shadow-sm'
            }`}>
              {isConnected ? (
                <>
                  <Wifi className="w-3.5 h-3.5" />
                  <span>已连接</span>
                </>
              ) : (
                <>
                  <WifiOff className="w-3.5 h-3.5" />
                  <span>重连中</span>
                </>
              )}
            </div>
          )}
          
          {/* 风险偏好下拉框 */}
          <div className="relative">
            <select
              value={riskPreference}
              onChange={(e) => setRiskPreference(e.target.value)}
              disabled={loading}
              className="px-4 py-2 pr-10 border border-gray-200 dark:border-gray-700 rounded-xl focus:outline-none focus:ring-2 focus:ring-purple-500 dark:bg-gray-800 dark:text-white text-sm appearance-none cursor-pointer shadow-sm hover:border-purple-300 transition-colors"
            >
              {riskPreferences.map((risk) => (
                <option key={risk.code} value={risk.code}>
                  {risk.name} - {risk.description}
                </option>
              ))}
            </select>
            <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500 pointer-events-none" />
          </div>
          
          {/* 生成决策/取消按钮 */}
          {loading ? (
            <button
              onClick={handleCancel}
              className="px-5 py-2.5 bg-gradient-to-r from-red-500 to-red-600 text-white rounded-xl hover:from-red-600 hover:to-red-700 transition-all shadow-lg hover:shadow-xl flex items-center gap-2 font-medium"
            >
              <Loader2 className="w-4 h-4 animate-spin" />
              取消
            </button>
          ) : (
            <button
              onClick={handleMakeDecision}
              disabled={!modelId}
              className="px-5 py-2.5 bg-gradient-to-r from-purple-500 to-indigo-600 text-white rounded-xl hover:from-purple-600 hover:to-indigo-700 transition-all shadow-lg hover:shadow-xl flex items-center gap-2 font-medium disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:shadow-lg"
            >
              <Brain className="w-4 h-4" />
              生成决策
            </button>
          )}
        </div>
      </div>

      {/* 错误提示 */}
      {error && (
        <div className="mb-6 p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-xl animate-pulse">
          <div className="flex items-center gap-3">
            <AlertTriangle className="w-5 h-5 text-red-600" />
            <p className="text-red-800 dark:text-red-200 font-medium">{error}</p>
          </div>
        </div>
      )}

      {/* 加载状态 - 展示进度 */}
      {loading && (
        <div className="mb-6 space-y-5">
          {/* 进度条 */}
          <div className="p-5 bg-gradient-to-r from-purple-50 to-indigo-50 dark:from-purple-900/20 dark:to-indigo-900/20 rounded-xl border border-purple-100 dark:border-purple-800">
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2">
                <Loader2 className="w-5 h-5 text-purple-600 animate-spin" />
                <span className="text-sm font-semibold text-purple-700 dark:text-purple-300">
                  {currentMessage || '正在初始化分析...'}
                </span>
              </div>
              <span className="text-lg font-bold text-purple-600 dark:text-purple-400">
                {progress}%
              </span>
            </div>
            <div className="w-full bg-purple-200 dark:bg-purple-800 rounded-full h-3 overflow-hidden">
              <div 
                className="bg-gradient-to-r from-purple-500 to-indigo-600 h-3 rounded-full transition-all duration-500 ease-out"
                style={{ width: `${progress}%` }}
              />
            </div>
            {reconnectCount > 0 && (
              <p className="text-xs text-yellow-600 dark:text-yellow-400 mt-2 flex items-center gap-1">
                <AlertTriangle className="w-3 h-3" />
                重连次数: {reconnectCount}/{maxReconnectAttempts}
              </p>
            )}
          </div>

          {/* 步骤列表 */}
          <div className="grid grid-cols-2 md:grid-cols-5 gap-3">
            {progressSteps.map((step) => (
              <div 
                key={step.id}
                className={`flex items-center gap-2 p-3 rounded-xl text-xs font-medium transition-all duration-300 ${
                  step.status === 'completed' 
                    ? 'bg-green-50 dark:bg-green-900/20 text-green-700 dark:text-green-300 border border-green-200 dark:border-green-800' 
                    : step.status === 'active'
                    ? 'bg-purple-50 dark:bg-purple-900/20 text-purple-700 dark:text-purple-300 border border-purple-200 dark:border-purple-800 shadow-md animate-pulse'
                    : 'bg-gray-50 dark:bg-gray-800 text-gray-400 border border-gray-200 dark:border-gray-700'
                }`}
              >
                <div className={`${step.status === 'active' ? 'animate-spin' : ''}`}>
                  {step.status === 'completed' ? (
                    <CheckCircle2 className="w-4 h-4 flex-shrink-0" />
                  ) : step.status === 'active' ? (
                    <Loader2 className="w-4 h-4 flex-shrink-0" />
                  ) : (
                    <Circle className="w-4 h-4 flex-shrink-0" />
                  )}
                </div>
                <span className="truncate">{step.message}</span>
              </div>
            ))}
          </div>

          {/* AI思考过程 */}
          {thinkingSteps.length > 0 && (
            <div className="p-5 bg-gradient-to-br from-amber-50 to-yellow-50 dark:from-amber-900/20 dark:to-yellow-900/20 rounded-xl border border-amber-200 dark:border-amber-800">
              <h4 className="text-sm font-semibold text-amber-800 dark:text-amber-300 mb-3 flex items-center gap-2">
                <Lightbulb className="w-4 h-4" />
                AI思考过程
              </h4>
              <div className="space-y-2 max-h-40 overflow-y-auto pr-2">
                {thinkingSteps.map((step, index) => (
                  <div 
                    key={step.id} 
                    className="flex items-start gap-2 text-xs text-amber-700 dark:text-amber-400 animate-fadeIn"
                    style={{ animationDelay: `${index * 50}ms` }}
                  >
                    <span className="w-1.5 h-1.5 bg-amber-400 rounded-full mt-1.5 flex-shrink-0" />
                    <span className="leading-relaxed">{step.message}</span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* 交易执行结果 */}
          {tradeResults.length > 0 && (
            <div className="p-5 bg-gray-50 dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700">
              <h4 className="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-3 flex items-center gap-2">
                <TrendingUp className="w-4 h-4" />
                交易执行
              </h4>
              <div className="space-y-2">
                {tradeResults.map((result, index) => (
                  <div 
                    key={index} 
                    className={`flex items-center gap-2 p-2 rounded-lg text-xs ${
                      result.success 
                        ? 'bg-green-50 dark:bg-green-900/20 text-green-700 dark:text-green-300' 
                        : 'bg-red-50 dark:bg-red-900/20 text-red-700 dark:text-red-300'
                    }`}
                  >
                    {result.success ? (
                      <CheckCircle2 className="w-4 h-4 flex-shrink-0" />
                    ) : (
                      <AlertTriangle className="w-4 h-4 flex-shrink-0" />
                    )}
                    <span className="font-medium">{result.action}</span>
                    <span className="text-gray-500">{result.stockCode}</span>
                    <span className="text-gray-400">|</span>
                    <span>{result.message}</span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* 实时日志 */}
          {logs.length > 0 && (
            <div className="p-4 bg-gray-900 rounded-xl border border-gray-700">
              <h4 className="text-xs font-semibold text-gray-400 mb-2 flex items-center gap-2">
                <Zap className="w-3 h-3" />
                系统日志
              </h4>
              <div className="space-y-1 max-h-32 overflow-y-auto font-mono text-xs">
                {logs.map((log, index) => (
                  <div key={index} className="flex items-center gap-2">
                    <span className="text-gray-500">[{log.time}]</span>
                    <span className={getLogTypeColor(log.type)}>[{log.type.toUpperCase()}]</span>
                    <span className="text-gray-300">{log.message}</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}

      {/* 决策结果展示 */}
      {decision && !loading && (
        <div className="space-y-6 animate-fadeIn">
          {/* 决策总结卡片 */}
          <div className="p-6 bg-gradient-to-r from-purple-50 via-indigo-50 to-blue-50 dark:from-purple-900/20 dark:via-indigo-900/20 dark:to-blue-900/20 rounded-xl border border-purple-200 dark:border-purple-800">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-3">
                <div className="p-2 bg-white dark:bg-gray-800 rounded-lg shadow-sm">
                  <Sparkles className="w-5 h-5 text-purple-600" />
                </div>
                <h3 className="text-lg font-bold text-gray-800 dark:text-gray-200">
                  决策总结
                </h3>
              </div>
              <span className={`px-3 py-1.5 text-sm font-semibold rounded-full bg-white dark:bg-gray-800 shadow-sm border ${
                getRiskPreferenceColor(riskPreference) === 'green' ? 'border-green-200 text-green-700' :
                getRiskPreferenceColor(riskPreference) === 'purple' ? 'border-purple-200 text-purple-700' :
                'border-blue-200 text-blue-700'
              }`}>
                {getRiskPreferenceName(riskPreference)}
              </span>
            </div>
            <p className="text-gray-800 dark:text-gray-200 leading-relaxed text-base">{decision.summary}</p>
          </div>

          {/* 交易决策 */}
          {decision.tradeDecisions && decision.tradeDecisions.length > 0 && (
            <div>
              <h3 className="text-lg font-bold text-gray-800 dark:text-gray-200 mb-4 flex items-center gap-2">
                <Target className="w-5 h-5 text-purple-600" />
                交易决策
              </h3>
              <div className="space-y-4">
                {decision.tradeDecisions.map((trade, index) => (
                  <div
                    key={index}
                    className={`p-5 rounded-xl border-2 ${getActionColor(trade.action)} shadow-sm hover:shadow-md transition-shadow`}
                  >
                    <div className="flex items-center justify-between mb-4">
                      <div className="flex items-center gap-4">
                        <span className={`px-4 py-1.5 rounded-full text-sm font-bold ${
                          trade.action?.toUpperCase() === 'BUY' ? 'bg-green-100 text-green-700' :
                          trade.action?.toUpperCase() === 'SELL' ? 'bg-red-100 text-red-700' :
                          'bg-blue-100 text-blue-700'
                        }`}>
                          {getActionText(trade.action)}
                        </span>
                        <span className="font-bold text-lg text-gray-900 dark:text-white">
                          {trade.stockName}
                        </span>
                        <span className="text-gray-500 font-mono">{trade.stockCode}</span>
                      </div>
                      {trade.action?.toUpperCase() === 'BUY' ? (
                        <TrendingUp className="w-6 h-6 text-green-600" />
                      ) : trade.action?.toUpperCase() === 'SELL' ? (
                        <TrendingDown className="w-6 h-6 text-red-600" />
                      ) : null}
                    </div>
                    
                    <div className="grid grid-cols-3 gap-4 mb-4">
                      <div className="p-3 bg-white dark:bg-gray-800 rounded-lg">
                        <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">交易数量</p>
                        <p className="text-xl font-bold text-gray-900 dark:text-white">{trade.amount} <span className="text-sm font-normal text-gray-500">股</span></p>
                      </div>
                      <div className="p-3 bg-white dark:bg-gray-800 rounded-lg">
                        <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">执行状态</p>
                        <p className={`text-sm font-bold flex items-center gap-1 ${trade.executed ? 'text-green-600' : 'text-red-600'}`}>
                          {trade.executed ? (
                            <><CheckCircle2 className="w-4 h-4" /> 已执行</>
                          ) : (
                            <><AlertTriangle className="w-4 h-4" /> 未执行</>
                          )}
                        </p>
                      </div>
                      <div className="p-3 bg-white dark:bg-gray-800 rounded-lg">
                        <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">置信度</p>
                        <p className="text-sm font-bold text-gray-700 dark:text-gray-300">{trade.confidence || 'N/A'}</p>
                      </div>
                    </div>
                    
                    <div className="p-3 bg-white dark:bg-gray-800 rounded-lg">
                      <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">决策原因</p>
                      <p className="text-sm text-gray-700 dark:text-gray-300 leading-relaxed">{trade.reason}</p>
                    </div>
                    
                    {trade.executionMessage && (
                      <p className="text-xs text-gray-500 dark:text-gray-400 mt-2 italic">
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
              <h3 className="text-lg font-bold text-gray-800 dark:text-gray-200 mb-4 flex items-center gap-2">
                <BarChart3 className="w-5 h-5 text-blue-600" />
                市场分析
              </h3>
              <div className="p-5 bg-blue-50 dark:bg-blue-900/20 rounded-xl border border-blue-200 dark:border-blue-800">
                <div className="flex items-center gap-2 mb-4">
                  <TrendingUp className="w-5 h-5 text-blue-600" />
                  <p className="text-gray-800 dark:text-gray-200">
                    <span className="font-bold">整体趋势：</span>
                    {decision.marketAnalysis.overallTrend}
                  </p>
                </div>
                
                {decision.marketAnalysis.stockAnalyses && decision.marketAnalysis.stockAnalyses.length > 0 && (
                  <div className="space-y-3">
                    {decision.marketAnalysis.stockAnalyses.map((analysis, index) => (
                      <div key={index} className="p-4 bg-white dark:bg-gray-800 rounded-xl shadow-sm">
                        <div className="flex items-center justify-between mb-3">
                          <span className="font-bold text-gray-900 dark:text-white text-lg">
                            {analysis.stockName}
                          </span>
                          <span className="text-sm px-3 py-1 bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300 rounded-full">
                            {analysis.trend}
                          </span>
                        </div>
                        <p className="text-sm text-gray-600 dark:text-gray-400 mb-3 leading-relaxed">
                          {analysis.technicalAnalysis}
                        </p>
                        <div className="flex gap-6 text-sm">
                          <div className="flex items-center gap-2">
                            <span className="text-gray-500">支撑位:</span>
                            <span className="font-mono font-semibold text-green-600">{analysis.supportLevel}</span>
                          </div>
                          <div className="flex items-center gap-2">
                            <span className="text-gray-500">阻力位:</span>
                            <span className="font-mono font-semibold text-red-600">{analysis.resistanceLevel}</span>
                          </div>
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
              <h3 className="text-lg font-bold text-gray-800 dark:text-gray-200 mb-4 flex items-center gap-2">
                <Shield className="w-5 h-5 text-amber-600" />
                风险评估
              </h3>
              <div className={`p-5 rounded-xl border-2 ${getRiskLevelColor(decision.riskAssessment.riskLevel)}`}>
                <div className="flex items-center justify-between mb-4">
                  <span className="font-bold text-lg">风险等级</span>
                  <span className={`px-4 py-1.5 rounded-full text-sm font-bold ${
                    decision.riskAssessment.riskLevel?.toUpperCase() === 'LOW' ? 'bg-green-100 text-green-700' :
                    decision.riskAssessment.riskLevel?.toUpperCase() === 'HIGH' ? 'bg-red-100 text-red-700' :
                    'bg-yellow-100 text-yellow-700'
                  }`}>
                    {decision.riskAssessment.riskLevel}
                  </span>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="p-3 bg-white dark:bg-gray-800 rounded-lg">
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">风险说明</p>
                    <p className="text-sm text-gray-700 dark:text-gray-300">{decision.riskAssessment.riskDescription}</p>
                  </div>
                  <div className="p-3 bg-white dark:bg-gray-800 rounded-lg">
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">风控措施</p>
                    <p className="text-sm text-gray-700 dark:text-gray-300">{decision.riskAssessment.riskControlMeasures}</p>
                  </div>
                  <div className="p-3 bg-white dark:bg-gray-800 rounded-lg">
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">预期收益</p>
                    <p className="text-sm font-semibold text-green-600">{decision.riskAssessment.expectedReturn}</p>
                  </div>
                  <div className="p-3 bg-white dark:bg-gray-800 rounded-lg">
                    <p className="text-xs text-gray-500 dark:text-gray-400 mb-1">最大可承受损失</p>
                    <p className="text-sm font-semibold text-red-600">{decision.riskAssessment.maxAcceptableLoss}</p>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      )}

      {/* 空状态 */}
      {!decision && !loading && !error && (
        <div className="text-center py-16 text-gray-500 dark:text-gray-400">
          <div className="p-6 bg-gradient-to-br from-purple-100 to-indigo-100 dark:from-purple-900/30 dark:to-indigo-900/30 rounded-full w-24 h-24 mx-auto mb-6 flex items-center justify-center">
            <Brain className="w-12 h-12 text-purple-600" />
          </div>
          <p className="text-lg font-medium mb-2 text-gray-700 dark:text-gray-300">选择风险偏好并点击"生成决策"</p>
          <p className="text-sm text-gray-500">让AI根据您的风险偏好分析并做出投资决策</p>
        </div>
      )}
    </div>
  );
}
