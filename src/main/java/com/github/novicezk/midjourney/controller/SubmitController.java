package com.github.novicezk.midjourney.controller;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;
import com.github.novicezk.midjourney.Constants;
import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.ReturnCode;
import com.github.novicezk.midjourney.dto.BaseSubmitDTO;
import com.github.novicezk.midjourney.dto.SubmitBlendDTO;
import com.github.novicezk.midjourney.dto.SubmitChangeDTO;
import com.github.novicezk.midjourney.dto.SubmitDescribeDTO;
import com.github.novicezk.midjourney.dto.SubmitImagineDTO;
import com.github.novicezk.midjourney.dto.SubmitSimpleChangeDTO;
import com.github.novicezk.midjourney.enums.TaskAction;
import com.github.novicezk.midjourney.enums.TaskStatus;
import com.github.novicezk.midjourney.enums.TranslateWay;
import com.github.novicezk.midjourney.exception.BannedPromptException;
import com.github.novicezk.midjourney.result.SubmitResultVO;
import com.github.novicezk.midjourney.service.TaskService;
import com.github.novicezk.midjourney.service.TaskStoreService;
import com.github.novicezk.midjourney.service.TranslateService;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.util.BannedPromptUtils;
import com.github.novicezk.midjourney.util.ConvertUtils;
import com.github.novicezk.midjourney.util.MimeTypeUtils;
import com.github.novicezk.midjourney.util.SnowFlake;
import com.github.novicezk.midjourney.util.TaskChangeParams;
import eu.maxschuster.dataurl.DataUrl;
import eu.maxschuster.dataurl.DataUrlSerializer;
import eu.maxschuster.dataurl.IDataUrlSerializer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Api(tags = "과제제출")
@RestController
@RequestMapping("/submit")
@RequiredArgsConstructor
public class SubmitController {
	private final TranslateService translateService;
	private final TaskStoreService taskStoreService;
	private final ProxyProperties properties;
	private final TaskService taskService;

