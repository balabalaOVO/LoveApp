package com.yupi.yuaiagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("conversation")
public class Conversation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String chatKey;
    private String title;
    private String appType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
