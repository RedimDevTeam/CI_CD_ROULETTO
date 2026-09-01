package com.dowinn.rouletto.controller;


import com.dowinn.rouletto.service.BoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bo")
public class BoController {

    @Autowired
    BoService boService;

    @GetMapping("/update/timer/{table}")
    public ResponseEntity<String> updateTimer(@PathVariable(name = "table") String tableId){
        boService.updateTimer(tableId);
        return ResponseEntity.ok("timer updated");
    }

    @GetMapping("/update/jackpot/{casino}")
    public ResponseEntity<String> updateJackPot(@PathVariable String casino){
        boService.updateJackPot(casino);
        return ResponseEntity.ok("jackPot updated for casino");
    }
}
