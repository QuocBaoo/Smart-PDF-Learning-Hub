-- ==========================================
-- SMART PDF LEARNING HUB - DATABASE SCHEMA
-- Target: Supabase PostgreSQL (with pgvector)
-- ==========================================

-- Enable Vector extension for semantic similarity search in RAG
CREATE EXTENSION IF NOT EXISTS vector WITH SCHEMA public;

-- 1. USERS TABLE (Syncs automatically with Supabase Auth users)
CREATE TABLE IF NOT EXISTS public.users (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255),
    avatar_url VARCHAR(1024),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- Trigger Function to sync Auth Users into Public Users Table
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.users (id, email, full_name, avatar_url)
    VALUES (
        new.id,
        new.email,
        coalesce(new.raw_user_meta_data->>'full_name', ''),
        coalesce(new.raw_user_meta_data->>'avatar_url', '')
    );
    RETURN new;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger definition
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- 2. DOCUMENTS TABLE
CREATE TABLE IF NOT EXISTS public.documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    title VARCHAR(512) NOT NULL,
    file_path VARCHAR(1024) NOT NULL,
    file_url VARCHAR(2048) NOT NULL,
    file_size INT NOT NULL,
    page_count INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- 3. PDF_CHUNKS TABLE (For simple RAG matching)
CREATE TABLE IF NOT EXISTS public.pdf_chunks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id UUID NOT NULL REFERENCES public.documents(id) ON DELETE CASCADE,
    page_number INT NOT NULL,
    chunk_content TEXT NOT NULL,
    embedding VECTOR(768), -- Gemini 768-dim text-embedding-004
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- Cosine Similarity Index
CREATE INDEX IF NOT EXISTS pdf_chunks_embedding_idx ON public.pdf_chunks 
USING hnsw (embedding vector_cosine_ops);

-- 4. BOOKMARKS TABLE
CREATE TABLE IF NOT EXISTS public.bookmarks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id UUID NOT NULL REFERENCES public.documents(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    page_number INT NOT NULL,
    label VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,
    UNIQUE(document_id, user_id, page_number)
);

-- 5. NOTES TABLE (Saves highlights and page margin annotations)
CREATE TABLE IF NOT EXISTS public.notes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id UUID NOT NULL REFERENCES public.documents(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    page_number INT NOT NULL,
    content TEXT NOT NULL,
    color_code VARCHAR(10) DEFAULT '#FFEB3B',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- 6. FLASHCARDS TABLE (Spaced Repetition SM-2 ready)
CREATE TABLE IF NOT EXISTS public.flashcards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id UUID REFERENCES public.documents(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    front TEXT NOT NULL,
    back TEXT NOT NULL,
    difficulty FLOAT DEFAULT 2.5,
    repetitions INT DEFAULT 0,
    interval INT DEFAULT 0,
    next_review TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- 7. QUIZZES TABLE
CREATE TABLE IF NOT EXISTS public.quizzes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id UUID REFERENCES public.documents(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    title VARCHAR(512) NOT NULL,
    questions JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- 8. QUIZ_RESULTS TABLE
CREATE TABLE IF NOT EXISTS public.quiz_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quiz_id UUID NOT NULL REFERENCES public.quizzes(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    score INT NOT NULL,
    total_questions INT NOT NULL,
    answers_submitted JSONB NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- 9. CHAT_SESSIONS TABLE
CREATE TABLE IF NOT EXISTS public.chat_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id UUID NOT NULL REFERENCES public.documents(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    title VARCHAR(255) DEFAULT 'New Conversation',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- 10. CHAT_MESSAGES TABLE
CREATE TABLE IF NOT EXISTS public.chat_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL REFERENCES public.chat_sessions(id) ON DELETE CASCADE,
    sender VARCHAR(10) CHECK (sender IN ('user', 'ai')),
    message TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- ==========================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ==========================================
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.documents ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pdf_chunks ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.bookmarks ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.notes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.flashcards ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.quizzes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.quiz_results ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.chat_sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.chat_messages ENABLE ROW LEVEL SECURITY;

-- Select Policies
CREATE POLICY user_self_read ON public.users FOR SELECT USING (auth.uid() = id);
CREATE POLICY doc_user_all ON public.documents FOR ALL USING (auth.uid() = user_id);
CREATE POLICY chunk_user_read ON public.pdf_chunks FOR SELECT USING (
    document_id IN (SELECT id FROM public.documents WHERE user_id = auth.uid())
);
CREATE POLICY bookmark_user_all ON public.bookmarks FOR ALL USING (auth.uid() = user_id);
CREATE POLICY note_user_all ON public.notes FOR ALL USING (auth.uid() = user_id);
CREATE POLICY flashcard_user_all ON public.flashcards FOR ALL USING (auth.uid() = user_id);
CREATE POLICY quiz_user_all ON public.quizzes FOR ALL USING (auth.uid() = user_id);
CREATE POLICY quiz_res_user_all ON public.quiz_results FOR ALL USING (auth.uid() = user_id);
CREATE POLICY chat_sess_user_all ON public.chat_sessions FOR ALL USING (auth.uid() = user_id);
CREATE POLICY chat_msg_user_all ON public.chat_messages FOR ALL USING (
    session_id IN (SELECT id FROM public.chat_sessions WHERE user_id = auth.uid())
);

-- ==========================================
-- MATCH PDF CHUNKS FUNCTION FOR RAG PIPELINE
-- ==========================================
CREATE OR REPLACE FUNCTION match_pdf_chunks (
  query_embedding vector(768),
  match_threshold float,
  match_count int,
  filter_document_id uuid
)
RETURNS TABLE (
  id uuid,
  document_id uuid,
  page_number int,
  chunk_content text,
  similarity float
)
LANGUAGE plpgsql
AS $$
BEGIN
  RETURN QUERY
  SELECT
    pdf_chunks.id,
    pdf_chunks.document_id,
    pdf_chunks.page_number,
    pdf_chunks.chunk_content,
    1 - (pdf_chunks.embedding <=> query_embedding) AS similarity
  FROM pdf_chunks
  WHERE pdf_chunks.document_id = filter_document_id
    AND 1 - (pdf_chunks.embedding <=> query_embedding) > match_threshold
  ORDER BY pdf_chunks.embedding <=> query_embedding
  LIMIT match_count;
END;
$$;
