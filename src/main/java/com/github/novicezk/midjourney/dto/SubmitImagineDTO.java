package com.github.novicezk.midjourney.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;


@Data
@ApiModel("Imagine Submit Parameters (이미지 생성 제출 매개변수)")
@EqualsAndHashCode(callSuper = true)
public class SubmitImagineDTO extends BaseSubmitDTO {

	@ApiModelProperty(value = "Prompt (프롬프트)", required = true, example = "Cat")
	private String prompt;

	@ApiModelProperty(value = "Array of image base64 strings for image-to-image (이미지 변환용 base64 문자열 배열)")
	private List<String> base64Array;

	@ApiModelProperty(hidden = true)
	@Deprecated(since = "3.0", forRemoval = true)
	private String base64;

}