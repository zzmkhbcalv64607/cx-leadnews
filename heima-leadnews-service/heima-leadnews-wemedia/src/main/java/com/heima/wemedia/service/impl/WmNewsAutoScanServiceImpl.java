package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.apis.article.IArticleClient;
import com.heima.common.aliyun.GreenImageScan;
import com.heima.common.aliyun.GreenTextScan;
import com.heima.common.tess4j.Tess4jClient;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.SensitiveWordUtil;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmSensitiveMapper;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author cys
 * @Date 2023/7/3 22:35
 */

@Service
@Slf4j
@Transactional
public class WmNewsAutoScanServiceImpl  implements WmNewsAutoScanService {


    @Autowired
    private WmNewsMapper wmNewsMapper;
    @Autowired
    private GreenTextScan greenTextScan;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private GreenImageScan greenImageScan;
    @Autowired
    private IArticleClient articleClient;
    @Autowired
    private WmChannelMapper wmChannelMapper;
    @Autowired
    private WmUserMapper wmUserMapper;
    @Autowired
    private WmSensitiveMapper wmSensitiveMapper;

    /**
     * 自媒体文章审核
     *
     * @param id 自媒体文章id
     */
    @Override
    @Async //表明当前方法是一个异步执行
    public void autoScanWmNews(Integer id) {
        //1.查询自媒体文章
        WmNews wmNews = wmNewsMapper.selectById(id);
        if (wmNews == null) {
            throw new RuntimeException("wmNews is null");
        }
        //如果文章状态为提交审核
        if (wmNews.getStatus().equals(WmNews.Status.SUBMIT.getCode())){
            Map<String, Object> textAndImages = handleTextAndImages(wmNews);
            //自管理的敏感词过滤
            Boolean isSensitives = handleSensitives(textAndImages.get("content").toString(),wmNews);
            if (!isSensitives) {
                return;
            }
            //2.审核文本内容  阿里云接口
//            Boolean isTextScan = handleTextScan(textAndImages.get("content").toString(),wmNews);
            boolean isTextScan = true;
            if (!isTextScan) {
                return;
            }
            //3.审核图片  阿里云接口
            Boolean isImageScan = handleImageScan((List<String>) textAndImages.get("images"),wmNews);
            //boolean isImageScan = true;
            //4.审核成功，保存app端的相关的文章数据
            ResponseResult responseResult = saveAppArticle(wmNews);
            if (!responseResult.getCode().equals(200)){
                throw new RuntimeException("saveArticle error 保存失败");
            }
            //回填article_id
            wmNews.setArticleId((Long) responseResult.getData());
            wmNews.setStatus((short) 9);
            wmNews.setReason("审核成功");
            wmNewsMapper.updateById(wmNews);
        }

    }


    /**
     * 处理文本和图片 自添加词语的过滤
     * @param wmNews
     * @return
     */
    private Boolean handleSensitives(String content, WmNews wmNews) {
        boolean flag = true;

        //获取敏感性词语
        List<WmSensitive> wmSensitives = wmSensitiveMapper
                .selectList(Wrappers.<WmSensitive>lambdaQuery()
                        .select(WmSensitive::getSensitives));
        List<String> sensitiveList = wmSensitives.stream().
                map(WmSensitive::getSensitives)
                .collect(Collectors.toList());
        //初始化敏感词库
        SensitiveWordUtil.initMap(sensitiveList);
        //查看文章内容是否包含敏感词
        Map<String, Integer> map = SensitiveWordUtil.matchWords(content);
        if(map.size() >0){
           ;
            wmNews.setStatus((short) 9);
            wmNews.setReason("当前文章中存在违规内容"+map);
            wmNewsMapper.updateById(wmNews);

            flag = true;
        }

        return flag;
    }

    /**
     * 保存app端的相关的文章数据
     * @param wmNews
     * @return
     */

