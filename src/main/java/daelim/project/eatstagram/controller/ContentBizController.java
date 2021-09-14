package daelim.project.eatstagram.controller;

import daelim.project.eatstagram.security.dto.AuthMemberDTO;
import daelim.project.eatstagram.service.biz.ContentBizService;
import daelim.project.eatstagram.service.content.ContentDTO;
import daelim.project.eatstagram.service.contentFile.ContentFileService;
import daelim.project.eatstagram.service.contentLike.ContentLikeDTO;
import daelim.project.eatstagram.service.contentLike.ContentLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/content")
@Slf4j
@RequiredArgsConstructor
public class ContentBizController {

    private final ContentBizService contentBizService;
    private final ContentFileService contentFileService;
    private final ContentLikeService contentLikeService;

    // 콘텐츠 페이징 리스트
    @RequestMapping("/getPagingList")
    @ResponseBody
    public Page<ContentDTO> getPagingList(Pageable pageable, @AuthenticationPrincipal AuthMemberDTO authMemberDTO) {
        return contentBizService.getContentPagingList(pageable, authMemberDTO.getUsername());
    }

    // 콘텐츠 추가
    @RequestMapping("/add")
    public ResponseEntity<String> add(@ModelAttribute ContentDTO contentDTO, MultipartFile[] uploadFiles) {
        return contentBizService.contentAdd(contentDTO, uploadFiles);
    }

    // 비디오 스트림
    @RequestMapping(value = "/stream/{contentName}")
    public void stream(@PathVariable("contentName")String contentName, HttpServletRequest request, HttpServletResponse response) throws Exception {
        contentFileService.videoStream(contentName, request, response);
    }

    @RequestMapping("/like/save")
    @ResponseBody
    public ResponseEntity<String> likeSave(@ModelAttribute ContentLikeDTO contentLikeDTO) {
        return contentLikeService.save(contentLikeDTO);
    }
}