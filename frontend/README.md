# AI模拟投资分析平台 - 前端

这是一个基于 Next.js 15 + TypeScript 构建的AI模拟投资分析平台前端应用。

## 功能特性

- 📈 **交互式股票图表**：使用 Recharts 展示股票价格走势，支持开盘价、收盘价、最高价、最低价等多维度数据可视化
- 🤖 **AI持仓面板**：实时展示AI模型的持仓情况和盈亏分析
- 💬 **智能对话界面**：与AI投资顾问进行实时对话，获取投资建议和市场分析

## 技术栈

- **框架**: Next.js 15
- **语言**: TypeScript
- **UI库**: TailwindCSS
- **图表库**: Recharts
- **HTTP客户端**: Axios
- **图标**: Lucide React

## 项目结构

```
frontend/
├── app/                    # Next.js App Router
│   ├── layout.tsx         # 根布局
│   ├── page.tsx           # 主页面
│   └── globals.css        # 全局样式
├── components/             # React组件
│   ├── StockChart.tsx     # 股票图表组件
│   ├── AIPositionPanel.tsx # AI持仓面板组件
│   └── ChatInterface.tsx  # 对话界面组件
├── hooks/                  # 自定义Hooks
│   └── useStockData.ts    # 股票数据获取Hook
├── lib/                    # 工具函数
│   ├── api.ts             # API服务
│   └── utils.ts           # 通用工具函数
├── types/                  # TypeScript类型定义
│   └── index.ts           # 类型定义
└── package.json           # 项目配置
```

## 安装和运行

### 1. 安装依赖

```bash
cd frontend
npm install
# 或
yarn install
# 或
pnpm install
```

### 2. 配置后端API

确保后端服务运行在 `http://localhost:8080`，或者修改 `next.config.mjs` 中的代理配置。

### 3. 启动开发服务器

```bash
npm run dev
# 或
yarn dev
# 或
pnpm dev
```

应用将在 [http://localhost:3000](http://localhost:3000) 启动。

### 4. 构建生产版本

```bash
npm run build
npm start
```

## 使用说明

1. **查询股票数据**：
   - 在顶部导航栏输入股票代码（如：AAPL、MSFT等）
   - 点击"查询"按钮或按Enter键
   - 股票图表将自动更新

2. **查看AI持仓**：
   - 输入AI模型代码（如：AI001）
   - 右侧面板将显示该模型的持仓情况和收益分析

3. **与AI对话**：
   - 在底部对话界面输入问题
   - AI投资顾问将提供专业的投资建议和市场分析

## API接口

前端通过以下API与后端通信：

- `GET /api/stocks/{stockCode}/get` - 获取股票基础数据
- `GET /api/ai/{aiCode}/income` - 获取AI收益数据

## 开发说明

### 添加新组件

在 `components/` 目录下创建新的组件文件，使用 TypeScript 和 TailwindCSS。

### 添加新API

在 `lib/api.ts` 中添加新的API调用函数。

### 自定义样式

修改 `app/globals.css` 或使用 TailwindCSS 的类名进行样式定制。

## 浏览器支持

- Chrome (最新版本)
- Firefox (最新版本)
- Safari (最新版本)
- Edge (最新版本)

## 注意事项

- 确保后端服务已启动并运行在正确的端口
- 如果后端API返回的数据格式不同，需要相应调整 `types/index.ts` 中的类型定义
- 对话功能目前使用模拟数据，如需真实AI对话，需要后端提供相应的API接口

## 许可证

仅供学习研究使用。
