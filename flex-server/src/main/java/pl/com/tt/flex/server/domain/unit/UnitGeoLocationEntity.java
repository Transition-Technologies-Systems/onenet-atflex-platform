package pl.com.tt.flex.server.domain.unit;


import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

/**
 * A UnitGeoLocationEntity.
 */
@Getter
@Setter
@Entity
@Table(name = "unit_geo_location")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@GenericGenerator(
    name = "unit_geo_location_id_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "unit_geo_location_seq"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
    }
)
public class UnitGeoLocationEntity extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "unit_geo_location_id_generator")
    private Long id;

    /**
     * e.g. 52.24423404202796
     */
    @NotNull
    @Size(max = 20)
    @Column(name = "latitude", length = 20, nullable = false)
    private String latitude;

    /**
     * e.g. 20.982350812202593
     */
    @NotNull
    @Size(max = 20)
    @Column(name = "longitude", length = 20, nullable = false)
    private String longitude;

    @Column(name = "main_location", length = 50)
    private boolean mainLocation;

    @ManyToOne
    @JoinColumn(name = "unit_id")
//    @JsonIgnoreProperties(value = "geoLocations", allowSetters = true)
    private UnitEntity unit;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UnitGeoLocationEntity)) {
            return false;
        }
        return Objects.nonNull(id) && Objects.equals(id, ((UnitGeoLocationEntity) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
