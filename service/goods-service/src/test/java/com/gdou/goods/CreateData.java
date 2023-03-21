//package com.gdou.goods;
//
//import cn.hutool.http.HttpUtil;
//import cn.hutool.json.JSONObject;
//import cn.hutool.json.JSONUtil;
//import com.alibaba.fastjson.JSON;
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.gdou.goods.entity.Categorize;
//import com.gdou.goods.entity.Goods;
//import com.gdou.goods.entity.Picture;
//import com.gdou.goods.mapper.CategorizeMapper;
//import com.gdou.goods.service.CategorizeService;
//import com.gdou.goods.service.GoodsService;
//import com.gdou.goods.service.PictureService;
//import com.gdou.goods.testEntity.TestGoods;
//import com.gdou.goods.testEntity.TestPic;
//import org.checkerframework.checker.units.qual.A;
//import org.checkerframework.checker.units.qual.C;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//@SpringBootTest
//public class CreateData {
//
//    @Autowired
//    private CategorizeService categorizeService;
//
//
//    @Test
//    public void createCategorize(){
//        String s = HttpUtil.get("https://www.uinav.com/api/public/v1/categories");
//        Object message = JSON.parseObject(s, Map.class).get("message");
//        List<test> tests = JSON.parseArray(JSON.toJSONString(message), test.class);
//        for (test test : tests) {
//            dg(test);
//        }
//    }
//
//
//    public void dg(test test){
//        Categorize categorize = new Categorize();
//        categorize.setId(test.getCat_id());
//        categorize.setParentId(test.getCat_pid());
//        categorize.setIcon(test.getCat_icon());
//        categorize.setLevel(test.getCat_level());
//        categorize.setName(test.getCat_name());
//
//        categorizeService.save(categorize);
//
//        List<com.gdou.goods.test> list = test.getChildren();
//        //判断儿子有没有
//        if (list!=null&&list.size()!=0){
//            for (com.gdou.goods.test children : list) {
//                dg(children);
//            }
//        }
//    }
//
//    @Autowired
//    private GoodsService goodsService;
//
//    @Autowired
//    private PictureService pictureService;
//
//
//    @Test
//    public void createGoods(){
////        获取所有三级分类id
//        List<Categorize> idList = categorizeService
//                .list(new LambdaQueryWrapper<Categorize>().eq(Categorize::getLevel, 2)
//                        .select(Categorize::getId));
////        List<Categorize> idList = new ArrayList<>();
////        idList.add( new Categorize().setId(14));
//        for (Categorize categorize : idList) {
//            String s = HttpUtil.get("http://api-ugo-web.itheima.net/api/public/v1/goods/search?query=&cid=" +
//                    categorize.getId()+ "&pagenum=1&pagesize=1000");
//        System.out.println(s);
//            Object message = JSONUtil.toBean(s, Map.class).get("message");
//        Object goods = JSON.parseObject(JSONUtil.toJsonStr(message), Map.class).get("goods");
//
//        List<TestGoods> goodsList = JSON.parseArray(JSON.toJSONString(goods), TestGoods.class);
//
//                if (goods==null||goodsList.isEmpty()){
//                    continue;
//                }
//            for (TestGoods goods1 : goodsList) {
//                String goodDetailJson = HttpUtil.get("http://api-ugo-web.itheima.net/api/public/v1/goods/detail?goods_id=" + goods1.getGoods_id());
//                Object detailMessage = JSON.parseObject(goodDetailJson, Map.class).get("message");
//                TestGoods testGoods = JSON.parseObject(JSON.toJSONString(detailMessage), TestGoods.class);
//                System.out.println(testGoods);
//
//                Goods goods2 = new Goods();
//                goods2.setId(testGoods.getGoods_id());
//                goods2.setName(testGoods.getGoods_name());
//                goods2.setPrice(testGoods.getGoods_price());
//                goods2.setNumber(testGoods.getGoods_number());
//                goods2.setIntroduce(testGoods.getGoods_introduce());
//                goods2.setBigLogo(testGoods.getGoods_big_logo());
//                goods2.setSmallLogo(testGoods.getGoods_small_logo());
//                goods2.setCatOneId(testGoods.getCat_one_id());
//                goods2.setCatTwoId(testGoods.getCat_two_id());
//                goods2.setCatThreeId(testGoods.getCat_three_id());
//
//                goodsService.save(goods2);
//
//                List<TestPic> pics = testGoods.getPics();
//                if (pics==null||pics.isEmpty()){
//                    continue;
//                }
//                for (TestPic pic : pics) {
//                    Picture picture = new Picture();
//                    picture.setId(pic.getPics_id());
//                    picture.setGoodsId(pic.getGoods_id());
//                    picture.setPicsBig(pic.getPics_big());
//                    picture.setPicsMid(pic.getPics_mid());
//                    picture.setPicsSma(pic.getPics_sma());
//                    picture.setPicsBigUrl(pic.getPics_big_url());
//                    picture.setPicsMidUrl(pic.getPics_mid_url());
//                    picture.setPicsSmaUrl(pic.getPics_sma_url());
//                    pictureService.save(picture);
//                }
//            }
//        }
//
//    }
//}
