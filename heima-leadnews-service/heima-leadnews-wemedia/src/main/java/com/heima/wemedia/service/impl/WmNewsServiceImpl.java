package com.heima.wemedia.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constants.WemediaConstants;
import com.heima.common.constants.WmNewsMessageConstants;
import com.heima.common.exception.CustomException;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.*;
import com.heima.utils.thread.WmThreadLocalUtil;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmNewsMaterialMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import com.heima.wemedia.service.WmNewsService;
import com.heima.wemedia.service.WmNewsTaskService;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author cys
 */
@Service
@Slf4j
@Transactional
public class WmNewsServiceImpl  extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {


    @Autowired
    private WmNewsMaterialMapper wmNewsMaterialMapper;
    @Autowired
    private WmMaterialMapper    wmMaterialMapper;
    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;
    @Autowired
    private WmNewsTaskService wmNewsTaskService;
    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;
    /**
     * 查询自媒体文章列表
     * @param dto
     * @return
     */
    @Override
    public ResponseResult findList(WmNewsPageReqDto dto) {
        //检查参数
        //分页检查
        dto.checkParam();
        //分页条件查询
        IPage page = new Page(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<WmNews> wrapper = new LambdaQueryWrapper<>();
        //状态精确查询
        if (dto.getStatus() != null) {
            wrapper.eq(WmNews::getStatus, dto.getStatus());
        }
        //频道查询
        if (dto.getChannelId() != null) {
            wrapper.eq(WmNews::getChannelId, dto.getChannelId());
        }
        //时间范围查询
        if (dto.getBeginPubDate() != null && dto.getEndPubDate()!=null) {
            //选择参数 开始时间<=发布时间<=结束时间
            wrapper.between(WmNews::getPublishTime, dto.getBeginPubDate(), dto.getEndPubDate());
        }
        //关键字模糊查询
        if (StringUtils.isNoneBlank(dto.getKeyword())){
            wrapper.like(WmNews::getTitle,dto.getKeyword());
        }
        //查询当前登录人的文章
        wrapper.eq(WmNews::getUserId, WmThreadLocalUtil.getUser().getId());
        //按照创建时间降序
        wrapper.orderByDesc(WmNews::getPublishTime);
         page = page(page, wrapper);
        //返回结果
        PageResponseResult responseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int) page.getTotal());
        responseResult.setData(page.getRecords());
        return responseResult;
    }


    /**
     * 查询文章详情
     * @param id
     * @return
     */
    @Override
    public ResponseResult findOne(Integer id) {
        //参数检查
        if (id == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //查询文章
        WmNews wmNews = getById(id);
        if (wmNews == null) {
            //返回结果
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);

        }
        //返回结果
        System.out.println(wmNews);
        return ResponseResult.okResult(wmNews);
    }

    /**
     * 删除文章
     * @param id
     * @return
     */
    @Override
    public ResponseResult delNews(Integer id) {
        //参数检查
        if (id == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //检查文章是否存在
        WmNews wmNews = getById(id);
        if (wmNews == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        //检测文章是否发布
        if(wmNews.getStatus().intValue() == WmNews.Status.PUBLISHED.getCode()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "发布文章不允许删除");
        }
        //删除文章
        try {
            removeById(id);
            wmNewsMaterialMapper.delete(new LambdaQueryWrapper<WmNewsMaterial>().eq(WmNewsMaterial::getNewsId,id));
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        } catch (Exception e) {
            log.error("删除文章失败",e);
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
        }


    }

    /**
     * 文章上下架
     * @param dto
     * @return
     */
    @Override
    public ResponseResult downOrUp(WmNewsDto dto) {
        // 1. 参数检查
        if (dto == null || dto.getId() == null || dto.getEnable() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"文章id或者状态不能为空");
        }
        // 2. 检查文章是否存在
        WmNews wmNews = getById(dto.getId());
        if (wmNews == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        // 3. 检查文章是否发布
        if(wmNews.getStatus().intValue() != WmNews.Status.PUBLISHED.getCode()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "当前文章不是发布状态，不允许上下架");
        }
        // 4. 修改文章状态
        if (dto.getEnable()!=null && dto.getEnable()>-1 && dto.getEnable()<2){
           update(Wrappers.<WmNews>lambdaUpdate()
                   .set(WmNews::getEnable,dto.getEnable())
                   .eq(WmNews::getId,dto.getId()));
           if (wmNews.getArticleId()!=null)
           {
               //发送消息 通知article 修改文章状态
               Map<String,Object> map = new HashMap<>();
               map.put("articleId",wmNews.getArticleId());
               map.put("enable",dto.getEnable());

               kafkaTemplate.send(WmNewsMessageConstants.WM_NEWS_UP_OR_DOWN_TOPIC
                       , JSON.toJSONString(map));
               log.info("发送成功等待接受");
           }
        }
            //返回结果
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);

