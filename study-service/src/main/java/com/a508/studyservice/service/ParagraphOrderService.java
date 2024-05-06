package com.a508.studyservice.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.a508.studyservice.dto.response.ProblemResponse;

public interface ParagraphOrderService {


	List<ProblemResponse> getParagraphProblems(String token , LocalDateTime date );


}
