package com.riozenc.task.webapp.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.List;

/**
 * @author belov
 * 查询条件
 */
@XStreamAlias("Item")
public class QueryConditions {

    @XStreamImplicit(itemFieldName="Data")
    private List<String> conditions;

    public List<String> getConditions() {
        return conditions;
    }

    public void setConditions(List<String> conditions) {
        this.conditions = conditions;
    }

}
