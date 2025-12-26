package com.myai.spring_ai.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeTools {
    // @Tool：标记该方法为大模型可调用的工具，description描述工具功能
    @Tool(description = "通过时区id获取当前时间")
    public String getTimeByZoneId(
            // @ToolParam：描述参数含义，指导大模型传参
            @ToolParam(description = "时区id，比如 Asia/Shanghai")
            String zoneId
    ) {
        // 逻辑：根据时区id获取当前时间并格式化
        ZoneId zid = ZoneId.of(zoneId);
        ZonedDateTime zonedDateTime = ZonedDateTime.now(zid);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return zonedDateTime.format(formatter);
    }
}