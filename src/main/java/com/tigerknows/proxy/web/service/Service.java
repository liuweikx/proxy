package com.tigerknows.proxy.web.service;

import java.util.Map;

import com.tigerknows.proxy.web.answer.Answer;


public interface Service {
    
    void registerSelf();
    
    void init();

    Answer getAnswer(Map<String, String> requestParams) throws Exception;
}
