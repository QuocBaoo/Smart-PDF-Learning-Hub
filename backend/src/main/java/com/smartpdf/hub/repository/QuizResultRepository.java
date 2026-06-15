package com.smartpdf.hub.repository;

import com.smartpdf.hub.model.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface QuizResultRepository extends JpaRepository<QuizResult, UUID> {

    List<QuizResult> findByUserIdOrderByCompletedAtDesc(UUID userId);

    List<QuizResult> findByQuizIdAndUserId(UUID quizId, UUID userId);

    // Tinh diem trung binh cua nguoi dung tren tat ca cac bai quiz
    @Query("SELECT COALESCE(AVG(r.score * 100.0 / r.totalQuestions), 0) FROM QuizResult r WHERE r.user.id = :userId")
    Double getAverageScorePercentageByUserId(@Param("userId") UUID userId);
}
