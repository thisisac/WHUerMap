package com.example.campusnavigator; // ⚠️注意：修改为你自己的项目实际包名

public interface WhuRouteListner {
    /**
     * 路径查询成功后的回调函数
     * @param route 返回的路径结果对象
     */
    void onRoute(WhuRoute route);
}