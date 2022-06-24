package com.wvp.domain.po;

import com.github.pagehelper.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * desc:返回的实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {

    private int code;
    private String msg;
    private String msgUUID;
    private Object data;


    public Result success(String msg, Object data) {
        this.code = 200;
        this.msg = msg;
        this.data = data;
        return this;
    }

    public Result success(String msg) {
        this.code = 200;
        this.msg = msg;
        return this;
    }

    public Result faceSuccess(String msg) {
        this.code = 101;
        this.msg = msg;
        return this;
    }

    public Result fail(String msg) {
        this.code = 500;
        this.msg = msg;
        return this;
    }

    public Result fail(String msg, Object data) {
        this.code = 500;
        this.msg = msg;
        this.data = data;
        return this;
    }

    public Result unPrize(String msg) {
        this.code = 202;
        this.msg = msg;
        return this;
    }
}
