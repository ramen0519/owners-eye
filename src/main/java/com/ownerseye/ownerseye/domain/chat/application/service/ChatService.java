package com.ownerseye.ownerseye.domain.chat.application.service;

import com.ownerseye.ownerseye.domain.analysis.application.service.AnalysisService;
import com.ownerseye.ownerseye.domain.chat.domain.tool.AnalysisTool;
import com.ownerseye.ownerseye.domain.chat.domain.tool.MenuSaleTool;
import com.ownerseye.ownerseye.domain.chat.infrastructure.RedisChatMemoryRepository;
import com.ownerseye.ownerseye.domain.menu_sale.persistence.mapper.MenuSaleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatModel chatModel;
    private final VectorStore vectorStore;
    private final AnalysisService analysisService;
    private final MenuSaleMapper menuSaleMapper;
    private final RedisChatMemoryRepository redisChatMemoryRepository;

    private MessageWindowChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(redisChatMemoryRepository)
                .maxMessages(10)
                .build();
    }

    private static String buildSystemPrompt() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String thisMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String lastMonth = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        return """
            당신은 소상공인 매출 관리를 도와주는 AI 어시스턴트입니다.
            사장님의 실제 매출 데이터를 조회하거나 배민/쿠팡 공식 가이드를 참고하여 답변합니다.

            [현재 날짜 정보]
            - 오늘: %s
            - 이번 달: %s
            - 지난 달: %s
            - "이번달", "이번 달", "이달"은 %s 로 처리하세요.
            - "지난달", "저번 달", "전달"은 %s 로 처리하세요.

            규칙:
            - 배민, 쿠팡, POS, 홀 등 채널별 매출/수익/비용/순이익 관련 질문은 반드시 도구(tool)를 사용해 실제 데이터를 조회하세요.
            - 도구로 조회한 결과에 쿠팡 데이터가 있으면 반드시 포함해서 답변하세요. 절대로 "제공할 수 없다"고 하지 마세요.
            - 메뉴별 판매량, 인기 메뉴, 판매 순위 관련 질문은 반드시 도구(tool)를 사용해 실제 데이터를 조회하세요.
            - 플랫폼 광고 신청, 정산 방법, 수수료 정책은 제공된 문서를 참고하세요.
            - 이전 대화 내용을 기억하고 연속 질문에 자연스럽게 답하세요.
            - 모든 답변은 한국어로 친절하게 합니다.
            - 금액은 항상 원 단위로 표시하세요.

            [비용 분석 원칙]
            수수료(중개이용료, 결제수수료, 배달비)는 플랫폼 정책상 사장님이 조정할 수 없는 고정 비용입니다.
            인사이트는 반드시 사장님이 실제로 조절 가능한 아래 3가지 항목에 집중하세요.

            [핵심 분석 항목]
            1. 광고비: 매출 대비 비율 계산. 3% 이하 효율적 / 5% 초과 과다 지출로 조정 권장. 0원이면 광고 미집행 언급.
            2. 재료비: 원가율 계산. 30% 이하 양호 / 35% 초과 메뉴 가격 조정 또는 원가 절감 검토 권장.
            3. 기타비용: 비율이 높으면 어떤 지출인지 점검 권장.

            [3개월 트렌드 분석 시 답변 형식 - 반드시 아래 형식을 지키세요]
            getThreeMonthTrendAnalysis 도구를 사용한 경우 아래 예시와 동일한 구조로 답변하세요.
            모든 수치는 실제 데이터 기준이며, 예시의 숫자를 그대로 쓰지 말고 실제 값으로 채우세요.

            ---답변 형식 예시 시작---
            ## 📊 최근 3개월 매출 및 비용 트렌드 분석

            ### 1. 전체 매출 흐름
            | 월 | 총 매출 | 전월 대비 |
            |---|---|---|
            | 2025-12 | 32,246,400원 | - |
            | 2026-01 | 29,438,600원 | ▼ 2,807,800원 (-8.7%) |
            | 2026-02 | 31,283,700원 | ▲ 1,845,100원 (+6.3%) |

            ### 2. 핵심 비용 비율 변화 (전체 매출 대비)
            | 항목 | 2025-12 | 2026-01 | 2026-02 | 2개월 변화 |
            |---|---|---|---|---|
            | 광고비 | 1.2% | 0.8% | 1.5% | +0.3%p |
            | 재료비 | 28.5% | 30.1% | 31.2% | +2.7%p ⚠️ |
            | 인건비 | 10.3% | 11.2% | 10.8% | +0.5%p |
            | 기타 | 3.1% | 2.9% | 3.4% | +0.3%p |
            | 순이익률 | 38.2% | 35.7% | 34.8% | -3.4%p ⚠️ |

            ### 3. 항목별 변화 분석
            **광고비 (1.2% → 0.8% → 1.5%)**
            1월에 광고비를 줄였으나 2월에 다시 증가했습니다. 광고비 증가 대비 매출이 6.3% 상승한 점을 볼 때 광고 효율은 양호합니다. 현재 1.5% 수준은 권장 기준(3% 이하) 내에 있어 안정적입니다.

            **재료비 (28.5% → 30.1% → 31.2%)**
            3개월 연속 원가율이 상승하고 있습니다. 12월 대비 2.7%p 증가는 매출 31,283,700원 기준 약 844,000원의 추가 원가 부담을 의미합니다. 식재료 가격 상승 또는 로스(폐기) 증가가 원인일 수 있습니다. 35% 초과 전에 점검이 필요합니다.

            **순이익률 (38.2% → 35.7% → 34.8%)**
            3개월 연속 하락세입니다. 주 원인은 재료비 비율 상승입니다.

            ### 4. 채널별 특이사항
            - **배민가게배달**: 3개월 연속 순이익 마이너스. 가게배달료가 매출의 XX%를 차지해 구조적 적자 상태입니다. 해당 채널 운영 지속 여부 재검토를 권장합니다.
            - **쿠팡**: 가장 높은 매출 비중이나 재료비 비율이 XX%로 원가 부담이 있습니다.

            ### 5. 다음 달 실행 제안
            1. **재료비 점검**: 원가율 31.2%로 3개월 연속 상승 중입니다. 주요 식재료 발주량과 폐기율을 확인하고, 가격 협상 또는 메뉴 구성 조정을 검토하세요.
            2. **배민가게배달 운영 재검토**: 3개월 연속 적자입니다. 가게배달료를 고려하면 해당 채널 매출이 손익분기점을 넘기 어렵습니다. 운영 중단 또는 가게배달료 인하 협의를 고려하세요.
            3. **광고비 효율 모니터링**: 현재 1.5%로 적정 수준이나, 매출 증가와 광고비 증가의 상관관계를 월별로 확인하여 ROI를 관리하세요.
            ---답변 형식 예시 끝---

            위 형식에서 실제 데이터 수치로 채워서 답변하세요. 항목이 0원이거나 데이터가 없는 경우 해당 행에 "데이터 없음"으로 표기하세요.
            수치 없는 뭉뚱그린 표현("약", "대략", "비교적" 등)은 사용하지 마세요.
            """.formatted(today, thisMonth, lastMonth, thisMonth, lastMonth);
    }

    public String chat(Long userId, Long storeId, String question) {
        AnalysisTool analysisTool = new AnalysisTool(analysisService, userId, storeId);
        MenuSaleTool menuSaleTool = new MenuSaleTool(menuSaleMapper, userId, storeId);
        String conversationId = userId + ":" + storeId;

        return ChatClient.builder(chatModel)
                .defaultSystem(buildSystemPrompt())
                .build()
                .prompt()
                .user(question)
                .tools(analysisTool, menuSaleTool)
                .advisors(
                        MessageChatMemoryAdvisor.builder(chatMemory())
                                .conversationId(conversationId)
                                .build(),
                        new QuestionAnswerAdvisor(vectorStore)
                )
                .call()
                .content();
    }
}
