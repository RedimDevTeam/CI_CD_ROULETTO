package com.dowinn.rouletto.service;


import com.dowinn.rouletto.entity.TableConfig;
import com.dowinn.rouletto.redis.RedisHelper;
import com.dowinn.rouletto.repository.TableConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BoService {

    @Autowired
    RedisHelper redisHelper;

    @Autowired
    TableConfigRepository tableConfigRepository;

    @Autowired
    JackPotService jackPotService;

    public void updateTimer(String tableId){
        TableConfig tableConfig = tableConfigRepository.findByTableIdAndActive(tableId, true);
        redisHelper.setTableConfig(tableId,tableConfig);
    }

    public void updateJackPot(String casino){
          jackPotService.updateCasinoJackPot(casino);
    }

}
