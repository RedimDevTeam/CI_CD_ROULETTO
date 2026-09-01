package com.dowinn.rouletto.controller;


import com.dowinn.rouletto.dto.api.request.GameCancelRequestDto;
import com.dowinn.rouletto.dto.api.request.GameDealRequestDto;
import com.dowinn.rouletto.dto.api.request.GameStartRequestDto;
import com.dowinn.rouletto.model.GameDetail;
import com.dowinn.rouletto.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/game")
public class GameController {

    @Autowired
    GameService gameService;

    @PostMapping("/start")
    public ResponseEntity<GameDetail> start(@RequestBody GameStartRequestDto gameStartRequestDto) {
        GameDetail gameDetail = gameService.startGame(gameStartRequestDto);
        return ResponseEntity.ok(gameDetail);
    }

    @PostMapping("/deal")
    public ResponseEntity<String> deal(@RequestBody GameDealRequestDto gameDealRequestDto) {
        gameService.dealBall(gameDealRequestDto);
        return ResponseEntity.ok("game completed ");
    }

    @PostMapping("/cancel")
    public ResponseEntity<String> cancel(@RequestBody GameCancelRequestDto gameCancelRequestDto) {
        gameService.cancelGame(gameCancelRequestDto,true);
        return ResponseEntity.ok("game cancelled");
    }

    @PostMapping("/spinball")
    public ResponseEntity<String> spinBall(@RequestBody GameStartRequestDto gameStartRequestDto){
        gameService.spinBallRecord(gameStartRequestDto);
        return ResponseEntity.ok("Spin Ball vide recorded");
    }

}
