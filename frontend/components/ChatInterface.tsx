'use client';

import { useState, useRef, useEffect } from 'react';
import { Send, Bot, User, Loader2 } from 'lucide-react';
import type { ChatMessage } from '@/types';

interface ChatInterfaceProps {
  onSendMessage?: (message: string) => Promise<string>;
  stockCode?: string;
  stockName?: string;
}

export default function ChatInterface({
  onSendMessage,
  stockCode,
  stockName,
}: ChatInterfaceProps) {
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: '1',
      role: 'assistant',
      content: `你好！我是AI投资顾问。我可以帮你分析${stockName || stockCode || '股票'}的投资情况，提供交易建议和市场洞察。有什么问题可以问我！`,
      timestamp: new Date(),
    },
  ]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSend = async () => {
    if (!input.trim() || isLoading) return;

    const userMessage: ChatMessage = {
      id: Date.now().toString(),
      role: 'user',
      content: input.trim(),
      timestamp: new Date(),
    };

    setMessages((prev) => [...prev, userMessage]);
    setInput('');
    setIsLoading(true);

    try {
      let responseText = '';
      if (onSendMessage) {
        responseText = await onSendMessage(userMessage.content);
      } else {
        // 模拟AI回复（如果没有后端支持）
        await new Promise((resolve) => setTimeout(resolve, 1000));
        responseText = generateMockResponse(userMessage.content, stockCode, stockName);
      }

      const assistantMessage: ChatMessage = {
        id: (Date.now() + 1).toString(),
        role: 'assistant',
        content: responseText,
        timestamp: new Date(),
      };

      setMessages((prev) => [...prev, assistantMessage]);
    } catch (error) {
      const errorMessage: ChatMessage = {
        id: (Date.now() + 1).toString(),
        role: 'assistant',
        content: '抱歉，处理您的请求时出现了错误。请稍后再试。',
        timestamp: new Date(),
      };
      setMessages((prev) => [...prev, errorMessage]);
    } finally {
      setIsLoading(false);
      inputRef.current?.focus();
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  const generateMockResponse = (
    userMessage: string,
    stockCode?: string,
    stockName?: string
  ): string => {
    const lowerMessage = userMessage.toLowerCase();
    const stock = stockName || stockCode || '该股票';

    if (lowerMessage.includes('价格') || lowerMessage.includes('走势')) {
      return `根据当前数据分析，${stock}的价格走势显示了一定的波动性。建议关注关键支撑位和阻力位，结合成交量变化来判断趋势。`;
    } else if (lowerMessage.includes('买入') || lowerMessage.includes('买')) {
      return `关于${stock}的买入建议：建议在价格回调至支撑位附近时考虑分批买入，同时设置止损位以控制风险。请注意市场整体趋势和个股基本面。`;
    } else if (lowerMessage.includes('卖出') || lowerMessage.includes('卖')) {
      return `关于${stock}的卖出建议：如果已达到目标价位或出现明显的技术面转弱信号，可以考虑分批止盈。建议保留部分仓位以应对可能的继续上涨。`;
    } else if (lowerMessage.includes('风险') || lowerMessage.includes('危险')) {
      return `${stock}的主要风险包括：市场波动风险、行业政策风险、公司基本面变化等。建议分散投资，不要将所有资金集中在单一股票上。`;
    } else if (lowerMessage.includes('预测') || lowerMessage.includes('未来')) {
      return `基于技术分析和市场趋势，${stock}在未来一段时间内可能会继续震荡上行，但需要密切关注市场情绪和资金流向的变化。`;
    } else {
      return `感谢您的问题。关于${stock}，我建议您关注以下几个方面：1. 技术面分析（价格走势、成交量）2. 基本面分析（公司业绩、行业前景）3. 市场情绪和资金流向。如果您有具体问题，可以详细描述，我会给出更精准的建议。`;
    }
  };

  return (
    <div className="w-full h-full bg-white dark:bg-gray-900 rounded-lg shadow-lg flex flex-col">
      {/* 聊天头部 */}
      <div className="p-4 border-b border-gray-200 dark:border-gray-700">
        <div className="flex items-center gap-2">
          <Bot className="w-5 h-5 text-blue-600" />
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
            AI投资顾问
          </h3>
        </div>
        {stockCode && (
          <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
            当前分析: {stockName || stockCode}
          </p>
        )}
      </div>

      {/* 消息列表 */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4">
        {messages.map((message) => (
          <div
            key={message.id}
            className={`flex gap-3 ${
              message.role === 'user' ? 'justify-end' : 'justify-start'
            }`}
          >
            {message.role === 'assistant' && (
              <div className="flex-shrink-0 w-8 h-8 rounded-full bg-blue-100 dark:bg-blue-900 flex items-center justify-center">
                <Bot className="w-5 h-5 text-blue-600 dark:text-blue-400" />
              </div>
            )}
            <div
              className={`max-w-[70%] rounded-lg px-4 py-2 ${
                message.role === 'user'
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-100 dark:bg-gray-800 text-gray-900 dark:text-white'
              }`}
            >
              <p className="text-sm whitespace-pre-wrap">{message.content}</p>
              <p className="text-xs opacity-70 mt-1">
                {message.timestamp.toLocaleTimeString('zh-CN', {
                  hour: '2-digit',
                  minute: '2-digit',
                })}
              </p>
            </div>
            {message.role === 'user' && (
              <div className="flex-shrink-0 w-8 h-8 rounded-full bg-gray-200 dark:bg-gray-700 flex items-center justify-center">
                <User className="w-5 h-5 text-gray-600 dark:text-gray-300" />
              </div>
            )}
          </div>
        ))}
        {isLoading && (
          <div className="flex gap-3 justify-start">
            <div className="flex-shrink-0 w-8 h-8 rounded-full bg-blue-100 dark:bg-blue-900 flex items-center justify-center">
              <Bot className="w-5 h-5 text-blue-600 dark:text-blue-400" />
            </div>
            <div className="bg-gray-100 dark:bg-gray-800 rounded-lg px-4 py-2">
              <Loader2 className="w-5 h-5 animate-spin text-blue-600" />
            </div>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* 输入区域 */}
      <div className="p-4 border-t border-gray-200 dark:border-gray-700">
        <div className="flex gap-2">
          <input
            ref={inputRef}
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyPress={handleKeyPress}
            placeholder="输入您的问题..."
            className="flex-1 px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-800 dark:text-white"
            disabled={isLoading}
          />
          <button
            onClick={handleSend}
            disabled={!input.trim() || isLoading}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center gap-2"
          >
            {isLoading ? (
              <Loader2 className="w-5 h-5 animate-spin" />
            ) : (
              <Send className="w-5 h-5" />
            )}
          </button>
        </div>
        <p className="text-xs text-gray-500 dark:text-gray-400 mt-2">
          按 Enter 发送，Shift + Enter 换行
        </p>
      </div>
    </div>
  );
}
