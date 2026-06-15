import Link from "next/link";
import { 
  FileText, 
  MessageSquareCode, 
  BrainCircuit, 
  Sparkles, 
  CheckCircle, 
  Layers, 
  TrendingUp, 
  ArrowRight,
  Bookmark,
  FileQuestion,
  Workflow
} from "lucide-react";

export default function Home() {
  return (
    <div className="flex flex-col min-h-screen">
      {/* Navigation Bar */}
      <header className="sticky top-0 z-50 w-full glass-panel border-b border-border/50">
        <div className="container mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-2 font-bold text-xl tracking-tight">
            <div className="p-1.5 rounded-lg bg-primary text-white flex items-center justify-center">
              <BrainCircuit className="w-5 h-5" />
            </div>
            <span className="bg-gradient-to-r from-foreground via-foreground/90 to-muted-foreground bg-clip-text text-transparent">
              Smart PDF <span className="text-primary">Learning Hub</span>
            </span>
          </div>

          <nav className="hidden md:flex items-center gap-6 text-sm font-medium text-muted-foreground">
            <a href="#features" className="hover:text-foreground transition-colors">Tính năng</a>
            <a href="#workflow" className="hover:text-foreground transition-colors">Quy trình</a>
            <a href="#about" className="hover:text-foreground transition-colors">Về dự án</a>
          </nav>

          <div className="flex items-center gap-3">
            <Link 
              href="/auth/login" 
              className="text-sm font-medium hover:text-foreground text-muted-foreground transition-colors px-4 py-2"
            >
              Đăng nhập
            </Link>
            <Link 
              href="/auth/signup" 
              className="text-sm font-medium bg-primary text-primary-foreground hover:bg-primary/95 transition-all shadow-md shadow-primary/10 rounded-lg px-4 py-2 flex items-center gap-1.5"
            >
              Bắt đầu miễn phí <ArrowRight className="w-4 h-4" />
            </Link>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <main className="flex-1">
        <section className="relative py-20 lg:py-32 overflow-hidden flex items-center justify-center">
          <div className="container mx-auto px-6 text-center max-w-4xl">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full border border-primary/20 bg-primary/5 text-primary text-xs font-semibold mb-6 animate-pulse">
              <Sparkles className="w-3.5 h-3.5" /> Tích hợp trí tuệ nhân tạo Gemini AI
            </div>
            
            <h1 className="text-4xl sm:text-6xl font-extrabold tracking-tight mb-8">
              Biến Tài Liệu PDF Thành <br />
              <span className="bg-gradient-to-r from-primary via-purple-500 to-indigo-400 bg-clip-text text-transparent">
                Kho Tàng Kiến Thức Chủ Động
              </span>
            </h1>
            
            <p className="text-lg text-muted-foreground mb-10 max-w-2xl mx-auto leading-relaxed">
              Tải tài liệu của bạn lên, đọc trực tuyến, highlight thông tin quan trọng. Hệ thống AI tự động phân tích giúp bạn tóm tắt, hỏi đáp RAG, sinh bộ câu hỏi trắc nghiệm Quiz, tạo thẻ ôn tập Flashcards và sơ đồ tư duy Mindmap tức thì.
            </p>

            <div className="flex flex-col sm:flex-row items-center justify-center gap-4 mb-16">
              <Link 
                href="/auth/signup" 
                className="w-full sm:w-auto text-base font-semibold bg-primary text-primary-foreground hover:bg-primary/95 hover:scale-[1.02] active:scale-[0.98] transition-all shadow-lg shadow-primary/25 rounded-xl px-8 py-4 flex items-center justify-center gap-2"
              >
                Trải nghiệm ngay <ArrowRight className="w-5 h-5" />
              </Link>
              <a 
                href="#features" 
                className="w-full sm:w-auto text-base font-semibold border border-border hover:bg-secondary/50 rounded-xl px-8 py-4 flex items-center justify-center gap-2 transition-all"
              >
                Xem tính năng
              </a>
            </div>

            {/* Glowing UI Preview Mockup */}
            <div className="relative mx-auto max-w-5xl border border-border/80 bg-card rounded-2xl overflow-hidden shadow-2xl shadow-primary/5 glow-card">
              <div className="h-10 bg-muted/50 border-b border-border/50 px-4 flex items-center gap-2">
                <div className="w-3 h-3 rounded-full bg-red-500/80" />
                <div className="w-3 h-3 rounded-full bg-yellow-500/80" />
                <div className="w-3 h-3 rounded-full bg-green-500/80" />
                <div className="flex-1 bg-background/50 text-[11px] text-muted-foreground/80 py-1 px-4 rounded-md max-w-md mx-auto truncate text-center">
                  smart-pdf-hub.vercel.app/documents/rag-intro.pdf
                </div>
              </div>
              
              <div className="grid grid-cols-1 md:grid-cols-3 h-[450px]">
                {/* PDF Left panel mockup */}
                <div className="col-span-2 border-r border-border/50 p-6 flex flex-col justify-between text-left bg-background/30">
                  <div>
                    <div className="flex items-center gap-2 mb-4">
                      <FileText className="w-5 h-5 text-red-500" />
                      <span className="font-semibold text-sm">Học máy nâng cao.pdf (Trang 1/24)</span>
                    </div>
                    <div className="space-y-3">
                      <div className="h-4 bg-muted rounded w-full" />
                      <div className="h-4 bg-muted rounded w-[95%]" />
                      <div className="h-4 bg-primary/15 rounded w-[90%] border-l-2 border-primary pl-2 py-0.5 text-xs text-primary font-medium flex items-center justify-between">
                        <span>Định lý Bayes mô tả xác suất của một sự kiện dựa trên các điều kiện liên quan...</span>
                        <span className="text-[10px] bg-primary/20 px-1.5 py-0.5 rounded text-primary">Highlight</span>
                      </div>
                      <div className="h-4 bg-muted rounded w-[97%]" />
                      <div className="h-4 bg-muted rounded w-[85%]" />
                    </div>
                  </div>
                  
                  <div className="p-4 rounded-xl border border-border/50 bg-card/60 flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <Bookmark className="w-4 h-4 text-amber-500" />
                      <span className="text-xs text-muted-foreground">Đã Bookmark trang 1, 4, 12</span>
                    </div>
                    <span className="text-xs text-primary hover:underline cursor-pointer">Xem tất cả</span>
                  </div>
                </div>

                {/* AI Chat Right panel mockup */}
                <div className="p-4 flex flex-col justify-between bg-muted/20 text-left">
                  <div className="space-y-4">
                    <div className="flex items-center gap-1.5 text-xs text-primary font-semibold border-b border-border/50 pb-2">
                      <MessageSquareCode className="w-4 h-4" /> AI Học Tập Trợ Giúp
                    </div>
                    <div className="space-y-3 text-xs">
                      <div className="p-2.5 rounded-lg bg-secondary/80 text-muted-foreground self-start">
                        Tóm tắt định lý Bayes trong đoạn highlight?
                      </div>
                      <div className="p-2.5 rounded-lg bg-primary/10 text-primary self-end border border-primary/20">
                        Định lý Bayes cho phép cập nhật xác suất của một giả thuyết khi có bằng chứng mới, giúp đưa ra quyết định tối ưu.
                      </div>
                    </div>
                  </div>

                  <div className="relative">
                    <input 
                      type="text" 
                      placeholder="Hỏi AI bất kỳ điều gì về PDF..." 
                      className="w-full text-xs bg-card border border-border rounded-lg pl-3 pr-8 py-2.5 outline-none focus:border-primary transition-colors"
                      disabled
                    />
                    <Sparkles className="w-4 h-4 text-primary absolute right-2.5 top-3" />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Feature Grid */}
        <section id="features" className="py-20 bg-muted/20 border-y border-border/50">
          <div className="container mx-auto px-6">
            <div className="text-center max-w-xl mx-auto mb-16">
              <h2 className="text-3xl font-bold mb-4">Các tính năng vượt trội</h2>
              <p className="text-muted-foreground">
                Tích hợp mọi công cụ học tập tiên tiến nhất giúp nâng tầm khả năng đọc hiểu và ghi nhớ.
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
              {/* Feature 1 */}
              <div className="p-6 rounded-2xl border border-border bg-card glow-card text-left">
                <div className="p-3 rounded-xl bg-primary/10 text-primary w-fit mb-6">
                  <FileText className="w-6 h-6" />
                </div>
                <h3 className="font-semibold text-lg mb-2">Đọc PDF Trực Tuyến</h3>
                <p className="text-sm text-muted-foreground leading-relaxed">
                  Giao diện đọc mượt mà, lưu trang đánh dấu (bookmark), tô màu văn bản (highlight) và viết ghi chú cho từng trang dễ dàng.
                </p>
              </div>

              {/* Feature 2 */}
              <div className="p-6 rounded-2xl border border-border bg-card glow-card text-left">
                <div className="p-3 rounded-xl bg-purple-500/10 text-purple-400 w-fit mb-6">
                  <MessageSquareCode className="w-6 h-6" />
                </div>
                <h3 className="font-semibold text-lg mb-2">Hỏi Đáp RAG Thông Minh</h3>
                <p className="text-sm text-muted-foreground leading-relaxed">
                  Hỏi đáp trực tiếp với tài liệu. AI sẽ tìm kiếm thông tin ngữ cảnh liên quan nhất trong file và đưa ra câu trả lời chuẩn xác.
                </p>
              </div>

              {/* Feature 3 */}
              <div className="p-6 rounded-2xl border border-border bg-card glow-card text-left">
                <div className="p-3 rounded-xl bg-green-500/10 text-green-400 w-fit mb-6">
                  <Layers className="w-6 h-6" />
                </div>
                <h3 className="font-semibold text-lg mb-2">Flashcard Lặp Lại Ngắt Quãng</h3>
                <p className="text-sm text-muted-foreground leading-relaxed">
                  Tự động sinh thẻ ghi nhớ kiến thức từ PDF. Tích hợp thuật toán Spaced Repetition (SM-2) để tối ưu lịch trình ôn luyện.
                </p>
              </div>

              {/* Feature 4 */}
              <div className="p-6 rounded-2xl border border-border bg-card glow-card text-left">
                <div className="p-3 rounded-xl bg-amber-500/10 text-amber-400 w-fit mb-6">
                  <FileQuestion className="w-6 h-6" />
                </div>
                <h3 className="font-semibold text-lg mb-2">Tự Động Tạo Quiz</h3>
                <p className="text-sm text-muted-foreground leading-relaxed">
                  Sinh các bộ câu hỏi trắc nghiệm hoặc đúng/sai dựa trên nội dung PDF giúp bạn tự kiểm tra và củng cố kiến thức đã học.
                </p>
              </div>

              {/* Feature 5 */}
              <div className="p-6 rounded-2xl border border-border bg-card glow-card text-left">
                <div className="p-3 rounded-xl bg-blue-500/10 text-blue-400 w-fit mb-6">
                  <Workflow className="w-6 h-6" />
                </div>
                <h3 className="font-semibold text-lg mb-2">Bản Đồ Tư Duy (Mindmap)</h3>
                <p className="text-sm text-muted-foreground leading-relaxed">
                  AI trực quan hóa toàn bộ bố cục tài liệu thành sơ đồ mindmap phân cấp, cho phép thu phóng và di chuyển linh hoạt.
                </p>
              </div>

              {/* Feature 6 */}
              <div className="p-6 rounded-2xl border border-border bg-card glow-card text-left">
                <div className="p-3 rounded-xl bg-pink-500/10 text-pink-400 w-fit mb-6">
                  <TrendingUp className="w-6 h-6" />
                </div>
                <h3 className="font-semibold text-lg mb-2">Báo Cáo Tiến Độ Dashboard</h3>
                <p className="text-sm text-muted-foreground leading-relaxed">
                  Theo dõi số lượng tài liệu đã học, thống kê số thẻ flashcards đã nhớ, lịch sử làm bài thi quiz và theo dõi chuỗi ngày tự học liên tục.
                </p>
              </div>
            </div>
          </div>
        </section>
      </main>

      {/* Footer */}
      <footer className="border-t border-border py-12 bg-card/50">
        <div className="container mx-auto px-6 flex flex-col md:flex-row items-center justify-between gap-6 text-sm text-muted-foreground">
          <div className="flex items-center gap-2 font-bold text-foreground">
            <BrainCircuit className="w-5 h-5 text-primary" />
            <span>Smart PDF Learning Hub</span>
          </div>
          <div>
            &copy; {new Date().getFullYear()} Smart PDF Learning Hub. Dự án mẫu cao cấp ứng tuyển vị trí Backend/Frontend.
          </div>
        </div>
      </footer>
    </div>
  );
}
