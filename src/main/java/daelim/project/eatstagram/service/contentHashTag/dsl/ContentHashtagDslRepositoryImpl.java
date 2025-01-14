package daelim.project.eatstagram.service.contentHashTag.dsl;

import com.querydsl.core.types.Projections;
import daelim.project.eatstagram.service.contentHashTag.ContentHashtagDTO;
import daelim.project.eatstagram.service.contentHashTag.QContentHashtagEntity;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

import static daelim.project.eatstagram.service.contentHashTag.QContentHashtagEntity.contentHashtagEntity;

public class ContentHashtagDslRepositoryImpl extends QuerydslRepositorySupport implements ContentHashtagDslRepository {

    public ContentHashtagDslRepositoryImpl() {
        super(QContentHashtagEntity.class);
    }

    @Override
    public List<ContentHashtagDTO> getListByContentId(String contentId) {
        return from(contentHashtagEntity)
                .where(contentHashtagEntity.contentId.eq(contentId))
                .select(Projections.bean(ContentHashtagDTO.class,
                        contentHashtagEntity.contentHashtagId,
                        contentHashtagEntity.hashtag))
                .fetch();
    }
}
