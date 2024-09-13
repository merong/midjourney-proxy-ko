package com.github.novicezk.midjourney.dto;

import com.github.novicezk.midjourney.enums.TaskAction;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@ApiModel("Change Task Submit Parameters (변경 작업 제출 매개변수)")
@EqualsAndHashCode(callSuper = true)
public class SubmitChangeDTO extends BaseSubmitDTO {

	@ApiModelProperty(value = "Task ID (작업 ID)", required = true, example = "\"1320098173412546\"")
	private String taskId;

	@ApiModelProperty(value = "UPSCALE(확대); VARIATION(변형); REROLL(재생성)", required = true,
			allowableValues = "UPSCALE, VARIATION, REROLL", example = "UPSCALE")
	private TaskAction action;

	@ApiModelProperty(value = "Index(1~4), required when action is UPSCALE or VARIATION (인덱스(1~4), action이 UPSCALE 또는 VARIATION일 때 필수)", allowableValues = "range[1, 4]", example = "1")
	private Integer index;

}