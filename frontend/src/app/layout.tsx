import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";

const inter = Inter({
  subsets: ["latin", "vietnamese"],
  variable: "--font-sans",
});

export const metadata: Metadata = {
  title: "Smart PDF Learning Hub - Học Tập Thông Minh Bằng AI",
  description: "Trang web hỗ trợ học tập đắc lực: Đọc PDF, Highlight ghi chú, Chat hỏi đáp RAG, Tự động tạo câu hỏi trắc nghiệm Quiz, Thẻ Flashcards ôn tập ngẫu nhiên và Bản đồ tư duy Mindmap.",
  keywords: ["PDF AI", "Learning Hub", "Học tập thông minh", "RAG PDF", "Gemini AI Quiz", "Flashcard Spaced Repetition", "React Flow Mindmap"],
  authors: [{ name: "Smart PDF Team" }],
  viewport: "width=device-width, initial-scale=1",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="vi" className="dark">
      <body className={`${inter.variable} font-sans min-h-screen bg-background text-foreground antialiased selection:bg-primary/20 selection:text-primary`}>
        {/* Ambient background glow decoration */}
        <div className="fixed -z-10 top-0 left-0 w-full h-full overflow-hidden pointer-events-none opacity-40">
          <div className="absolute top-[-10%] left-[-10%] w-[50%] h-[50%] rounded-full bg-primary/10 blur-[120px]" />
          <div className="absolute bottom-[-10%] right-[-10%] w-[50%] h-[50%] rounded-full bg-accent/15 blur-[120px]" />
        </div>
        
        {children}
      </body>
    </html>
  );
}
