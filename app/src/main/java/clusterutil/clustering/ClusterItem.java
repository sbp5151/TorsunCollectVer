/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */

package clusterutil.clustering;


import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.model.LatLng;
import com.jld.torsun.activity.baiduMap.Info;

/**
 * ClusterItem represents a marker on the map.
 */
public interface ClusterItem {

    /**
     * The position of this marker. This must always return the same value.
     */
    LatLng getPosition();

    BitmapDescriptor getBitmapDescriptor();
    BitmapDescriptor getDefaultBitmapDescriptor();

    /**获取item对应的游客对象，便于通过marker获取游客信息*/
    Info getMarkerItem();
}