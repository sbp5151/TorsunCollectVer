package com.jld.torsun.modle;

import java.io.Serializable;

/**
 * Created by lz on 2016/7/8.
 * 电影类型
 */
public class MovieTypeBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     *  {
     "id": "1",
     "name": "科幻片"
     },
     */
    private String id;
    private String name;

    public MovieTypeBean() {
    }

    public MovieTypeBean(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "MovieTypeBean{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
