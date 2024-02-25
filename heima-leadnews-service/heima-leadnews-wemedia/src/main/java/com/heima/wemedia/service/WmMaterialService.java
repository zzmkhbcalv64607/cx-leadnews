package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author cys
 */
public interface WmMaterialService extends IService<WmMaterial> {
    /**
     * 上传图片
     * @param multipartFile
     * @return
     */
    public ResponseResult uploadPicture(MultipartFile multipartFile);

    /**
     * 查询素材列表
     * @param dto
     * @return
     */
    public ResponseResult findList( WmMaterialDto dto);

    /**
     * 删除图片
     * @param id
     * @return
     */
    public ResponseResult delPicture( Integer id);

    /**
     * 取消收藏图片
     * @param id
     * @return
     */
    public ResponseResult cancelCollect(Integer id);

    /**
     * 收藏图片
     * @param id
     * @return
     */
    public ResponseResult collect(Integer id);
}