package daelim.project.eatstagram.service.contentFile.dsl;

import daelim.project.eatstagram.service.contentFile.ContentFileDTO;

import java.util.List;

public interface ContentFileDslRepository {

    List<ContentFileDTO> getListByContentId(String contentId);
}