//        try {
//            wmNews.setEnable(dto.getEnable());
//            //更新文章
//            updateById(wmNews);
//            //返回结果
//            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
//        } catch (Exception e) {
//            //记录日志
//            log.error("文章上下架失败",e);
//            //返回结果
//            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
//        }
    }

    /**
     * 提交修改文章 保存为草稿
     * @param dto
     * @return
     */
    @Override
    public ResponseResult submitNews(WmNewsDto dto) {
        //提交判断
        if(dto== null ||dto.getContent()==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //保存 或 修改
        WmNews wmNews = new WmNews();
        //属性拷贝 dto->wmNews 名称相同的属性拷贝
        BeanUtils.copyProperties(dto,wmNews);
        //封面图片
        if (dto.getImages()!=null && dto.getImages().size()>0) {
            //设置封面图片 逗号分隔
            String join = StringUtils.join(dto.getImages(), ",");
            wmNews.setImages(join);
        }
        //如果当前封面类型是自动 则设置封面图片为-1
        if (dto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)){
            wmNews.setType(null);
        }
        saveOrUpdateWmNews(wmNews);
        //判断是否为草稿 如果是草稿结束此方法
        if (dto.getStatus().equals(WmNews.Status.NORMAL.getCode())){
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }
        //不是草稿 保存文章内容图片与素材关系
        //或取文章内容中的图片信息
        List<String> images =ectractUrlInfo (dto.getContent());
        saveRelativeInfoForContent(images,wmNews.getId());
        //不是草稿 保存文章封面图片与素材关系
        saveRelativeInfoForCover(dto,wmNews,images);
        //审核文章
        wmNewsAutoScanService.autoScanWmNews(wmNews.getId());
      //  wmNewsTaskService.addNewsToTask(wmNews.getId(), wmNews.getPublishTime());
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
    /**
     * 分页查询文章列表
     * @param dto
     */
    @Override
    public ResponseResult findListAndPage(WmNewsPageReqDto dto) {
        int flag = 0;
        //参数检查
        if (dto == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //分页检查
        dto.checkParam();
        //
        LambdaQueryWrapper<WmNews> wrapper = new LambdaQueryWrapper<>();
        if (dto.getStatus()!=null){
            wrapper.eq(WmNews::getStatus,dto.getStatus());
            flag = 1;
        }
        if (Strings.isNotBlank(dto.getTitle())){
            wrapper.like(WmNews::getTitle,dto.getTitle());
            flag = 1;
        }
        Page<WmNews> page =null;
        Page<WmNews> objectPage = new Page<>(dto.getPage(), dto.getSize());
        if (flag == 0){
            page = page(objectPage);
        }else {
             page = page(objectPage, wrapper);
        }

        ResponseResult responseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int)page.getTotal());
        responseResult.setData(page.getRecords());
        return responseResult;
    }


    /**
     * 查询文章详情
     * @param id
     * @return
     */
    @Override
    public ResponseResult find(Integer id) {
        if (id == null || id < 1){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        WmNews wmNews = getById(id);
        if (wmNews == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        ResponseResult responseResult = new ResponseResult<>();
        responseResult.setData(wmNews);
        return responseResult;
    }

    @Override
    public ResponseResult updateStatus(WmNewsDto dto) {
        if (dto == null || dto.getId() == null || dto.getStatus() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        WmNews wmNews = getById(dto.getId());
        if (wmNews == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        wmNews.setStatus(dto.getStatus());
        if(StringUtils.isNotBlank(dto.getMsg())){
            wmNews.setReason(dto.getMsg());
        }
        boolean status = updateById(wmNews);
        //审核成功，则需要创建app端文章数据，并修改自媒体文章
        if(dto.getStatus().equals(WemediaConstants.WM_NEWS_AUTH_PASS)){
            //创建app端文章数据
            ResponseResult responseResult = wmNewsAutoScanService.saveAppArticle(wmNews);
            if(responseResult.getCode().equals(200)){
                wmNews.setArticleId((Long) responseResult.getData());
                wmNews.setStatus(WmNews.Status.PUBLISHED.getCode());
                updateById(wmNews);
            }
        }
        if(dto.getStatus().equals(WemediaConstants.WM_NEWS_PEO_PASS)){
            //创建app端文章数据
            ResponseResult responseResult = wmNewsAutoScanService.saveAppArticle(wmNews);
            if(responseResult.getCode().equals(200)){
                wmNews.setArticleId((Long) responseResult.getData());
                wmNews.setStatus(WmNews.Status.PUBLISHED.getCode());
                updateById(wmNews);
            }
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 第一个功能：如果当前封面类型为自动，则设置封面类型的数据
     * 匹配规则：
     * 1，如果内容图片大于等于1，小于3  单图  type 1
     * 2，如果内容图片大于等于3  多图  type 3
     * 3，如果内容没有图片，无图  type 0
     *
     * 第二个功能：保存封面图片与素材的关系
     * @param dto
     * @param wmNews
     * @param materials
     */
    private void saveRelativeInfoForCover(WmNewsDto dto, WmNews wmNews, List<String> materials) {

        List<String> images = dto.getImages();

        //如果当前封面类型为自动，则设置封面类型的数据
        if(dto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)){
            //多图
            if(materials.size() >= 3){
                wmNews.setType(WemediaConstants.WM_NEWS_MANY_IMAGE);
                images = materials.stream().limit(3).collect(Collectors.toList());
            }else if(materials.size() >= 1 && materials.size() < 3){
                //单图
                wmNews.setType(WemediaConstants.WM_NEWS_SINGLE_IMAGE);
                images = materials.stream().limit(1).collect(Collectors.toList());
            }else {
                //无图
                wmNews.setType(WemediaConstants.WM_NEWS_NONE_IMAGE);
            }

            //修改文章
            if(images != null && images.size() > 0){
                wmNews.setImages(StringUtils.join(images,","));
            }
            updateById(wmNews);
        }
        if(images != null && images.size() > 0){
            saveRelativeInfo(images,wmNews.getId(),WemediaConstants.WM_COVER_REFERENCE);
        }
    }

    /**
     * 处理文章内容图片与素材关系
     * @param images
     * @param id
     */
    private void saveRelativeInfoForContent(List<String> images, Integer id) {
        saveRelativeInfo(images,id,WemediaConstants.WM_CONTENT_REFERENCE);
    }
    /**
     * 处理文章封面图片与素材关系到数据库中
     * @param images
     * @param id
     */
    private void saveRelativeInfo(List<String> images, Integer id, Short wmContentReference) {
        if (images != null || !images.isEmpty()) {
            //通过图片的Url查询素材id
            List<WmMaterial> materials = wmMaterialMapper.selectList(Wrappers.<WmMaterial>lambdaQuery()
                    .in(WmMaterial::getUrl, images));
            //判断素材是否有效
            if (materials == null || materials.size() == 0) {
                //手动抛出异常 第一个功能：能够提示用户图片无效 第二个功能：能够回滚事务
                throw new CustomException(AppHttpCodeEnum.MATERIASL_REFERENCE_FAIL);
            }

            if (materials.size() != images.size()) {
                throw new CustomException(AppHttpCodeEnum.MATERIASL_REFERENCE_FAIL);
            }
            List<Integer> idList = materials.stream().map(WmMaterial::getId).collect(Collectors.toList());

            //批量保存
            wmNewsMaterialMapper.saveRelations(idList,id,wmContentReference);
            //
        }

    }

    /**
     * 或取文章内容中的图片信息
     * @param content
     */
    private List<String> ectractUrlInfo(String content) {
        List<String> materials = new ArrayList<>();
        List<Map> maps = JSON.parseArray(content, Map.class);
        for (Map map : maps) {
            if (map.get("type").equals("image")){
                String value = (String) map.get("value");
                materials.add(value);
            }
        }
        return  materials;
    }

    /**
     * 保存或修改文章
     * @param wmNews
     */
    private void saveOrUpdateWmNews(WmNews wmNews) {
        //设置当前用户id
        wmNews.setUserId(WmThreadLocalUtil.getUser().getId());
        //设置当前时间
        wmNews.setCreatedTime(new Date());
        //设置提交时间
        wmNews.setSubmitedTime(new Date());
        //设置上架状态
        wmNews.setEnable((short) 1);
        //保存或修改文章
        if (wmNews.getId() == null) {
            //保存文章
            save(wmNews);
        }else {
            //修改文章
            wmNewsMaterialMapper.delete(Wrappers.<WmNewsMaterial>lambdaQuery()
                            .eq(WmNewsMaterial::getNewsId,wmNews.getId()));
            updateById(wmNews);
        }


    }
}
