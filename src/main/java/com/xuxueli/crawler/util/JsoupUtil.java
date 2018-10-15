package com.xuxueli.crawler.util;

import com.xuxueli.crawler.conf.XxlCrawlerConf;
import com.xuxueli.crawler.model.PageLoadInfo;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * jsoup tool
 *
 * @author xuxueli 2015-05-14 22:44:43
 */
public class JsoupUtil {
    private static Logger logger = LoggerFactory.getLogger(JsoupUtil.class);

    /**
     * 加载页面
     *
     * @param pageLoadInfo
     *
     * @return
     */
    public static Document load(PageLoadInfo pageLoadInfo) {
        if (!UrlUtil.isUrl(pageLoadInfo.getUrl())) {
            return null;
        }
        try {
            // 请求设置
            Connection conn = Jsoup.connect(pageLoadInfo.getUrl());
            if (pageLoadInfo.getParamMap() != null && !pageLoadInfo.getParamMap().isEmpty()) {
                conn.data(pageLoadInfo.getParamMap());
            }
            if (pageLoadInfo.getCookieMap() != null && !pageLoadInfo.getCookieMap().isEmpty()) {
                conn.cookies(pageLoadInfo.getCookieMap());
            }
            if (pageLoadInfo.getHeaderMap()!=null && !pageLoadInfo.getHeaderMap().isEmpty()) {
                conn.headers(pageLoadInfo.getHeaderMap());
            }
            if (pageLoadInfo.getUserAgent()!=null) {
                conn.userAgent(pageLoadInfo.getUserAgent());
            }
            if (pageLoadInfo.getReferrer() != null) {
                conn.referrer(pageLoadInfo.getReferrer());
            }
            conn.timeout(pageLoadInfo.getTimeoutMillis());
            conn.validateTLSCertificates(pageLoadInfo.isValidateTLSCertificates());
            conn.maxBodySize(0);    // 取消默认1M限制

            // 代理
            if (pageLoadInfo.getProxy() != null) {
                conn.proxy(pageLoadInfo.getProxy());
            }

            // 发出请求
            Document html = null;
            if (pageLoadInfo.getIfPost()) {
                html = conn.post();
            } else {
                html = conn.get();
            }
            return html;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 抽取元素数据
     *
     * @param fieldElement
     * @param selectType
     * @param selectVal
     * @return
     */
    public static String parseElement(Element fieldElement, XxlCrawlerConf.SelectType selectType, String selectVal) {
        String fieldElementOrigin = null;
        if (XxlCrawlerConf.SelectType.HTML == selectType) {
            fieldElementOrigin = fieldElement.html();
        } else if (XxlCrawlerConf.SelectType.VAL == selectType) {
            fieldElementOrigin = fieldElement.val();
        } else if (XxlCrawlerConf.SelectType.TEXT == selectType) {
            fieldElementOrigin = fieldElement.text();
        } else if (XxlCrawlerConf.SelectType.ATTR == selectType) {
            fieldElementOrigin = fieldElement.attr(selectVal);
        }  else if (XxlCrawlerConf.SelectType.HAS_CLASS == selectType) {
            fieldElementOrigin = String.valueOf(fieldElement.hasClass(selectVal));
        }  else {
            fieldElementOrigin = fieldElement.toString();
        }
        return fieldElementOrigin;
    }

    /**
     * 获取页面上所有超链接地址 （<a>标签的href值）
     *
     * @param html  页面文档
     * @return
     */
    public static Set<String> findLinks(Document html) {

        if (html == null) {
            return null;
        }

        // element
        /**
         *
         * Elements resultSelect = html.select(tagName);	// 选择器方式
         * Element resultId = html.getElementById(tagName);	// 元素ID方式
         * Elements resultClass = html.getElementsByClass(tagName);	// ClassName方式
         * Elements resultTag = html.getElementsByTag(tagName);	// html标签方式 "body"
         *
         */
        Elements hrefElements = html.select("a[href]");

        // 抽取数据
        Set<String> links = new HashSet<String>();
        if (hrefElements!=null && hrefElements.size() > 0) {
            for (Element item : hrefElements) {
                String href = item.attr("abs:href");    // href、abs:href
                if (UrlUtil.isUrl(href)) {
                    links.add(href);
                }
            }
        }
        return links;
    }

    /**
     * 获取页面上所有图片地址 （<a>标签的href值）
     *
     * @param html
     * @return
     */
    public static Set<String> findImages(Document html) {

        Elements imgs = html.getElementsByTag("img");

        Set<String> images = new HashSet<String>();
        if (imgs!=null && imgs.size() > 0) {
            for (Element element: imgs) {
                String imgSrc = element.attr("abs:src");
                images.add(imgSrc);
            }
        }

        return images;
    }

}
