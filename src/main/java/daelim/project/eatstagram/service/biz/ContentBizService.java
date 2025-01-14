package daelim.project.eatstagram.service.biz;

import daelim.project.eatstagram.service.content.ContentDTO;
import daelim.project.eatstagram.service.content.ContentService;
import daelim.project.eatstagram.service.contentCategory.ContentCategoryDTO;
import daelim.project.eatstagram.service.contentCategory.ContentCategoryService;
import daelim.project.eatstagram.service.contentFile.ContentFileDTO;
import daelim.project.eatstagram.service.contentFile.ContentFileService;
import daelim.project.eatstagram.service.contentHashTag.ContentHashtagDTO;
import daelim.project.eatstagram.service.contentHashTag.ContentHashtagService;
import daelim.project.eatstagram.service.contentLike.ContentLikeService;
import daelim.project.eatstagram.storage.StorageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentBizService {

    private final ContentService contentService;
    private final ContentFileService contentFileService;
    private final ContentHashtagService contentHashtagService;
    private final ContentCategoryService contentCategoryService;
    private final ContentLikeService contentLikeService;
    private final StorageRepository storageRepository;

    // 콘텐츠 페이징 리스트
    public Page<ContentDTO> getContentPagingList(Pageable pageable, String username) {
        Page<ContentDTO> pagingList = contentService.getRepository().getPagingList(pageable);
        for (ContentDTO contentDTO : pagingList) {
            List<ContentFileDTO> contentFileList = contentFileService.getRepository().getListByContentId(contentDTO.getContentId());
            List<ContentHashtagDTO> contentHashtagList = contentHashtagService.getRepository().getListByContentId(contentDTO.getContentId());
            List<ContentCategoryDTO> contentCategoryList = contentCategoryService.getRepository().getListByContentId(contentDTO.getContentId());
            long likeCount = contentLikeService.getRepository().countByContentId(contentDTO.getContentId());
            boolean likeCheck = contentLikeService.getRepository().findByUsernameAndContentId(username, contentDTO.getContentId()) != null;
            contentDTO.setContentFileDTOList(contentFileList);
            contentDTO.setContentHashtagDTOList(contentHashtagList);
            contentDTO.setContentCategoryDTOList(contentCategoryList);
            contentDTO.setLikeCount(likeCount);
            contentDTO.setLikeCheck(likeCheck);
        }
        return pagingList;
    }

    // 콘텐츠 저장
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<String> contentAdd(ContentDTO contentDTO, MultipartFile[] uploadFiles) {
        if (uploadFiles.length <= 0 || contentDTO.getContentCategoryDTOList().size() <= 0 || StringUtils.isEmpty(contentDTO.getText()))
            return new ResponseEntity<>("{\"response\": \"error\"}", HttpStatus.OK);

        ContentDTO result = contentService.save(contentDTO);
        for (ContentHashtagDTO contentHashtagDTO : contentDTO.getContentHashtagDTOList()) {
            contentHashtagDTO.setContentId(result.getContentId());
            contentHashtagService.save(contentHashtagDTO);
        }
        for (ContentCategoryDTO contentCategoryDTO : contentDTO.getContentCategoryDTOList()) {
            contentCategoryDTO.setContentId(result.getContentId());
            contentCategoryService.save(contentCategoryDTO);
        }

        Path folderPath = storageRepository.makeFolder(ContentFileService.CONTENT_FILE_FOLDER_NAME);

        for (MultipartFile uploadFile : uploadFiles) {

            String fileType = uploadFile.getContentType();
            assert fileType != null;
            // "이미지" 와 "동영상" 파일만 업로드 가능
            if (!(fileType.startsWith("image") || fileType.startsWith("video"))) {
                log.warn("this file is not image or video type");
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return new ResponseEntity<>("{\"response\": \"error\"}", HttpStatus.OK);
            }

            // 실제 파일 이름 IE 나 Edge 는 전체 경로가 들어오므로
            String originalName = uploadFile.getOriginalFilename();
            assert originalName != null;
            String fileName = originalName.substring(originalName.lastIndexOf("\\") + 1);

            String uuid = UUID.randomUUID().toString();
            fileName =  uuid + "_" + fileName;
            String saveName = folderPath + File.separator + fileName;
            Path savePath = Paths.get(saveName);

            ContentFileDTO contentFileDTO = ContentFileDTO.builder()
                    .name(fileName)
                    .type(fileType)
                    .path(savePath.normalize().toAbsolutePath().toString())
                    .contentId(result.getContentId())
                    .build();
            contentFileService.save(contentFileDTO);

            try {
                uploadFile.transferTo(savePath);
            } catch (IOException e) {
                e.printStackTrace();
                return new ResponseEntity<>("{\"response\": \"error\"}", HttpStatus.OK);
            }
        }
        return new ResponseEntity<>("{\"response\": \"ok\"}", HttpStatus.OK);
    }
}
