package com.atck.guimall.search.service;

import com.atck.guimall.search.vo.SearchParam;
import com.atck.guimall.search.vo.SearchResult;

public interface MallSearchService
{

    SearchResult search(SearchParam param);
}
