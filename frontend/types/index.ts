// API响应类型（与后端Response.java对应）
export interface ApiResponse<T> {
  body: T | null;
  message: string;
}

// 股票基础数据
export interface StockBase {
  id: number;
  time: string; // YYYY-MM-DD
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
}

// 股票未来预测
export interface StockFuture {
  futurePrice?: number;
  trend?: string;
  confidence?: number;
}

// 股票元数据
export interface MetaData {
  information?: string;
  symbol?: string;
  lastRefreshed?: string;
  interval?: string;
  outputSize?: string;
  timeZone?: string;
}

// 股票信息VO
export interface StockInfoVO {
  stockBase: StockBase[];
  stockFuture?: StockFuture;
  metaData?: MetaData;
}

// 股票持仓明细
export interface StockPositionDetail {
  stockCode: string;
  stockName: string;
  position: number; // 持仓数量
  averageCost: number; // 平均成本
  currentPrice: number; // 当前价格
  returnRate: number; // 收益率（当前价格 / 平均成本）
  realizedProfit: number; // 已实现收益
  unrealizedProfit: number; // 未实现收益
  totalProfit: number; // 总收益
}

// AI收益数据（新结构 - 使用平均成本法）
export interface AiIncome {
  income: number; // 总收益（取整）
  yieldRate: number; // 整体收益率
  positionCount: number; // 持仓股票种类数
  stockCount: number; // 总持仓股数
  totalCost: number; // 总成本
  realizedProfit: number; // 总已实现收益
  unrealizedProfit: number; // 总未实现收益
  positionDetails: StockPositionDetail[]; // 持仓明细
}

// 持仓信息（旧结构，用于兼容）
export interface Position {
  stockCode: string;
  stockName: string;
  volume: number; // 正为卖出，负为买入
  date: string;
  currentPrice?: number;
  profit?: number;
  profitRate?: number;
}

// 对话消息
export interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
}

// AI投资决策VO
export interface AiDecisionVO {
  summary: string;
  tradeDecisions: TradeDecision[];
  marketAnalysis: MarketAnalysis;
  riskAssessment: RiskAssessment;
}

// 交易决策
export interface TradeDecision {
  action: string; // BUY/SELL/HOLD
  stockCode: string;
  stockName: string;
  amount: number;
  reason: string;
  executed?: boolean;
  executionMessage?: string;
}

// 市场分析
export interface MarketAnalysis {
  overallTrend: string;
  stockAnalyses: StockAnalysis[];
}

// 个股分析
export interface StockAnalysis {
  stockCode: string;
  stockName: string;
  technicalAnalysis: string;
  trend: string;
  supportLevel: string;
  resistanceLevel: string;
}

// 风险评估
export interface RiskAssessment {
  riskLevel: string; // LOW/MEDIUM/HIGH
  riskDescription: string;
  riskControlMeasures: string;
  expectedReturn: string;
  maxAcceptableLoss: string;
}
