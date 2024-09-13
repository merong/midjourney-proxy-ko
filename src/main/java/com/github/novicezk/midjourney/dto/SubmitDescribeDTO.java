package com.github.novicezk.midjourney.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel("Describe Submit Parameters (설명 제출 매개변수)")
@EqualsAndHashCode(callSuper = true)
public class SubmitDescribeDTO extends BaseSubmitDTO {

	@ApiModelProperty(value = "Image base64 string (이미지 base64 문자열)", required = true, example = "data:image/png;base64,xxx")
	private String base64;
}