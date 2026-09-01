package com.dowinn.rouletto.controller;


import com.dowinn.rouletto.communication.APIResponse;
import com.dowinn.rouletto.dto.api.request.GameHistoryRequestDto;
import com.dowinn.rouletto.dto.api.response.GameHistoryResponseDto;
import com.dowinn.rouletto.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class HistoryController {


    @Autowired
    HistoryService historyService;

    @PostMapping("/player")
    public ResponseEntity<APIResponse> getPlayerHistory(@RequestBody GameHistoryRequestDto gameHistoryRequestDto){
        return ResponseEntity.ok(historyService.getPlayerHistory(gameHistoryRequestDto));
    }


}
