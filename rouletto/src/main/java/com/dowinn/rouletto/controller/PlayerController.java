package com.dowinn.rouletto.controller;


import com.dowinn.rouletto.communication.APIResponse;
import com.dowinn.rouletto.dto.api.request.DeleteRequestDto;
import com.dowinn.rouletto.dto.api.request.SaveBetRequestDto;
import com.dowinn.rouletto.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/save/bet")
public class PlayerController {

    @Autowired
    HistoryService historyService;


    @PostMapping("/player")
    public ResponseEntity<APIResponse> saveBet(@RequestHeader("Authorization") String authHeader, @RequestBody SaveBetRequestDto saveBetRequestDto){
        return ResponseEntity.ok(historyService.saveBets(saveBetRequestDto,authHeader));
    }

    @PostMapping("/player/savebets")
    public ResponseEntity<APIResponse> getBet(@RequestHeader("Authorization") String authHeader,@RequestBody SaveBetRequestDto saveBetRequestDto){
        return ResponseEntity.ok(historyService.getSavedBets(authHeader,saveBetRequestDto.getTableId()));
    }

    @DeleteMapping("/player")
    public ResponseEntity<APIResponse> deleteBet(@RequestHeader("Authorization") String authHeader, @RequestBody SaveBetRequestDto saveBetRequestDto){
        return ResponseEntity.ok(historyService.deleteSavedBets(authHeader,saveBetRequestDto));
    }


}
