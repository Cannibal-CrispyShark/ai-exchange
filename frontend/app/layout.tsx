import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "AI模拟投资分析平台",
  description: "基于AI的股票投资模拟分析平台",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="zh-CN">
      <body className="antialiased">
        {children}
      </body>
    </html>
  );
}
