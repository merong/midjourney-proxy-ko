package com.github.novicezk.midjourney.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@ApiModel("Simple Change Task Submit Parameters (간단한 변경 작업 제출 매개변수)")
@EqualsAndHashCode(callSuper = true)
public class SubmitSimpleChangeDTO extends BaseSubmitDTO {

	@ApiModelProperty(value = "Change description: ID $action$index (변경 설명: ID $action$index)", required = true, example = "1320098173412546 U2")
	private String content;

}