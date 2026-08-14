package com.aiinterview.backend.service.coding;

import com.aiinterview.backend.entity.CodingProblem;
import com.aiinterview.backend.entity.CodingProgress;
import com.aiinterview.backend.entity.User;

public interface CodingProgressService {

    CodingProgress getProgress(User user);

    CodingProgress getOrCreateProgress(User user);

    CodingProgress saveCurrentProblem(
            User user,
            CodingProblem problem
    );

    CodingProgress saveLastSelectedProblem(
            User user,
            CodingProblem problem
    );

    CodingProgress saveCodeState(
            User user,
            CodingProblem problem,
            String language,
            String code
    );

    CodingProgress markProblemCompleted(
            User user,
            CodingProblem problem
    );

    CodingProgress updateSubmission(
            User user,
            boolean successful
    );
}