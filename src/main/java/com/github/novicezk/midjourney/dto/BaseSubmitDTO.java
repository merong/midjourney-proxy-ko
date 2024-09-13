package com.github.novicezk.midjourney.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseSubmitDTO {

	@ApiModelProperty("맞춤 매개변수")
	protected String state;

	@ApiModelProperty("콜백 주소, 비어 있으면 전역 사용 notifyHook")
	protected String notifyHook;
}
