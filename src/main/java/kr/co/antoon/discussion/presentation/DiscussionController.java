package kr.co.antoon.discussion.presentation;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import kr.co.antoon.common.dto.PageDto;
import kr.co.antoon.common.dto.ResponseDto;
import kr.co.antoon.common.dto.SwaggerNote;
import kr.co.antoon.discussion.application.DiscussionService;
import kr.co.antoon.discussion.dto.request.DiscussionCreateRequest;
import kr.co.antoon.discussion.dto.request.DiscussionUpdateRequest;
import kr.co.antoon.discussion.dto.response.DiscussionCreateResponse;
import kr.co.antoon.discussion.dto.response.DiscussionReadResponse;
import kr.co.antoon.discussion.dto.response.DiscussionUpdateResponse;
import kr.co.antoon.discussion.facade.DiscussionFacade;
import kr.co.antoon.oauth.config.AuthUser;
import kr.co.antoon.oauth.dto.AuthInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "종목토론방 API")
@RestController
@RequestMapping(value = "/api/v1/webtoons")
@RequiredArgsConstructor
public class DiscussionController {
    private final DiscussionFacade discussionFacade;
    private final DiscussionService discussionService;

    @ApiOperation(value = "종목토론방 댓글 달기 API", notes = SwaggerNote.DISCUSSION_CREATE_NOTE)
    @PostMapping("/{webtoonId}/discussions")
    public ResponseEntity<DiscussionCreateResponse> create(
            @PathVariable Long webtoonId,
            @Validated @RequestBody DiscussionCreateRequest request,
            @AuthUser AuthInfo info
            ) {
        return ResponseDto.created(discussionFacade.register(info.userId(), webtoonId, request));
    }

    @ApiOperation(value = "종목토론방 댓글 단건 조회", notes = SwaggerNote.DISCUSSION_READ_ONE_NOTE)
    @GetMapping("/discussions/{discussionId}")
    public ResponseEntity<DiscussionReadResponse> findOne(
            @PathVariable Long discussionId,
            @AuthUser AuthInfo info
    ) {
        var response = discussionFacade.findById(info.userId(), discussionId);
        return ResponseDto.ok(response);
    }

    @ApiOperation(value = "종목토론방 댓글 페이지 조회", notes = SwaggerNote.DISCUSSION_READL_ALL_NOTE)
    @GetMapping("/discussions")
    public ResponseEntity<PageDto<DiscussionReadResponse>> findAll(
            @PageableDefault(size = 20, page = 0, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthUser AuthInfo info
    ) {
        var response = discussionFacade.findAll(info.userId(), pageable);
        return PageDto.ok(response);
    }

    @ApiOperation(value = "종목토론방 댓글 수정", notes = SwaggerNote.DISCUSSION_UPDATE_NOTE)
    @PatchMapping("/discussions/{discussionId}")
    public ResponseEntity<DiscussionUpdateResponse> update(
            @PathVariable Long discussionId,
            @Validated @RequestBody DiscussionUpdateRequest request,
            @AuthUser AuthInfo info
    ) {
        var response = discussionFacade.update(info.userId(), discussionId, request);
        return ResponseDto.ok(response);
    }

    @ApiOperation(value = "종목토론방 댓글 삭제", notes = SwaggerNote.DISCUSSION_DELETE_NOTE)
    @DeleteMapping("/discussions/{discussionId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long discussionId,
            @AuthUser AuthInfo info
    ) {
        discussionService.delete(discussionId);
        return ResponseDto.noContent();
    }
}