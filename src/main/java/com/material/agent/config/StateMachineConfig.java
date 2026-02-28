package com.material.agent.config;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.EnumSet;

@Configuration
public class StateMachineConfig {
    
    @Bean
    public StateMachine<AgentState, AgentEvent> stateMachine() throws Exception {
        StateMachineBuilder.Builder<AgentState, AgentEvent> builder = StateMachineBuilder.builder();
        
        builder.configureStates()
            .withStates()
            .initial(AgentState.INTENT_RECOGNIZED)
            .states(EnumSet.allOf(AgentState.class))
            .end(AgentState.RESPONSE_GENERATED)
            .end(AgentState.ERROR);
        
        builder.configureTransitions()
            .withExternal()
                .source(AgentState.INTENT_RECOGNIZED)
                .target(AgentState.PARAM_EXTRACTED)
                .event(AgentEvent.EXTRACT_PARAM)
            .and()
            .withExternal()
                .source(AgentState.PARAM_EXTRACTED)
                .target(AgentState.DATA_QUERY)
                .event(AgentEvent.QUERY_DATA)
            .and()
            .withExternal()
                .source(AgentState.DATA_QUERY)
                .target(AgentState.RESULT_VERIFIED)
                .event(AgentEvent.VERIFY_RESULT)
            .and()
            .withExternal()
                .source(AgentState.RESULT_VERIFIED)
                .target(AgentState.RESPONSE_GENERATED)
                .event(AgentEvent.GENERATE_RESPONSE);
        
        return builder.build();
    }
}
