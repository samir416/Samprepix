package com.aiinterview.backend.service.coding;

import com.aiinterview.backend.entity.CodingProblem;
import com.aiinterview.backend.entity.CodingProblemCompletion;
import com.aiinterview.backend.entity.User;

import java.util.List;

public interface CodingProblemCompletionService {

    CodingProblemCompletion getCompletion(
            User user,
            CodingProblem problem
    );

    CodingProblemCompletion getOrCreateCompletion(
            User user,
            CodingProblem problem
    );

    CodingProblemCompletion recordSubmission(
            User user,
            CodingProblem problem,
            String language,
            String code,
            boolean successful
    );

    CodingProblemCompletion markCompleted(
            User user,
            CodingProblem problem,
            String language,
            String code
    );

    List<CodingProblemCompletion> getUserCompletions(
            User user
    );

    List<CodingProblemCompletion> getCompletedProblems(
            User user
    );

    boolean isProblemCompleted(
            User user,
            CodingProblem problem
    );

    long getCompletedProblemCount(
            User user
    );
}