    @Override
    public ResponseResult saveAppArticle(WmNews wmNews) {
        ArticleDto articleDto = new ArticleDto();
        //拷贝属性
        BeanUtils.copyProperties(wmNews,articleDto);
        //布局
        articleDto.setLayout(wmNews.getType());
        //频道
        WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());
        if (wmChannel != null) {
            articleDto.setLabels(wmChannel.getName());
        }
        //作者id
        articleDto.setAuthorId(wmNews.getUserId().longValue());
        WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());
        if (wmUser != null) {
            articleDto.setAuthorName(wmUser.getName());
        }
        //设置文章id
        if (wmNews.getArticleId()!=null) {
            articleDto.setId(wmNews.getArticleId());
        }
        articleDto.setCreatedTime(new Date());
        ResponseResult responseResult = articleClient.saveArticle(articleDto);
        return responseResult;
    }

    /**
     * 审核图片
     * @param images
     * @param wmNews
     * @return
     */
    private Boolean handleImageScan(List<String> images, WmNews wmNews) {
        //图片为空 跳过
        if (images == null || images.size() == 0) {
            return true;
        }

//        //图片不为空
//        //图片去重
      List<byte[]> imageList = new ArrayList<>();
//        images=images.stream().distinct().collect(Collectors.toList());
//        //图片下载
//        try {
//            for (String image : images) {
//                byte[] bytes = fileStorageService.downLoadFile(image);
//                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
//                BufferedImage bufferedImage = ImageIO.read(in);
//                //图片识别
//                String result = tess4jClient.doOCR(bufferedImage);
//                //过滤文字
//                Boolean isBoolean = handleSensitives(result, wmNews);
//                if (!isBoolean) {
//                    return is;
//                }
//                imageList.add(bytes);
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }


        try {
            Map map = greenImageScan.imageScan(imageList);
            if (map != null) {
                //审核不通过
                if (map.get("suggestion").equals("block")) {
                    wmNews.setStatus((short) 3);
                    wmNews.setReason("由于系统原因，转为人工审核");
                    wmNewsMapper.updateById(wmNews);
                    return true;
                }
                //审核失败 人工审核
                //审核不通过
                if (map.get("suggestion").equals("review")) {
                    wmNews.setStatus((short) 3);
                    wmNews.setReason("审核失败，需要人工审核");
                    wmNewsMapper.updateById(wmNews);
                    return true;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 审核内容
     * @param content
     * @param wmNews
     * @return
     */
    private Boolean handleTextScan(String content, WmNews wmNews) {

        //内容为空 跳过
        if((wmNews.getTitle()+"-"+content).length()==0){
            return true;
        }

        try {
            Map map = greenTextScan.greeTextScan(wmNews.getTitle()+"-"+content);
            if (map != null) {
                //审核不通过
                if (map.get("suggestion").equals("block")) {
                    wmNews.setStatus((short) 3);
                    wmNews.setReason("由于系统原因，转为人工审核");
                    wmNewsMapper.updateById(wmNews);
                    return true;
                }
                //审核失败 人工审核
                //审核不通过
                if (map.get("suggestion").equals("review")) {
                    wmNews.setStatus((short) 3 );
                    wmNews.setReason("审核失败，需要人工审核");
                    wmNewsMapper.updateById(wmNews);
                    return true;
                }

            }
            //审核不通过
            //审核通过
        } catch (Exception e) {

            e.printStackTrace();
            return true;
        }
        return true;
    }

    /**
     * 从自媒体文章中提取文本和图片
     * 提取文章的封面图片
     *
     * @param wmNews
     * @return
     */
    private Map<String, Object> handleTextAndImages(WmNews wmNews) {
        //存储文本内容
        StringBuilder stringBuilder = new StringBuilder();

        List<String> images = new ArrayList<>();
        //1.从自媒体文章中提取文本和图片
        if (StringUtils.isNoneBlank(wmNews.getContent())) {
            List<Map> maps = JSONArray.parseArray(wmNews.getContent(), Map.class);
            for (Map map : maps) {
                if (map.get("type").equals("text")) {
                     //文本
                    stringBuilder.append(map.get("value"));
                }
                if (map.get("type").equals("image")) {
                    //图片
                    images.add(map.get("value").toString());

                }
            }
        }

        //2.提取文章的封面图片
        if (StringUtils.isNoneBlank(wmNews.getImages())) {
          String[] spilt= wmNews.getImages().split(",");
          images.addAll(Arrays.asList(spilt));
        }
        Map<String, Object> maps = new HashMap<>();
        maps.put("content", stringBuilder.toString());
        maps.put("images", images);

        return maps;
    }
}
