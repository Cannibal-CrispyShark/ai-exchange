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

// AI收益数据
export interface EarnInStock {
  income: number;
  stockCode: string;
}

export interface EarnInDay {
  income: number;
  day: string; // YYYY-MM-DD
}

export interface AiIncome {
  incomeTotal: number;
  earnInStocks: EarnInStock[];
  earnInDays: EarnInDay[];
}

// 持仓信息
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
