package pl.com.tt.flex.model.service.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class MinimalDTO<ID extends Serializable, T extends Serializable> implements Serializable{

    private ID id;
    private T value;
}
