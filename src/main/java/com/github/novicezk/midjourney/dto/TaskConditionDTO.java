package com.github.novicezk.midjourney.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("Task Query Parameters (작업 조회 매개변수)")
public class TaskConditionDTO {

	private List<String> ids;

}