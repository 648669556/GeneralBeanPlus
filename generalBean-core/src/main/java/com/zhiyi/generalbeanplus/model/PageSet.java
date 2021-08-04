package com.zhiyi.generalbeanplus.model;

import java.util.List;

/**
 * 分页对象
 * 
 * @author sjw
 *
 */
public class PageSet<T> {
    /**
     * 
     */
    private static final long serialVersionUID = -994687089597979437L;
    /**
     * 开始行号
     */
    private Integer           start;
    /**
     * 每页的条数
     */
    private Integer           pageSize;
    /**
     * 总共有几条
     */
    private Integer           resultCount = 0;
    /**
     * 请求页
     */
    private Integer           currentPageNo;
    /**
     * 总共有几页
     * 
     */
    public Integer            totalPageCount;
    /**
     * 总共有几页 -- 先兼容小程序  -- 小程序发布后期删除
     */
    public Integer            totallPageCount;
    /**
     * 请求参数
     */
    public Object             param;
    /**
     * 当前页的记录
     */
    private List<T>     resultList;

    /**
     * 分页专用id
     */
    private String            boundaryId;

    /**
     * 
     * @param start
     *            开始行号
     * @param pageSize
     *            取值数量
     */
    public PageSet(Integer start, Integer pageSize) {
        this.start = start;
        this.pageSize = pageSize;
    }

    public PageSet() {
        super();
    }

    public Integer getCurrentPageNo() {
        return currentPageNo;
    }

    public void setCurrentPageNo(Integer currentPageNo) {
        this.currentPageNo = currentPageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getResultCount() {
        return resultCount;
    }

    public void setResultCount(Integer resultCount) {
        this.resultCount = resultCount;
    }

    public List<T> getResultList() {
        return resultList;
    }

    public void setResultList(List<T> resultList) {
        this.resultList = resultList;
    }

    /**
     * 是否需要查询总数(pageNo 不为空的时候就是需要总页数的时候)
     * 
     * @return 2017年3月21日
     */
    public boolean isNeedCount() {
        return currentPageNo != null;
    }

    public Integer getTotalPageCount() {
        return totalPageCount;
    }

    public void setTotalPageCount(Integer totalPageCount) {
        this.totalPageCount = totalPageCount;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Object getParam() {
        return param;
    }

    public void setParam(Object param) {
        this.param = param;
    }

    public String getBoundaryId() {
        return boundaryId;
    }

    public void setBoundaryId(String boundaryId) {
        this.boundaryId = boundaryId;
    }

    public Integer getTotallPageCount() {
        return totallPageCount;
    }

    public void setTotallPageCount(Integer totallPageCount) {
        this.totallPageCount = totallPageCount;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    @Override
    public String toString() {
        return "PageSet [start=" + start + ", pageSize=" + pageSize + ", resultCount=" + resultCount + ", currentPageNo=" + currentPageNo + ", totalPageCount=" + totalPageCount + ", param=" + param
               + ", resultList=" + resultList + ", boundaryId=" + boundaryId + "]";
    }

}
