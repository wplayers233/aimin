package com.oimc.aimin.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema( description ="模糊查询分页参数")
public class PageRequest {
    @Schema(description = "模糊查询内容", example = "admin")
    private String content;

    @Schema(description = "部门id", example = "1")
    private Integer deptId;

    @Schema(description = "分页大小", example = "10")
    @NotNull(message = "分页大小不能为空")
    @Min(value = 1, message = "分页大小至少为 1")
    @Max(value = 100, message = "单页最多 100 条")
    private Integer pageSize = 10;

    @Schema(description = "分页索引", example = "1")
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码从 1 开始")
    private Integer currentPage = 1;
}
