package pl.com.tt.flex.server.service.subportfolio.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.server.domain.subportfolio.SubportfolioEntity;

import java.io.Serializable;
import java.time.Instant;

/**
 * A minimal DTO for the {@link SubportfolioEntity} entity.
 */
@Getter
@Setter
@NoArgsConstructor
public class SubportfolioMinDTO implements Serializable {

    private Long id;
    private String name;
    private Instant createdDate;

    public SubportfolioMinDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public SubportfolioMinDTO(Long id, Instant createdDate) {
        this.id = id;
        this.createdDate = createdDate;
    }
}
