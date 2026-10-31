package com.devforge.web;

import com.devforge.service.BoardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final BoardService boardService;

    public DashboardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return boardService.dashboard();
    }
}
