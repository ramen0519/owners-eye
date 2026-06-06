package com.ownerseye.ownerseye.domain.insight.domain.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

public class ToolChoiceAdvisor implements CallAdvisor {

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        List<Message> messages = request.prompt().getInstructions();
        boolean hasToolResponse = messages.stream().anyMatch(m -> m instanceof ToolResponseMessage);

        if (hasToolResponse) {
            Prompt modified = new Prompt(messages, ChatOptions.builder().temperature(0.3).build());
            request = ChatClientRequest.builder()
                    .prompt(modified)
                    .context(request.context())
                    .build();
        }

        return chain.nextCall(request);
    }

    @Override
    public String getName() {
        return ToolChoiceAdvisor.class.getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
