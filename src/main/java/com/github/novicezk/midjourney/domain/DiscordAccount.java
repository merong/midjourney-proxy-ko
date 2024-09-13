package com.github.novicezk.midjourney.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.novicezk.midjourney.Constants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("Discord Account (Discord 계정)")
public class DiscordAccount extends DomainObject {

	@ApiModelProperty("Server ID (서버 ID)")
	private String guildId;
	@ApiModelProperty("Channel ID (채널 ID)")
	private String channelId;
	@ApiModelProperty("User Token (사용자 토큰)")
	private String userToken;
	@ApiModelProperty("User Agent (사용자 에이전트)")
	private String userAgent = Constants.DEFAULT_DISCORD_USER_AGENT;

	@ApiModelProperty("Is Available (사용 가능 여부)")
	private boolean enable = true;

	@ApiModelProperty("Concurrency (동시 처리 수)")
	private int coreSize = 3;
	@ApiModelProperty("Queue Length (대기열 길이)")
	private int queueSize = 10;
	@ApiModelProperty("Task Timeout (Minutes) (작업 시간 초과 (분))")
	private int timeoutMinutes = 5;

	@JsonIgnore
	public String getDisplay() {
		return this.channelId;
	}
}
