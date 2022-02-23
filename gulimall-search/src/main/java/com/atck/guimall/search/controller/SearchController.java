package com.atck.guimall.search.controller;

import com.atck.guimall.search.service.MallSearchService;
import com.atck.guimall.search.vo.SearchParam;
import com.atck.guimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SearchController
{
    @Autowired
    MallSearchService mallSearchService;

    /**
     * 自动将页面提交的查询参数封装为指定的对象
     * @param param
     * @return
     */
    @GetMapping("/list.html")
    public String listPage(SearchParam param, Model model, HttpServletRequest request)
    {
        String queryString = request.getQueryString();
        param.set_queryString(queryString);
        SearchResult result =  mallSearchService.search(param);
        model.addAttribute("result",result);
        return "list";
    }
}