	@ApiOperation(value = "Imagine 작업 제출")
	@PostMapping("/imagine")
	public SubmitResultVO imagine(@RequestBody SubmitImagineDTO imagineDTO) {
		String prompt = imagineDTO.getPrompt();
		if (CharSequenceUtil.isBlank(prompt)) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "prompt비워둘 수 없습니다.");
		}
		prompt = prompt.trim();
		Task task = newTask(imagineDTO);
		task.setAction(TaskAction.IMAGINE);
		task.setPrompt(prompt);
		String promptEn = translatePrompt(prompt);
		try {
			BannedPromptUtils.checkBanned(promptEn);
		} catch (BannedPromptException e) {
			return SubmitResultVO.fail(ReturnCode.BANNED_PROMPT, "민감한 단어가 포함되어 있을 수 있습니다.")
					.setProperty("promptEn", promptEn).setProperty("bannedWord", e.getMessage());
		}
		List<String> base64Array = Optional.ofNullable(imagineDTO.getBase64Array()).orElse(new ArrayList<>());
		if (CharSequenceUtil.isNotBlank(imagineDTO.getBase64())) {
			base64Array.add(imagineDTO.getBase64());
		}
		List<DataUrl> dataUrls;
		try {
			dataUrls = ConvertUtils.convertBase64Array(base64Array);
		} catch (MalformedURLException e) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "base64형식 오류");
		}
		task.setPromptEn(promptEn);
		task.setDescription("/imagine " + prompt);
		return this.taskService.submitImagine(task, dataUrls);
	}

	@ApiOperation(value = "도면 변경-simple")
	@PostMapping("/simple-change")
	public SubmitResultVO simpleChange(@RequestBody SubmitSimpleChangeDTO simpleChangeDTO) {
		TaskChangeParams changeParams = ConvertUtils.convertChangeParams(simpleChangeDTO.getContent());
		if (changeParams == null) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "content매개변수 오류");
		}
		SubmitChangeDTO changeDTO = new SubmitChangeDTO();
		changeDTO.setAction(changeParams.getAction());
		changeDTO.setTaskId(changeParams.getId());
		changeDTO.setIndex(changeParams.getIndex());
		changeDTO.setState(simpleChangeDTO.getState());
		changeDTO.setNotifyHook(simpleChangeDTO.getNotifyHook());
		return change(changeDTO);
	}

	@ApiOperation(value = "도면 변경")
	@PostMapping("/change")
	public SubmitResultVO change(@RequestBody SubmitChangeDTO changeDTO) {
		if (CharSequenceUtil.isBlank(changeDTO.getTaskId())) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "taskId비워둘 수 없습니다.");
		}
		if (!Set.of(TaskAction.UPSCALE, TaskAction.VARIATION, TaskAction.REROLL).contains(changeDTO.getAction())) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "action매개변수 오류");
		}
		String description = "/up " + changeDTO.getTaskId();
		if (TaskAction.REROLL.equals(changeDTO.getAction())) {
			description += " R";
		} else {
			description += " " + changeDTO.getAction().name().charAt(0) + changeDTO.getIndex();
		}
		Task targetTask = this.taskStoreService.get(changeDTO.getTaskId());
		if (targetTask == null) {
			return SubmitResultVO.fail(ReturnCode.NOT_FOUND, "연결된 작업이 존재하지 않거나 만료되었습니다.");
		}
		if (!TaskStatus.SUCCESS.equals(targetTask.getStatus())) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "관련 작업 상태 오류");
		}
		if (!Set.of(TaskAction.IMAGINE, TaskAction.VARIATION, TaskAction.REROLL, TaskAction.BLEND).contains(targetTask.getAction())) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "연결된 작업은 실행 변경을 허용하지 않습니다.");
		}
		Task task = newTask(changeDTO);
		task.setAction(changeDTO.getAction());
		task.setPrompt(targetTask.getPrompt());
		task.setPromptEn(targetTask.getPromptEn());
		task.setProperty(Constants.TASK_PROPERTY_FINAL_PROMPT, targetTask.getProperty(Constants.TASK_PROPERTY_FINAL_PROMPT));
		task.setProperty(Constants.TASK_PROPERTY_PROGRESS_MESSAGE_ID, targetTask.getProperty(Constants.TASK_PROPERTY_MESSAGE_ID));
		task.setProperty(Constants.TASK_PROPERTY_DISCORD_INSTANCE_ID, targetTask.getProperty(Constants.TASK_PROPERTY_DISCORD_INSTANCE_ID));
		task.setDescription(description);
		int messageFlags = targetTask.getPropertyGeneric(Constants.TASK_PROPERTY_FLAGS);
		String messageId = targetTask.getPropertyGeneric(Constants.TASK_PROPERTY_MESSAGE_ID);
		String messageHash = targetTask.getPropertyGeneric(Constants.TASK_PROPERTY_MESSAGE_HASH);
		task.setProperty(Constants.TASK_PROPERTY_REFERENCED_MESSAGE_ID, messageId);
		if (TaskAction.UPSCALE.equals(changeDTO.getAction())) {
			return this.taskService.submitUpscale(task, messageId, messageHash, changeDTO.getIndex(), messageFlags);
		} else if (TaskAction.VARIATION.equals(changeDTO.getAction())) {
			return this.taskService.submitVariation(task, messageId, messageHash, changeDTO.getIndex(), messageFlags);
		} else {
			return this.taskService.submitReroll(task, messageId, messageHash, messageFlags);
		}
	}

	@ApiOperation(value = "작업제출상세")
	@PostMapping("/describe")
	public SubmitResultVO describe(@RequestBody SubmitDescribeDTO describeDTO) {
		if (CharSequenceUtil.isBlank(describeDTO.getBase64())) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "base64비워둘 수 없습니다.");
		}
		IDataUrlSerializer serializer = new DataUrlSerializer();
		DataUrl dataUrl;
		try {
			dataUrl = serializer.unserialize(describeDTO.getBase64());
		} catch (MalformedURLException e) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "base64형식 오류");
		}
		Task task = newTask(describeDTO);
		task.setAction(TaskAction.DESCRIBE);
		String taskFileName = task.getId() + "." + MimeTypeUtils.guessFileSuffix(dataUrl.getMimeType());
		task.setDescription("/describe " + taskFileName);
		return this.taskService.submitDescribe(task, dataUrl);
	}

	@ApiOperation(value = "Blend제출상세")
	@PostMapping("/blend")
	public SubmitResultVO blend(@RequestBody SubmitBlendDTO blendDTO) {
		List<String> base64Array = blendDTO.getBase64Array();
		if (base64Array == null || base64Array.size() < 2 || base64Array.size() > 5) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "base64List매개변수 오류");
		}
		if (blendDTO.getDimensions() == null) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "dimensions매개변수 오류");
		}
		IDataUrlSerializer serializer = new DataUrlSerializer();
		List<DataUrl> dataUrlList = new ArrayList<>();
		try {
			for (String base64 : base64Array) {
				DataUrl dataUrl = serializer.unserialize(base64);
				dataUrlList.add(dataUrl);
			}
		} catch (MalformedURLException e) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "base64형식 오류");
		}
		Task task = newTask(blendDTO);
		task.setAction(TaskAction.BLEND);
		task.setDescription("/blend " + task.getId() + " " + dataUrlList.size());
		return this.taskService.submitBlend(task, dataUrlList, blendDTO.getDimensions());
	}

	private Task newTask(BaseSubmitDTO base) {
		Task task = new Task();
		task.setId(System.currentTimeMillis() + RandomUtil.randomNumbers(3));
		task.setSubmitTime(System.currentTimeMillis());
		task.setState(base.getState());
		String notifyHook = CharSequenceUtil.isBlank(base.getNotifyHook()) ? this.properties.getNotifyHook() : base.getNotifyHook();
		task.setProperty(Constants.TASK_PROPERTY_NOTIFY_HOOK, notifyHook);
		task.setProperty(Constants.TASK_PROPERTY_NONCE, SnowFlake.INSTANCE.nextId());
		return task;
	}

	private String translatePrompt(String prompt) {
		if (TranslateWay.NULL.equals(this.properties.getTranslateWay()) || CharSequenceUtil.isBlank(prompt) || !this.translateService.containsChinese(prompt)) {
			return prompt;
		}
		String paramStr = "";
		Matcher paramMatcher = Pattern.compile("\\x20+--[a-z]+.*$", Pattern.CASE_INSENSITIVE).matcher(prompt);
		if (paramMatcher.find()) {
			paramStr = paramMatcher.group(0);
		}
		String promptWithoutParam = CharSequenceUtil.sub(prompt, 0, prompt.length() - paramStr.length());
		List<String> imageUrls = new ArrayList<>();
		Matcher imageMatcher = Pattern.compile("https?://[a-z0-9-_:@&?=+,.!/~*'%$]+\\x20+", Pattern.CASE_INSENSITIVE).matcher(promptWithoutParam);
		while (imageMatcher.find()) {
			imageUrls.add(imageMatcher.group(0));
		}
		String text = promptWithoutParam;
		for (String imageUrl : imageUrls) {
			text = CharSequenceUtil.replaceFirst(text, imageUrl, "");
		}
		if (CharSequenceUtil.isNotBlank(text)) {
			text = this.translateService.translateToEnglish(text).trim();
		}
		if (CharSequenceUtil.isNotBlank(paramStr)) {
			Matcher paramNomatcher = Pattern.compile("--no\\s+(.*?)(?=--|$)").matcher(paramStr);
			if (paramNomatcher.find()) {
				String paramNoStr = paramNomatcher.group(1).trim();
				String paramNoStrEn = this.translateService.translateToEnglish(paramNoStr).trim();
				paramStr = paramNomatcher.replaceFirst("--no " + paramNoStrEn + " ");
			}
		}
		return CharSequenceUtil.join("", imageUrls) + text + paramStr;
	}

}
