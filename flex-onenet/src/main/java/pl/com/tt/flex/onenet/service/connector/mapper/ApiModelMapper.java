package pl.com.tt.flex.onenet.service.connector.mapper;

import java.util.List;

public interface ApiModelMapper <D, M> {

	M toApiModel(D dto);

	D toDto(M apiModel);

	List<M> toApiModel(List<D> dtoList);

	List <D> toDto(List<M> apiModelList);
}
