package pl.com.tt.flex.model.service.dto.kdm_model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class KdmModelTimestampsMinimalDTO implements Serializable {

    private String timestamp;
    private List<String> stations = new ArrayList<>();
}
