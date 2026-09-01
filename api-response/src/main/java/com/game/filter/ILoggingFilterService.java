package com.game.filter;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface ILoggingFilterService {

    Map<String,String> getTokenData(HttpServletRequest request);
}
