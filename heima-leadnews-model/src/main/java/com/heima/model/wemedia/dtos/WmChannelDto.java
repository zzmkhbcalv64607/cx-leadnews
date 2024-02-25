package com.heima.model.wemedia.dtos;

import com.baomidou.mybatisplus.annotation.TableField;
import com.heima.model.common.dtos.PageRequestDto;
import lombok.Data;

/**
 * @author cys
 * @Date 2023-2023/7/16-11:09
 */
@Data
public class WmChannelDto extends PageRequestDto {
    /**
     * 频道名称
     */
    @TableField("name")
    private String name;
}
