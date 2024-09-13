package com.github.novicezk.midjourney.support;

import com.github.novicezk.midjourney.domain.DomainObject;
import com.github.novicezk.midjourney.enums.TaskAction;
import com.github.novicezk.midjourney.enums.TaskStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("Task (작업)")
public class Task extends DomainObject {
	@Serial
	private static final long serialVersionUID = -674915748204390789L;

	@ApiModelProperty("Task Type (작업 유형)")
	private TaskAction action;
	@ApiModelProperty("Task Status (작업 상태)")
	private TaskStatus status = TaskStatus.NOT_START;

	@ApiModelProperty("Prompt (프롬프트)")
	private String prompt;
	@ApiModelProperty("Prompt - English (프롬프트 - 영어)")
	private String promptEn;

	@ApiModelProperty("Task Description (작업 설명)")
	private String description;
	@ApiModelProperty("Custom Parameters (사용자 정의 매개변수)")
	private String state;

	@ApiModelProperty("Submission Time (제출 시간)")
	private Long submitTime;
	@ApiModelProperty("Start Execution Time (실행 시작 시간)")
	private Long startTime;
	@ApiModelProperty("End Time (종료 시간)")
	private Long finishTime;

	@ApiModelProperty("Image URL (이미지 URL)")
	private String imageUrl;

	@ApiModelProperty("Task Progress (작업 진행률)")
	private String progress;
	@ApiModelProperty("Failure Reason (실패 이유)")
	private String failReason;

	public void start() {
		this.startTime = System.currentTimeMillis();
		this.status = TaskStatus.SUBMITTED;
		this.progress = "0%";
	}

	public void success() {
		this.finishTime = System.currentTimeMillis();
		this.status = TaskStatus.SUCCESS;
		this.progress = "100%";
	}

	public void fail(String reason) {
		this.finishTime = System.currentTimeMillis();
		this.status = TaskStatus.FAILURE;
		this.failReason = reason;
		this.progress = "";
	}
}
