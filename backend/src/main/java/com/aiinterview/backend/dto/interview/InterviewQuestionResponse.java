    package com.aiinterview.backend.dto.interview;

    public class InterviewQuestionResponse {

        private String evaluation;
        private String nextQuestion;
        private Integer currentQuestion;
        private Integer totalQuestions;
        private Integer score;
        private Boolean completed;

        public InterviewQuestionResponse() {
        }

        public InterviewQuestionResponse(String evaluation,
                                        String nextQuestion,
                                        Integer currentQuestion,
                                        Integer totalQuestions,
                                        Integer score,
                                        Boolean completed) {
            this.evaluation = evaluation;
            this.nextQuestion = nextQuestion;
            this.currentQuestion = currentQuestion;
            this.totalQuestions = totalQuestions;
            this.score = score;
            this.completed = completed;
        }

        public String getEvaluation() {
            return evaluation;
        }

        public void setEvaluation(String evaluation) {
            this.evaluation = evaluation;
        }

        public String getNextQuestion() {
            return nextQuestion;
        }

        public void setNextQuestion(String nextQuestion) {
            this.nextQuestion = nextQuestion;
        }

        public Integer getCurrentQuestion() {
            return currentQuestion;
        }

        public void setCurrentQuestion(Integer currentQuestion) {
            this.currentQuestion = currentQuestion;
        }

        public Integer getTotalQuestions() {
            return totalQuestions;
        }

        public void setTotalQuestions(Integer totalQuestions) {
            this.totalQuestions = totalQuestions;
        }

        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
        }

        public Boolean getCompleted() {
            return completed;
        }

        public void setCompleted(Boolean completed) {
            this.completed = completed;
        }
    }