package daelim.project.eatstagram.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage")
@Getter @Setter
public class StorageProperties {

    //private String location = "C:/Eatstagram/public/";
    private String location = "C:/daelim/Project/EATSTAGRAM_FRONT/public/";

}
