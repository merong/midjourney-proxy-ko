package com.github.novicezk.midjourney.dto;

import com.github.novicezk.midjourney.enums.BlendDimensions;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@ApiModel("Blend Submit Parameters (블렌드 제출 매개변수)")
@EqualsAndHashCode(callSuper = true)
public class SubmitBlendDTO extends BaseSubmitDTO {

	@ApiModelProperty(value = "Array of image base64 strings (이미지 base64 문자열 배열)", required = true, example = "[\"data:image/png;base64,xxx1\", \"data:image/png;base64,xxx2\"]")
	private List<String> base64Array;

	@ApiModelProperty(value = "Aspect ratio: PORTRAIT(2:3); SQUARE(1:1); LANDSCAPE(3:2) (비율: 세로(2:3); 정사각형(1:1); 가로(3:2))", example = "SQUARE")
	private BlendDimensions dimensions = BlendDimensions.SQUARE;
}
