package com.wang.esjd.utils;

import com.wang.esjd.pojo.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wanglimin
 * @create 2020-06-04 9:46
 */
public class HtmlParseUtil {
    public static void main(String[] args) throws Exception {
        new HtmlParseUtil().parseJD("vue").forEach(System.out::println);

    }
    public List<Content> parseJD(String keyword) throws Exception{
        //获取请求 https://search.jd.com/Search?keyword=java
        //ajax不能获取
        String url = "https://search.jd.com/Search?keyword="+keyword;
        //解析网页(Jsoup返回的Document对象就是浏览器Documentd对象)
        Document document = Jsoup.parse(new URL(url), 30000);
        Element element = document.getElementById("J_goodsList");

        //获取所有的li元素
        Elements elements = element.getElementsByTag("li");
        //获取元素中的内容

        ArrayList<Content> goodsList = new ArrayList<>();

        for(Element el : elements){
            //关于图片特别多的网站，所有的图片都是延迟加载的
            //source-data-lazy-img
            String img = el.getElementsByTag("img").eq(0).attr("src");
            String price = el.getElementsByClass("p-price").eq(0).text();
            String title = el.getElementsByClass("p-name").eq(0).text();

            Content content = new Content();
            content.setImg(img);
            content.setPrice(price);
            content.setTitle(title);
            goodsList.add(content);
        }
        return goodsList;
    }
}
