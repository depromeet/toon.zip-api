package kr.co.antoon.vote.presentation;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import kr.co.antoon.common.dto.ResponseDto;
import kr.co.antoon.vote.dto.response.TopicResponse;
import kr.co.antoon.vote.facade.TopicFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "투표 상세 페이지 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/topics")
public class TopicController {
    private final TopicFacade topicFacade;

    @ApiOperation(value = "투표 상세 페이지 Topic 조회")
    @GetMapping("/{topicId}")
    public ResponseEntity<TopicResponse> getTopicById(
            @PathVariable("topicId") Long topicId
    ) {
        var response = topicFacade.findTopicById(topicId);
        return ResponseDto.ok(response);
    }
}