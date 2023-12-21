package pl.com.tt.flex.model.service.dto.kdm_model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class KdmAreaDTO implements Serializable {

    private Long id;

    private String areaName;

    private boolean lvModel;
}